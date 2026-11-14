# RiftGuard v0.81 — Story Presentation and Touch-Layer Polish

**Status:** Alpha development branch; release approval and device playtesting remain separate. Builds use `0.81.0-alpha1` / versionCode 77.

## Actual in-game improvements

1. **Illustrated dialogue:** Preserve all existing native Canvas realm/character cinematic art, changing only the subtitle overlay. A readable dark inset panel, contrasting speaker name, gold/purple speaker accent, scene progress indicator and an unobstructed continue prompt. Every authored dialogue/memory/camp beat is measured and wrapped to fit within the painted panel **without truncation**. Layout is cached **once per story beat**, not allocated on every frame; original skip button's rendered bounds and touch rect stay unchanged.
2. **Chronicle:** Total memories, Cael memories and world seal status grouped into a legible summary. Each kingdom row gets an earned/10 progress strip. The selected kingdom and all 10 memories remain on-screen in the same arrangement, but visually distinguish recovered/tappable and missing/replayable memories. World and memory taps use exactly the painted rectangular cards; 4–8px visual gutters no longer unintentionally select adjacent items.
3. **Eclipse Cave choice:** Three narrative consequences are visually separated with large, clearly labeled touch targets, body descriptions, and live Cael memory threshold progress. The locked mercy choice visibly displays how many Cael keys remain and stays noninteractive until legitimately unlocked. The other outcomes remain available. **Same save keys/endings** and choice coordinates, now unified in `StoryPresentationV081`.
4. **Cave ending summary:** Consistent ending title, contextual color, readable epilogue card and obvious replay/return actions. Exact tap rectangles match the painted buttons (eliminates invisible taps between cards).
5. **HUD editor:** Higher-contrast draggable rings indicate the actual touch radius; instructions clarify SAVE (persist), RESET (preview) and CANCEL (discard). **No drag physics, saved layout, hitbox, combat or button-position changes**.
6. **Aspect ratios:** Existing game scale/letterbox input mapping is retained; new touch regression coverage exercises the actual Cave UI on 1280×720 and 2340×1080.

## Verified code/test coverage

- `StoryPresentationV081Test`: all prologue/chapter/boss/memory/camp/Cave dialogue fully fits the panel; no text words are silently lost; exact choice/ending/Chronicle hitboxes, including gutters and disabled mercy.
- `StoryScreensV081IntegrationTest`: actual native Android Canvas screenshots for Chronicle, dialogue, Cave choices and Cave ending. It checks in-game touch and saves, scene transitions, skip story, locked choice, button gutters and ultrawide coordinate mapping.
- Existing ~440+ unit/Robolectric tests, Android CI APK/input/graphics diagnostics, and dedicated Android emulator QA remain mandatory before merge.
- Screenshot artifacts from GitHub Actions include `281-v081-chronicle-recovery-progress.png` through `285-v081-cave-choices-ultrawide.png`.

## Explicitly not included

No 300-stage rebalance, storyline rewrite, new art pack, combat loop refactor, save migration, core physics changes, external network change, Play publication or production signing. Automated screenshot generation is not equivalent to comprehensive human art review or physical-device testing.
