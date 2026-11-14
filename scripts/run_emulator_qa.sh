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
# Android system immersive-mode first-run tutorial obscures actual game art
# and intercepts tap inputs. Suppress it for repeatable QA screenshots.
adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell am start -W -n "$PKG/.MainActivity" | tee emulator-qa/launch.txt
sleep 4
adb shell screencap -p /sdcard/rg-home.png
adb pull /sdcard/rg-home.png emulator-qa/01-home.png
# Test actual upgrade pages and their common four-button footer.
adb shell input tap 219 535
sleep 2
# Some emulator images ignore the secure setting; dismiss system tutorial.
# "Got it" is at 800,304 in the 1280x720 virtual landscape display.
adb shell input tap 800 304
sleep 1
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
# Real touch preview: future fortress appearance is shown without purchase.
adb shell input tap 530 526
sleep 1
adb shell screencap -p /sdcard/rg-next-fortress.png
adb pull /sdcard/rg-next-fortress.png emulator-qa/04b-fortress-next-look.png
adb shell input tap 530 526
adb shell input tap 765 656
sleep 2
adb shell screencap -p /sdcard/rg-ascension.png
adb pull /sdcard/rg-ascension.png emulator-qa/05-ascension-upgrade-tree.png
adb shell input tap 204 656
adb shell input tap 204 656
sleep 1
# The actual HOME WARDEN LV tile opens a fully native, interactive stat sheet.
adb shell input tap 449 389
sleep 2
adb shell screencap -p /sdcard/rg-warden-level.png
adb pull /sdcard/rg-warden-level.png emulator-qa/05b-warden-level-and-stats.png
adb shell input tap 202 655
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
sleep 2
# Android 16 sometimes displays the *system-owned* fullscreen coach;
# dismiss it first. Then use the real visible SKIP CHAPTER button.
# A physical BACK key can be intercepted by that system coach and leave
# the authored story open, producing fake "combat" screenshots.
adb shell input tap 927 284 || true
sleep 1
adb shell input tap 1130 76
sleep 1
# A second identical tap is harmless after the chapter closes (its
# location is outside Pause and all live action controls), but makes
# the test robust when Android consumed the first tap as system coaching.
adb shell input tap 1130 76
sleep 2
# First wave has an 18-second in-game preparation timer. Launch it using
# the same visible BEGIN WAVE 1 button a player must press; otherwise the
# old QA proved controls in an *empty* map rather than enemy combat.
adb shell input tap 640 243
sleep 2
# Frame telemetry must measure real active enemy waves, not chapter pages,
# splash screens, the 18s intermission, or zero-enemy scenery.
adb shell dumpsys gfxinfo "$PKG" reset >/dev/null || true
sleep 8
adb shell screencap -p /sdcard/rg-battle.png
adb pull /sdcard/rg-battle.png emulator-qa/04-battle.png
# Verify genuine Android process-death recovery WHILE THE WARDEN IS STILL
# ALIVE. Dumping multiple slow gfxinfo/meminfo/logcat reports before killing
# the app previously let the unattended enemies win and legitimately clear
# the checkpoint. Never alter battle/defeat rules just to pass CI.
adb shell am force-stop "$PKG"
# Keep recovery log isolated from the previous Activity instance.
adb shell logcat -c || true
adb shell am start -W -n "$PKG/.MainActivity" > emulator-qa/relaunch.txt
sleep 4
adb shell screencap -p /sdcard/rg-recovered-home.png
adb pull /sdcard/rg-recovered-home.png emulator-qa/06-relaunch-with-continue.png
# Use the actual visible CONTINUE action on HOME.
adb shell input tap 950 338
sleep 4
adb shell screencap -p /sdcard/rg-recovered-wave.png
adb pull /sdcard/rg-recovered-wave.png emulator-qa/07-restored-wave-paused.png
adb shell logcat -d -v brief -s RiftGuardQA:I > emulator-qa/qa-recovery-state.txt

# A recovered wave is intentionally paused. RESUME via the real button; then
# verify the live combat controls against the same recovered battle.
adb shell input tap 640 280
sleep 2
adb shell dumpsys gfxinfo "$PKG" reset >/dev/null || true
sleep 5
# Exercise all five live touchscreen controls on the running Android game:
# MOVE via swipe, DODGE, BUILD, SKILL and ATTACK.
adb shell input swipe 180 570 280 570 500 || true
adb shell input tap 729 590
adb shell input tap 849 590
adb shell input tap 974 593
adb shell input tap 1118 590
# Capture IMMEDIATELY; the Warden must be alive when testing input.
adb shell screencap -p /sdcard/rg-combat.png
adb pull /sdcard/rg-combat.png emulator-qa/05-touch-combat.png
# Debug-only app diagnostics prove the recovered battle has real enemies
# and that all five input controls received touches, not story/menu taps.
adb shell logcat -d -v brief -s RiftGuardQA:I > emulator-qa/qa-state.txt
adb shell dumpsys gfxinfo "$PKG" framestats > emulator-qa/gfxinfo-framestats.txt
adb shell dumpsys meminfo "$PKG" > emulator-qa/memory.txt
adb shell dumpsys activity processes > emulator-qa/processes.txt
adb shell logcat -d -v threadtime > emulator-qa/logcat.txt
python3 scripts/analyze_emulator_qa.py emulator-qa
