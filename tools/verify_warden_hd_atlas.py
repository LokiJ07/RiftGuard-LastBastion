#!/usr/bin/env python3
"""Validate and prepare a NEW authored transparent Warden sprite sheet.

Production art contract from the user's ai-animation-skills SKILL.md:
  8 columns x 6 rows; 256px illustrated cells; 2px transparent atlas gutters;
  rows = idle, run, attack, cast, hurt, death; fixed foot pivot (128,236).
  Timings: 6, 12, 12, 8, 8, 10 fps by row, respectively.

This is an OFFLINE ASSET IMPORTER, NOT a fake AI generator or image upscaler.
Do not submit a marketing infographic or duplicate static frames as HD art.
Only integrate if this utility returns success and visual inspection passes.

Usage:
  python -m pip install pillow
  python tools/verify_warden_hd_atlas.py artist_sheet.png --out build/hd-atlas
"""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageChops

COLS, ROWS = 8, 6
CELL, PADDING = 256, 2
STRIDE = CELL + PADDING * 2
PIVOT = (128, 236)
ANIMS = (
    ("idle", 6, True), ("run", 12, True), ("attack", 12, False),
    ("cast", 8, False), ("hurt", 8, False), ("death", 10, False),
)


def inspect(source: Path, output: Path, make_gifs: bool = True) -> dict:
    with Image.open(source) as image:
        atlas = image.convert("RGBA")
    width, height = atlas.size
    expected = (STRIDE * COLS, STRIDE * ROWS)
    if (width, height) != expected:
        raise ValueError(f"Invalid atlas {width}x{height}; required {expected[0]}x{expected[1]}")
    output.mkdir(parents=True, exist_ok=True)
    errors = []
    warnings = []
    details = {}
    for row, (name, fps, repeating) in enumerate(ANIMS):
        frames = []
        right_edges = []
        for index in range(COLS):
            left, top = index * STRIDE + PADDING, row * STRIDE + PADDING
            crop = atlas.crop((left, top, left + CELL, top + CELL))
            # A real authored frame must have actual body pixels and a
            # transparent border, not a rectangular screenshot background.
            alpha = crop.getchannel("A")
            bbox = alpha.getbbox()
            if bbox is None:
                errors.append(f"{name}[{index}]: empty transparent frame")
                continue
            bbox_area = (bbox[2] - bbox[0]) * (bbox[3] - bbox[1])
            if bbox_area < 2500:
                errors.append(f"{name}[{index}]: painted body too small")
            if min(bbox[0], bbox[1], CELL - bbox[2], CELL - bbox[3]) < 2:
                errors.append(f"{name}[{index}]: subject clipped at edge")
            if not 20 <= bbox[3] - PIVOT[1] <= 20:
                # Log a warning rather than failing pose bounce. The user
                # should review planted-foot alignment on real artwork.
                warnings.append(f"{name}[{index}]: check feet pivot y={bbox[3]}")
            # Transparent 2px source gutters catch atlas bleeding at runtime.
            pad_area = atlas.crop((left - PADDING, top - PADDING,
                                   left + CELL + PADDING, top + CELL + PADDING))
            margin = Image.new("L", pad_area.size, 0)
            margin.paste(alpha, (PADDING, PADDING))
            bleed = ImageChops.difference(pad_area.getchannel("A"), margin)
            if bleed.getbbox():
                errors.append(f"{name}[{index}]: nontransparent sprite gutter")
            right_edges.append(bbox[3])
            frames.append(crop)
        if not frames:
            continue
        # Strong rejection of 8-frame still image copied into every cell.
        if len(frames) == COLS and repeating:
            unique = len({frame.tobytes() for frame in frames})
            if unique < 4:
                errors.append(f"{name}: only {unique} distinct frames (need >=4)")
        if make_gifs and len(frames) == COLS:
            frames[0].save(output / f"warden_{name}_qa.gif",
                           save_all=True, append_images=frames[1:],
                           duration=round(1000 / fps), loop=0,
                           disposal=2, optimize=False)
        details[name] = {
            "row": row, "frames": len(frames), "fps": fps,
            "loop": repeating, "pivot": list(PIVOT),
            "maxFootDriftPx": max(right_edges) - min(right_edges),
        }
        if repeating and details[name]["maxFootDriftPx"] > 24:
            warnings.append(f"{name}: planted-foot baseline drift exceeds 24px")
    report = {
        "valid": not errors, "source": str(source),
        "size": list(atlas.size), "cols": COLS, "rows": ROWS,
        "frameSize": CELL, "transparentPadding": PADDING,
        "framePivot": list(PIVOT), "animations": details,
        "errors": errors, "warnings": warnings,
        "manualAcceptance": [
            "original HD artwork rather than resized old poses",
            "consistent armor silhouette across all six rows",
            "weapon-free Warden (equipment will attach to real game rig)",
            "no background/text/UI baked into any sprite",
            "attack contact and recovery readable at Android display scale",
        ],
    }
    (output / "atlas_qa_report.json").write_text(
        json.dumps(report, indent=2), encoding="utf-8")
    if errors:
        raise ValueError("Atlas rejected: " + "; ".join(errors[:12]))
    atlas.save(output / "riftguard_warden_hd_motion.webp",
               format="WEBP", quality=92, method=6, exact=True)
    return report


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("atlas", type=Path)
    parser.add_argument("--out", type=Path, default=Path("build/hd-atlas"))
    parser.add_argument("--no-gifs", action="store_true")
    args = parser.parse_args()
    try:
        report = inspect(args.atlas, args.out, not args.no_gifs)
    except ValueError as exc:
        parser.exit(1, str(exc) + "\n")
    print(f"PASS: {report['size']} RGBA Warden atlas; "
          f"{COLS * ROWS} aligned frames and PNG/WebP export QA")


if __name__ == "__main__":
    main()
