#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=com.lokij07.riftguard
# The emulator is a real Android OS running on a virtual device, not Robolectric.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell wm size 1280x720
adb shell wm density 240
adb shell settings put system accelerometer_rotation 0 || true
adb shell settings put system user_rotation 1 || true
adb shell am start -W -n "$PKG/.MainActivity" | tee emulator-qa/launch.txt
sleep 4
adb shell screencap -p /sdcard/rg-home.png
adb pull /sdcard/rg-home.png emulator-qa/01-home.png
# Test actual upgrade pages and their common four-button footer.
adb shell input tap 219 535
sleep 2
adb shell screencap -p /sdcard/rg-armory.png
adb pull /sdcard/rg-armory.png emulator-qa/02-armory-evolved-weapons.png
adb shell input tap 1060 656
sleep 2
adb shell screencap -p /sdcard/rg-mastery.png
adb pull /sdcard/rg-mastery.png emulator-qa/03-weapon-mastery.png
adb shell input tap 204 656
adb shell input tap 478 656
sleep 2
adb shell screencap -p /sdcard/rg-fortress.png
adb pull /sdcard/rg-fortress.png emulator-qa/04-fortress-arsenal.png
adb shell input tap 765 656
sleep 2
adb shell screencap -p /sdcard/rg-ascension.png
adb pull /sdcard/rg-ascension.png emulator-qa/05-ascension-upgrade-tree.png
adb shell input tap 204 656
adb shell input tap 204 656
sleep 1
adb shell input tap 220 452
sleep 2
adb shell screencap -p /sdcard/rg-map.png
adb pull /sdcard/rg-map.png emulator-qa/02-map.png
adb shell input tap 600 621
sleep 2
adb shell screencap -p /sdcard/rg-brief.png
adb pull /sdcard/rg-brief.png emulator-qa/03-mission-briefing.png
adb shell input tap 930 624
sleep 4
adb shell screencap -p /sdcard/rg-battle.png
adb pull /sdcard/rg-battle.png emulator-qa/04-battle.png
# Exercise all four major combat controls on an actual Android touchscreen emulator:
# MOVE via swipe, DODGE, BUILD and ATTACK.
adb shell input swipe 180 570 280 570 500 || true
adb shell input tap 729 590
adb shell input tap 849 590
adb shell input tap 1118 590
sleep 4
adb shell screencap -p /sdcard/rg-combat.png
adb pull /sdcard/rg-combat.png emulator-qa/05-touch-combat.png
adb shell dumpsys gfxinfo "$PKG" framestats > emulator-qa/gfxinfo-framestats.txt
adb shell dumpsys meminfo "$PKG" > emulator-qa/memory.txt
adb shell dumpsys activity processes > emulator-qa/processes.txt
adb shell logcat -d -v threadtime > emulator-qa/logcat.txt
python3 scripts/analyze_emulator_qa.py emulator-qa
