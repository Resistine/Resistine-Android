#!/usr/bin/env python3
"""Build official_registry.jsonl from the F-Droid repo index.

Usage:
  python tools/fdroid_registry.py \
    --index https://f-droid.org/repo/index-v1.jar \
    --output app/src/main/assets/official_registry.jsonl
"""
from __future__ import annotations

import argparse
import io
import json
import os
import sys
import tempfile
import urllib.request
import zipfile

FDROID_PACKAGE_URL = "https://f-droid.org/packages/{package}/"


def _download(url: str) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": "ResistineRegistryBuilder/1.0"})
    with urllib.request.urlopen(req) as resp:
        return resp.read()


def _load_index_bytes(index_path: str) -> bytes:
    if index_path.startswith("http://") or index_path.startswith("https://"):
        return _download(index_path)
    with open(index_path, "rb") as f:
        return f.read()


def _extract_index_json(index_bytes: bytes, index_path: str) -> dict:
    if index_path.endswith(".jar"):
        with zipfile.ZipFile(io.BytesIO(index_bytes)) as zf:
            with zf.open("index-v1.json") as f:
                return json.load(f)
    if index_path.endswith(".json"):
        return json.loads(index_bytes.decode("utf-8"))
    raise ValueError("Unsupported index file. Use index-v1.jar or index-v1.json.")


def _pick_name(app: dict) -> str:
    name = app.get("name")
    if name:
        return name
    localized = app.get("localized")
    if isinstance(localized, dict):
        for key in ("en-US", "en_US", "en", "default"):
            entry = localized.get(key)
            if isinstance(entry, dict) and entry.get("name"):
                return entry["name"]
        for entry in localized.values():
            if isinstance(entry, dict) and entry.get("name"):
                return entry["name"]
    return ""


def _pick_category(app: dict) -> str:
    categories = app.get("categories")
    if isinstance(categories, list) and categories:
        return categories[0]
    category = app.get("category")
    return category or ""


def _pick_dev(app: dict) -> str:
    return app.get("authorName") or app.get("developerName") or app.get("author") or ""


def _latest_version(packages: dict, package_name: str) -> tuple[int, str]:
    versions = packages.get(package_name)
    if not isinstance(versions, list):
        return 0, ""
    best_code = -1
    best_name = ""
    for v in versions:
        try:
            code = int(v.get("versionCode", -1))
        except Exception:
            code = -1
        if code > best_code:
            best_code = code
            best_name = v.get("versionName") or ""
    return max(best_code, 0), best_name


def build_registry(index_data: dict, limit: int | None) -> list[dict]:
    apps = index_data.get("apps")
    packages = index_data.get("packages", {})
    if not isinstance(apps, list):
        raise ValueError("Unexpected index format: 'apps' list not found.")
    entries: list[dict] = []
    for app in apps:
        package_name = app.get("packageName")
        if not package_name:
            continue
        version_code, version_name = _latest_version(packages, package_name)
        entries.append(
            {
                "package": package_name,
                "name": _pick_name(app),
                "versionCode": version_code,
                "versionName": version_name,
                "certSha256": [],
                "devName": _pick_dev(app),
                "storeUrl": FDROID_PACKAGE_URL.format(package=package_name),
                "category": _pick_category(app),
            }
        )
        if limit and len(entries) >= limit:
            break
    return entries


def write_jsonl(entries: list[dict], output_path: str) -> None:
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        for entry in entries:
            f.write(json.dumps(entry, ensure_ascii=True))
            f.write("\n")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--index", required=True, help="Path or URL to F-Droid index-v1.jar or index-v1.json")
    parser.add_argument("--output", default="app/src/main/assets/official_registry.jsonl")
    parser.add_argument("--limit", type=int, default=0, help="Optional max number of apps to include")
    args = parser.parse_args()

    index_bytes = _load_index_bytes(args.index)
    index_data = _extract_index_json(index_bytes, args.index)
    entries = build_registry(index_data, limit=args.limit or None)
    write_jsonl(entries, args.output)
    print(f"Wrote {len(entries)} entries to {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
