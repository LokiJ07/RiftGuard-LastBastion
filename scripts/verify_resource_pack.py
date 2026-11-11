#!/usr/bin/env python3
"""CI smoke check: fetch every public resource file and verify its Git blob SHA.

No account, ads, keys, proprietary download service or nonstdlib packages.
This verifies the delivery endpoint used by the Android asset downloader,
in addition to the Android JVM manifest/checksum/cache tests.
"""
import hashlib
import json
import re
import sys
import urllib.request

MANIFEST = "https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/main/game-content/manifest.json"
ORIGIN = "https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/"
EXPECTED = {
    "riftguard_atlas.webp",
    "riftguard_castle_game.webp",
    "riftguard_enemy_motion.webp",
    "riftguard_hero_motion.webp",
    "riftguard_terrain_tiles.webp",
    "riftguard_weapons_motion.webp",
    "riftguard_world_props.webp",
}


def download(url, limit):
    request = urllib.request.Request(url, headers={"User-Agent": "RiftGuard-CI-Resource-Verifier"})
    with urllib.request.urlopen(request, timeout=25) as response:
        result = response.read(limit + 1)
        if len(result) > limit:
            raise RuntimeError(f"Oversized download at {url}")
        return result


def check():
    manifest = json.loads(download(MANIFEST, 32768))
    assert manifest["version"] >= 1
    assets = manifest["assets"]
    assert isinstance(assets, list) and len(assets) == 7
    seen = set()
    for entry in assets:
        name, url, blob = entry["name"], entry["url"], entry["git_sha"]
        size = int(entry["bytes"])
        assert name in EXPECTED and name not in seen
        seen.add(name)
        assert 100 < size <= 20 * 1024 * 1024
        assert re.fullmatch(r"[a-f0-9]{40}", blob)
        assert url.startswith(ORIGIN)
        assert re.fullmatch(r"[a-f0-9]{40}", url[len(ORIGIN):][:40])
        assert url == ORIGIN + url[len(ORIGIN):][:40] + "/app/src/main/res/drawable-nodpi/" + name
        body = download(url, size)
        assert len(body) == size, f"Wrong byte size for {name}: {len(body)} != {size}"
        actual = hashlib.sha1(b"blob " + str(size).encode() + bytes([0]) + body).hexdigest()
        assert actual == blob, f"Checksum mismatch: {name}"
        print(f"OK: {name} ({size} bytes; verified Git blob {blob[:10]})", flush=True)
    assert seen == EXPECTED
    print("PASS: all 7 downloadable resources are reachable, complete and verified.", flush=True)


if __name__ == "__main__":
    try:
        check()
    except Exception as exc:
        print(f"RESOURCE DELIVERY FAILED: {exc}", file=sys.stderr)
        sys.exit(1)
