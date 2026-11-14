#!/usr/bin/env python3
"""Non-networking, fail-closed RiftGuard commercial packaging preflight.

Preflight can pass on ALPHA development builds; it *never* declares the game
commercially approved. Production requires human QA attestation + protected
signing credentials + a non-prerelease app version.
"""
import argparse
import json
import os
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
REQUIRED_KEYS = (
    "RIFTGUARD_RELEASE_KEYSTORE_FILE",
    "RIFTGUARD_RELEASE_STORE_PASSWORD",
    "RIFTGUARD_RELEASE_KEY_ALIAS",
    "RIFTGUARD_RELEASE_KEY_PASSWORD",
)


def analyze(root: Path, mode: str = "preflight", human_qa: bool = False, environ=None):
    if mode not in ("preflight", "production"):
        raise ValueError("unknown audit mode")
    env = os.environ if environ is None else environ
    failures, warnings = [], []
    gpath = root / "app/build.gradle"
    mpath = root / "app/src/main/AndroidManifest.xml"
    if not gpath.is_file() or not mpath.is_file():
        return {"status": "blocked", "mode": mode, "failures": [
            "Android Gradle config and Android manifest must exist"
        ], "warnings": [], "checks": {}}
    gradle = gpath.read_text(encoding="utf-8")
    properties = {}
    for key in ("compileSdk", "targetSdk", "minSdk", "versionCode"):
        match = re.search(r"(?m)^\s*" + key + r"\s+(\d+)\s*$", gradle)
        if match:
            properties[key] = int(match.group(1))
        else:
            failures.append(f"Missing numeric {key} in Gradle config")
    match = re.search(r"(?m)^\s*versionName\s+['\"]([^'\"]+)['\"]", gradle)
    version = match.group(1) if match else ""
    if not version:
        failures.append("Missing versionName")
    if properties.get("compileSdk", 0) < 36 or properties.get("targetSdk", 0) < 36:
        failures.append("Google Play mobile submissions require API 36+ (2026)")
    if properties.get("minSdk", 999) > properties.get("targetSdk", 0):
        failures.append("minSdk must not exceed targetSdk")
    if properties.get("versionCode", 0) < 1:
        failures.append("versionCode must be positive")

    try:
        manifest = ET.parse(mpath).getroot()
        apps = manifest.findall("application")
        if len(apps) != 1:
            failures.append("Expected exactly one Android application")
        else:
            app = apps[0]
            if app.attrib.get(ANDROID + "debuggable") == "true":
                failures.append("Application cannot be explicitly debuggable")
            if app.attrib.get(ANDROID + "usesCleartextTraffic") != "false":
                failures.append("Cleartext traffic must remain disabled")
            if app.attrib.get(ANDROID + "appCategory") != "game":
                warnings.append("Android appCategory should remain game")
            exported = [
                a for a in app.findall("activity")
                if a.attrib.get(ANDROID + "exported") == "true"
            ]
            if len(exported) != 1:
                warnings.append("Review exported activity list")
        perms = [
            p.attrib.get(ANDROID + "name", "")
            for p in manifest.findall("uses-permission")
        ]
        for dangerous in ("READ_SMS", "READ_CONTACTS", "ACCESS_FINE_LOCATION",
                          "RECORD_AUDIO", "QUERY_ALL_PACKAGES"):
            if any(s.endswith(dangerous) for s in perms):
                failures.append(f"Unexpected sensitive permission {dangerous}")
    except ET.ParseError:
        failures.append("AndroidManifest.xml is not parseable")

    if mode == "production":
        if re.search(r"(?i)(alpha|beta|snapshot|dev|rc)[-_.]?\d*", version):
            failures.append("Production build cannot use a prerelease versionName")
        if not human_qa:
            failures.append("Human playtest, physical device and asset-rights signoff required")
        missing = [k for k in REQUIRED_KEYS if not env.get(k)]
        if missing:
            # Report names only, NEVER values or secret content.
            failures.append("Missing protected production signing configuration: " +
                            ", ".join(missing))
        else:
            keystore = Path(env["RIFTGUARD_RELEASE_KEYSTORE_FILE"])
            if not keystore.is_file() or keystore.stat().st_size < 100:
                failures.append("Production signing keystore file missing or invalid")
        if "signingConfig signingConfigs.production" not in gradle:
            failures.append("Release variant must use protected production signing")
    else:
        warnings.append("Development CI output is UNSIGNED; not for Play production")
    warnings.append("Automated packaging checks do not validate real-device QA, " +
                    "licensed assets, Data safety or Play Console testing approval")
    return {
        "status": "pass" if not failures else "blocked",
        "mode": mode,
        "checks": {**properties, "versionName": version},
        "failures": failures,
        "warnings": warnings,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent)
    parser.add_argument("--mode", choices=("preflight", "production"), default="preflight")
    parser.add_argument("--human-qa", action="store_true",
                        help="Attest human real-device, rights and playtest QA completed")
    parser.add_argument("--output", type=Path, default=None)
    args = parser.parse_args()
    result = analyze(args.root, args.mode, args.human_qa)
    message = json.dumps(result, indent=2)
    print(message)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(message + "\n", encoding="utf-8")
    sys.exit(0 if result["status"] == "pass" else 1)


if __name__ == "__main__":
    main()
