#!/usr/bin/env python3
"""Summarize actual Android emulator frame/memory/log captures, never claim
to represent a physical handset. Exit nonzero for a missing/crashed app."""
from pathlib import Path
import json, re, statistics, sys

folder=Path(sys.argv[1] if len(sys.argv)>1 else "emulator-qa")
shots=sorted(folder.glob("*.png"))
if len(shots)<5 or any(f.stat().st_size<2000 for f in shots):
    raise SystemExit("FAIL: Required real Android emulator screenshot(s) missing")
gfx=(folder/"gfxinfo-framestats.txt").read_text(errors="replace")
from gfxinfo_parser import frame_times_ms
frames=frame_times_ms(gfx)
log=(folder/"logcat.txt").read_text(errors="replace")
# A fatal in unrelated Android services must not be misreported as our app.
fatal=bool(re.search(r"FATAL EXCEPTION:[\s\S]{0,800}Process:\s*com\.lokij07\.riftguard",log))
launch=(folder/"launch.txt").read_text(errors="replace")
if "Error:" in launch or "Exception" in launch:
    raise SystemExit("FAIL: Android activity did not launch")
if fatal:
    raise SystemExit("FAIL: RiftGuard crashed in Android emulator logcat")
# Real PLAY + five input dispatch events are mandatory. A screenshot of a
# seven-scene chapter cannot pass a combat smoke test again.
state=(folder/"qa-state.txt").read_text(errors="replace") if (
    folder/"qa-state.txt").exists() else ""
if "scene=PLAY story=false paused=false briefing=false" not in state:
    raise SystemExit("FAIL: Emulator combat screenshot was not actual PLAY")
for control in ("MOVE","DODGE","BUILD","SKILL","ATTACK"):
    if f"control={control} scene=PLAY story=false" not in state:
        raise SystemExit(f"FAIL: Actual battle never received {control} touch")
if len(frames)<8:
    raise SystemExit("FAIL: Insufficient Android graphics frame measurements")
p50=statistics.median(frames)
ordered=sorted(frames)
p95=ordered[min(len(ordered)-1,int((len(ordered)-1)*.95))]
p99=ordered[min(len(ordered)-1,int((len(ordered)-1)*.99))]
mem=(folder/"memory.txt").read_text(errors="replace")
m=re.search(r"TOTAL PSS:\s*([\d,]+)",mem)
if not m:m=re.search(r"\bTOTAL\s+([\d,]+)",mem)
summary={
    "environment":"Android Emulator (virtualized; NOT a physical phone)",
    "captures":len(shots),
    "actual_playing_battle_verified":True,
    "accepted_touch_controls":["MOVE","DODGE","BUILD","SKILL","ATTACK"],
    "frame_count":len(frames),
    "frame_ms_p50":round(p50,2),
    "frame_ms_p95":round(p95,2),
    "frame_ms_p99":round(p99,2),
    "over_16_67_ms":sum(t>16.67 for t in frames),
    "over_33_33_ms":sum(t>33.33 for t in frames),
    "total_pss_kb":int(m.group(1).replace(",","")) if m else None,
    "app_crash":False,
    "qa_verdict":"PASS: launch, input walkthrough, screenshot generation and crash detection; FPS reported, not certified."
}
(folder/"summary.json").write_text(json.dumps(summary,indent=2)+"\n")
print(json.dumps(summary,indent=2))
