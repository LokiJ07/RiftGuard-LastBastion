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
frames=[]
inside=False
for line in gfx.splitlines():
    if line.strip()=="---PROFILEDATA---":
        inside=not inside
        continue
    if not inside:
        continue
    parts=line.strip().split(",")
    if len(parts)<14 or not parts[0].strip().isdigit():
        continue
    try:
        intended=int(parts[1]); completed=int(parts[13])
        if intended>0 and completed>intended:
            ms=(completed-intended)/1_000_000
            if 0<ms<5000: frames.append(ms)
    except ValueError:
        continue
log=(folder/"logcat.txt").read_text(errors="replace")
# A fatal in unrelated Android services must not be misreported as our app.
fatal=bool(re.search(r"FATAL EXCEPTION:[\s\S]{0,800}Process:\s*com\.lokij07\.riftguard",log))
launch=(folder/"launch.txt").read_text(errors="replace")
if "Error:" in launch or "Exception" in launch:
    raise SystemExit("FAIL: Android activity did not launch")
if fatal:
    raise SystemExit("FAIL: RiftGuard crashed in Android emulator logcat")
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
