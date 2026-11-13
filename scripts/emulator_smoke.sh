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
# Confirm Android's first-run immersive-mode tutorial, which previously
# covered the battlefield and falsely counted system-overlay taps as game QA.
adb shell settings put secure immersive_mode_confirmations confirmed || true
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
# An emulator robot is slower than a real player, so use the actual EASY
# difficulty control to prevent time-to-defeat during instrumentation.
adb shell input tap 939 614
adb shell input tap 620 619
sleep 2
take "04-mission-briefing"
adb shell input tap 890 615
sleep 2
# Some emulator images ignore the setting until the full-screen window opens.
# The dialog's real Android "Got it" button is at (800,304) on 1280x720.
adb shell input tap 800 304
sleep 1
# First-time v0.65 players now receive a real seven-shot prologue followed
# by a Greenhaven chapter. The emulator must acknowledge each authored beat
# before validating actual battle controls, not mistake the story for a crash.
take "05-story-prologue"
for scene_beat in $(seq 1 12); do
  adb shell input tap 1100 645
done
sleep 1
take "05-battle-start"
# Reset Android's hardware frame timeline, then issue actual movement/touch.
adb shell dumpsys gfxinfo "$APP" reset >/dev/null || true
# Real battle inputs are tested promptly before time-consuming ADB shell
# command overhead can advance the stage into LOSE -> map navigation.
# Also construct a defense so the battle persists long enough for the test.
adb shell input tap 849 589
adb shell input swipe 150 565 215 520 160
adb shell input tap 730 590
adb shell input tap 1125 560
sleep 0.2
take "06-battle-interaction"
# The battle screenshot must differ from the initial start state and cannot
# silently be the atlas. This test is image-based but not dependent on OCR.
python3 - "$OUT" <<'PY'
import sys, pathlib, zlib, struct
p=pathlib.Path(sys.argv[1])
def read_png(path):
    data=path.read_bytes(); pos=8; compressed=b''
    while pos+12 <= len(data):
        n=int.from_bytes(data[pos:pos+4],'big')
        name=data[pos+4:pos+8]
        chunk=data[pos+8:pos+8+n]
        pos+=n+12
        if name==b'IDAT': compressed+=chunk
        if name==b'IEND': break
    return zlib.decompress(compressed)
a=read_png(p/"05-battle-start.png")
b=read_png(p/"06-battle-interaction.png")
if a==b:
    raise SystemExit("Battle inputs did not change any displayed pixels")
# On this fixed 1280x720 portrait-free emulator, (400,55) is the green
# CORE health fill only while the actual playable battle scene is visible.
# It is dark slate on the kingdom atlas or result navigation screen.
def sample_rgba(path,x,y):
    source=path.read_bytes()
    import struct
    width,height,depth,kind,_,_,_=struct.unpack('>IIBBBBB',source[16:29])
    if (width,height,depth,kind)!=(1280,720,8,6):
        raise SystemExit("Unexpected screenshot format for battle HUD assertion")
    pos=8; content=b''
    while pos<len(source):
        n=int.from_bytes(source[pos:pos+4],'big')
        tag=source[pos+4:pos+8]
        if tag==b'IDAT':content+=source[pos+8:pos+8+n]
        pos+=12+n
        if tag==b'IEND':break
    raw=zlib.decompress(content)
    stride=width*4; prev=bytearray(stride);offset=0
    for row in range(y+1):
        filt=raw[offset];offset+=1
        current=bytearray(raw[offset:offset+stride]);offset+=stride
        for i in range(stride):
            a=current[i-4] if i>=4 else 0
            b=prev[i]
            c=prev[i-4] if i>=4 else 0
            if filt==1: current[i]=(current[i]+a)&255
            elif filt==2: current[i]=(current[i]+b)&255
            elif filt==3: current[i]=(current[i]+(a+b)//2)&255
            elif filt==4:
                p=a+b-c
                pa,pb,pc=abs(p-a),abs(p-b),abs(p-c)
                predictor=a if pa<=pb and pa<=pc else b if pb<=pc else c
                current[i]=(current[i]+predictor)&255
            elif filt!=0: raise SystemExit("Unexpected PNG filter")
        prev=current
    off=x*4
    return tuple(prev[off:off+4])
r,g,b,_=sample_rgba(p/"06-battle-interaction.png",400,55)
if not(g>r+35 and g>b+20):
    raise SystemExit("Battle interaction ended on atlas/defeat/overlay, not live battle!")
print("BATTLE_SCENE_PRESENT_AFTER_REAL_INPUT; green CORE HUD pixel verified")
PY
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
