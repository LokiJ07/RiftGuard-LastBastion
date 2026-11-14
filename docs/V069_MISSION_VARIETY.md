# RiftGuard v0.69 — Playable Mission Variety, Development Alpha 1

Stacked on v0.68; no merge to main. Scope deliberately limited to **15
signature stages** (internal stage indices 6..20 / levels 3A through 7C)
so battle feel can be evaluated before spreading mechanics across 300 stages.

Three real, mutually exclusive **wave-two** experiences:
1. ESCORT: a moving, animated caravan uses the real world road, has 260 HP,
   attackers target and hurt it, and the battle fails if it is destroyed.
   Enemies being wiped out does not skip the cart reaching safety.
   On successful arrival it pays +45 **battle gold** once, not supplies.
2. SIEGE ENGINE: exactly one normal wave-two enemy is replaced by a
   distinctive armored cannon with visible 1.5s charge and 65-damage salvo
   (scaled by difficulty) targeting core HP. Destroy it to earn +35 battle
   gold once. Counts and enemy limits are unchanged.
3. SPLIT ROUTES: some existing wave-two attackers use upper/lower flanking
   routes that converge at the keep. They move faster, carry markers, and
   a no-breach defense earns +30 battle gold if two or more flankers die.

Story briefing and atlas thumbnails explain the stage event, live HUD
progress is visible, the convoy and threat markers use custom original
Canvas rendering and preserve existing environmental and character artwork.

**Recovery guarantee:** Events exist only in wave two. The existing
wave-boundary snapshot always restarts a wave from its beginning, so event
state is intentionally reset on retry/cold relaunch. All battle rewards are
applied to the original gold and captured at the next wave boundary;
they are not durable first-clear supplies and cannot be duplicated by
resuming a snapshot from the same already-completed wave.

**Remaining work:** curate these encounters through all ten realms,
player-test damage and duration (especially Expert), enhance mission NPC
animations, then build deeper upgrades and realm-exclusive bosses. CI,
emulator and devices have different validation roles. No production claims.

Release gates: Android CI testDebugUnitTest, assembleDebug, bundleRelease,
resource pack, Android 10 and Android 16 emulator input and resume QA, art
screenshots, low/mid/high physical devices, accessibility, signing/legal
assets/store preparation and human retention playtests.
