#!/usr/bin/env python3
"""Collect APK metadata from an attached device/emulator and update JSONL registry.

Requirements: adb, aapt, apksigner on PATH.

Example:
  python tools/collect_apk_metadata.py \
    --packages packages.txt \
    --output app/src/main/assets/official_registry.jsonl \
    --apk-dir tools/apks
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from typing import Dict, List, Tuple

PACKAGE_RE = re.compile(r"package: name='([^']+)' versionCode='([^']*)' versionName='([^']*)'")
LABEL_RE = re.compile(r"application-label:'([^']*)'")
SHA256_RE = re.compile(r"SHA-256 digest:\s*([0-9A-Fa-f:]+)")


def run(cmd: List[str], check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, check=check, text=True, capture_output=True)


def read_packages(path: str) -> List[str]:
    packages: List[str] = []
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            value = line.strip()
            if not value or value.startswith("#"):
                continue
            packages.append(value)
    return packages


def load_jsonl(path: str) -> Tuple[List[dict], Dict[str, int]]:
    entries: List[dict] = []
    index: Dict[str, int] = {}
    if not os.path.exists(path):
        return entries, index
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                continue
            pkg = obj.get("package")
            if isinstance(pkg, str) and pkg:
                index[pkg] = len(entries)
            entries.append(obj)
    return entries, index


def write_jsonl(path: str, entries: List[dict]) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        for entry in entries:
            f.write(json.dumps(entry, ensure_ascii=True))
            f.write("\n")


def adb_shell(adb: str, args: List[str]) -> str:
    result = run([adb, "shell"] + args, check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout


def adb_pull(adb: str, remote: str, local: str) -> None:
    os.makedirs(os.path.dirname(local), exist_ok=True)
    result = run([adb, "pull", remote, local], check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())


def get_apk_paths(adb: str, package: str) -> List[str]:
    output = adb_shell(adb, ["pm", "path", package])
    paths = []
    for line in output.splitlines():
        line = line.strip()
        if line.startswith("package:"):
            paths.append(line.replace("package:", "", 1))
    return paths


def pick_base_apk(paths: List[str]) -> str:
    for path in paths:
        if path.endswith("base.apk"):
            return path
    return paths[0]


def parse_aapt(aapt: str, apk_path: str) -> Tuple[str, int, str, str]:
    result = run([aapt, "dump", "badging", apk_path], check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    pkg = ""
    version_code = 0
    version_name = ""
    label = ""
    for line in result.stdout.splitlines():
        if line.startswith("package:"):
            match = PACKAGE_RE.search(line)
            if match:
                pkg = match.group(1)
                try:
                    version_code = int(match.group(2))
                except ValueError:
                    version_code = 0
                version_name = match.group(3)
        elif line.startswith("application-label:"):
            match = LABEL_RE.search(line)
            if match:
                label = match.group(1)
    return pkg, version_code, version_name, label


def parse_apksigner(apksigner: str, apk_path: str) -> List[str]:
    result = run([apksigner, "verify", "--print-certs", apk_path], check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    digests: List[str] = []
    for line in result.stdout.splitlines():
        match = SHA256_RE.search(line)
        if match:
            digest = match.group(1).replace(":", "").lower()
            digests.append(digest)
    return digests


def update_entry(existing: dict, package: str, name: str, version_code: int, version_name: str, certs: List[str]) -> dict:
    entry = dict(existing)
    entry["package"] = package
    if name and not entry.get("name"):
        entry["name"] = name
    entry["versionCode"] = version_code
    entry["versionName"] = version_name
    entry["certSha256"] = certs
    if not entry.get("storeUrl"):
        entry["storeUrl"] = f"https://play.google.com/store/apps/details?id={package}"
    if "devName" not in entry:
        entry["devName"] = ""
    if "category" not in entry:
        entry["category"] = ""
    if "name" not in entry:
        entry["name"] = name
    return entry


def create_entry(package: str, name: str, version_code: int, version_name: str, certs: List[str]) -> dict:
    return {
        "package": package,
        "name": name,
        "versionCode": version_code,
        "versionName": version_name,
        "certSha256": certs,
        "devName": "",
        "storeUrl": f"https://play.google.com/store/apps/details?id={package}",
        "category": "",
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--packages", required=True, help="Path to packages.txt")
    parser.add_argument("--output", default="app/src/main/assets/official_registry.jsonl")
    parser.add_argument("--apk-dir", default="tools/apks")
    parser.add_argument("--adb", default="adb")
    parser.add_argument("--aapt", default="aapt")
    parser.add_argument("--apksigner", default="apksigner")
    args = parser.parse_args()

    packages = read_packages(args.packages)
    entries, index = load_jsonl(args.output)

    for package in packages:
        try:
            paths = get_apk_paths(args.adb, package)
        except Exception as exc:
            print(f"[WARN] {package}: {exc}")
            continue
        if not paths:
            print(f"[WARN] {package}: no APK paths found")
            continue
        base_path = pick_base_apk(paths)

        local_dir = os.path.join(args.apk_dir, package)
        os.makedirs(local_dir, exist_ok=True)
        local_paths = []
        for path in paths:
            filename = os.path.basename(path)
            local_path = os.path.join(local_dir, filename)
            try:
                adb_pull(args.adb, path, local_path)
            except Exception as exc:
                print(f"[WARN] {package}: failed to pull {path}: {exc}")
                continue
            local_paths.append(local_path)

        base_local = None
        for path in local_paths:
            if path.endswith("base.apk"):
                base_local = path
                break
        if base_local is None and local_paths:
            base_local = local_paths[0]
        if base_local is None:
            print(f"[WARN] {package}: no local APK files")
            continue

        try:
            pkg_name, version_code, version_name, label = parse_aapt(args.aapt, base_local)
            certs = parse_apksigner(args.apksigner, base_local)
        except Exception as exc:
            print(f"[WARN] {package}: failed to parse APK: {exc}")
            continue

        pkg_name = pkg_name or package
        label = label or ""

        if pkg_name in index:
            entries[index[pkg_name]] = update_entry(
                entries[index[pkg_name]],
                pkg_name,
                label,
                version_code,
                version_name,
                certs,
            )
        else:
            entries.append(create_entry(pkg_name, label, version_code, version_name, certs))
            index[pkg_name] = len(entries) - 1

        print(f"[OK] {pkg_name} v{version_name} ({version_code})")

    write_jsonl(args.output, entries)
    return 0


if __name__ == "__main__":
    sys.exit(main())
