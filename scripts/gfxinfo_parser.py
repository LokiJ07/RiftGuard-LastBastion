"""Version-tolerant Android gfxinfo FrameTimeline CSV decoder.

Android 10 and Android 16 use different framestats field positions.
Decode by column name, never by fixed numeric indexes.
"""
def frame_times_ms(gfx: str) -> list[float]:
    frames: list[float] = []
    inside = False
    indices = None
    for line in gfx.splitlines():
        stripped = line.strip()
        if stripped == "---PROFILEDATA---":
            inside = not inside
            indices = None
            continue
        if not inside or not stripped:
            continue
        parts = [part.strip() for part in stripped.split(",")]
        if "IntendedVsync" in parts and "FrameCompleted" in parts:
            indices = (parts.index("IntendedVsync"), parts.index("FrameCompleted"))
            continue
        if indices is None or not parts[0].isdigit():
            continue
        try:
            first, last = indices
            if len(parts) <= max(first, last):
                continue
            intended, completed = int(parts[first]), int(parts[last])
            if intended > 0 and completed > intended:
                elapsed_ms = (completed - intended) / 1_000_000
                if 0 < elapsed_ms < 5000:
                    frames.append(elapsed_ms)
        except (ValueError, IndexError):
            continue
    return frames
