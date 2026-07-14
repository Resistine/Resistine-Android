#!/usr/bin/env python3
"""Receive synthetic Wazuh frames from the Android emulator for local QA."""

import argparse
import json
import socket
import struct
from pathlib import Path


def read_exact(connection: socket.socket, length: int) -> bytes:
    chunks = bytearray()
    while len(chunks) < length:
        chunk = connection.recv(length - len(chunks))
        if not chunk:
            raise ConnectionError(f"connection closed after {len(chunks)} of {length} bytes")
        chunks.extend(chunk)
    return bytes(chunks)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=1514)
    parser.add_argument("--frames", type=int, default=2)
    parser.add_argument("--agent-id", default="999")
    parser.add_argument("--timeout", type=int, default=60)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    expected_prefix = f"!{args.agent_id}!#AES:".encode("ascii")
    received = []

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((args.host, args.port))
        server.listen(1)
        server.settimeout(args.timeout)
        print(f"READY {args.host}:{args.port}", flush=True)

        connection, peer = server.accept()
        with connection:
            connection.settimeout(20)
            for index in range(args.frames):
                payload_length = struct.unpack("<I", read_exact(connection, 4))[0]
                payload = read_exact(connection, payload_length)
                received.append(
                    {
                        "index": index,
                        "payload_length": payload_length,
                        "has_expected_agent_prefix": payload.startswith(expected_prefix),
                        "prefix": payload[: len(expected_prefix)].decode("ascii", errors="replace"),
                    }
                )

    result = {
        "peer": peer[0],
        "frame_count": len(received),
        "all_prefixes_valid": all(frame["has_expected_agent_prefix"] for frame in received),
        "frames": received,
    }
    Path(args.output).write_text(json.dumps(result, indent=2), encoding="utf-8")
    print(json.dumps(result), flush=True)

    if len(received) != args.frames or not result["all_prefixes_valid"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
