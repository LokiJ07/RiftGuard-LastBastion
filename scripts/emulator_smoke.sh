#!/usr/bin/env bash
# Genuine emulator-level APK launch, menu navigation, battle input and
# Android gfxinfo/meminfo diagnostics. Not a physical handset benchmark.
set -euo pipefail
APP=com.lokij07.riftguard
PACKAGE="$APP/.MainActivity"
APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/emulator-qa"
mkdir -p "$OUT"
adb wait-for-device
adb shell wm size 1280x720
adb shell wm density 240
adb shell settings put system accelerometer_rotation 0 || true
adb shell settings put system user_rotation 1 || true
adb install -r "$APK"
adb shell am force-stop "$APP" || true
adb logcat -c || true
adb shell am start -W -n "$PACKAGE" | tee "$OUT/launch.txt"
sleep 4
check_running() {
  if ! adb shell pidof "$APP" >/dev/null; then
    echo "APP_PROCESS_MISSING at $1" | tee -a "$OUT/failure.txt"
    adb logcat -d -v time > "$OUT/logcat.txt"
    exit 1
  fi
}
take() {
  check_running "$1"
  adb exec-out screencap -p > "$OUT/$1.png"
  test "$(wc -c < "$OUT/$1.png")" -gt 3000
}
take "01-home"
# Tap genuine visible Campaign item and open region/level previews.
adb shell input tap 240 450
sleep 1
take "02-world-map"
adb shell input tap 190 470
sleep 1
take "03-regional-journey"
adb shell input tap 620 619
sleep 2
take "04-mission-briefing"
adb shell input tap 890 615
sleep 2
take "05-battle-start"
# Reset Android's hardware frame timeline, then issue actual movement/touch.
adb shell dumpsys gfxinfo "$APP" reset >/dev/null || true
for n in $(seq 1 12); do
  adb shell input swipe 150 565 215 520 180
  adb shell input tap 730 590
  adb shell input tap 1045 588
done
sleep 2
take "06-battle-interaction"
adb shell dumpsys gfxinfo "$APP" framestats > "$OUT/gfxinfo-framestats.txt" || true
adb shell dumpsys gfxinfo "$APP" > "$OUT/gfxinfo-summary.txt" || true
adb shell dumpsys meminfo "$APP" > "$OUT/memory.txt" || true
adb shell dumpsys activity top > "$OUT/activity-top.txt" || true
adb logcat -d -v time > "$OUT/logcat.txt" || true
# Frame completion sample is diagnostic, not a guarantee of physical-device FPS.
python3 - "$OUT" <<'PY'
import sys, pathlib, json, statistics
p=pathlib.Path(sys.argv[1])
text=(p/"gfxinfo-framestats.txt").read_text(errors="replace")
values=[]
record=False
for raw in text.splitlines():
    row=raw.strip()
    if row=="---PROFILEDATA---":
        record=not record
        continue
    if not record or not row.startswith("0,"):
        continue
    cols=row.split(",")
    try:
        intended=int(cols[1]); completed=int(cols[13])
        if intended>0 and completed>intended:
            milliseconds=(completed-intended)/1e6
            if .01<milliseconds<2000: values.append(milliseconds)
    except (ValueError,IndexError):
        continue
values.sort()
report={
    "test_environment":"GitHub Actions Android emulator API 29 x86_64",
    "physical_device_tested":False,
    "frame_samples":len(values),
    "median_frame_ms":round(statistics.median(values),2) if values else None,
    "p95_frame_ms":round(values[min(len(values)-1,int(len(values)*.95))],2) if values else None,
    "note":"Emulator graphics metrics; never interpret as handset FPS certification"
}
(p/"frame-report.json").write_text(json.dumps(report,indent=2)+"\n")
print(json.dumps(report))
PY
if grep -E "FATAL EXCEPTION|ANR in $APP" "$OUT/logcat.txt" > "$OUT/runtime-crashes.txt"; then
  echo "Android Runtime fatal crash/ANR detected" >&2
  exit 1
fi
test -s "$OUT/memory.txt"
echo "EMULATOR_SMOKE_PASS"
