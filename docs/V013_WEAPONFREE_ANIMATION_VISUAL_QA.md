# RiftGuard v0.13.0 — Visual overhaul Phase 1 (2026-11-12)

## Actual changed game code
1. **WardenBodyArt (new):** Original, vector-resolution scalable, weapon-free hero body, drawn in an independent multi-part rig. Includes separately articulated legs with planted footsteps, two knee joints, shaded greaves and boots, animated cape, metal torso with embossed emblem/buckle/rivets, rotating face/masked helm, and separately posed rear arm. Body does not include ANY fixed sword, bow, spear, gun or staff.
2. **MotionArt integration:** live hero draw now invokes WardenBodyArt for base armor, completely bypassing cropped painted torso frames that included a sidearm; the equipped one of six weapons still goes through the existing independent WeaponRig wrist transform. A change of weapon no longer leaves a built-in painted legacy weapon beneath it.
3. **BiomeAtmosphere (new):** Separated behind-world illumination and front-of-actor weather layers. Ten visually distinct deterministic biome types: pollen/fireflies, swamp spores, ash embers, snow drift, foundry smoke/heat, crystal shards, storm sleet, shadow fog and eclipse particles. Gradients, per-particle positions and movement run natively on Android without network.
4. **Visual QA:** Robolectric/native graphics tests check real pixel differences for running planted gait, idle, attack torso/cape, spell casting, hurt reaction, mirrored model, gear-free body, 6 externally equipped overlays, 10 different biomes and animated weather. Gallery creates actual rendered frames `36-weaponfree-warden-animation.png`, `37-animated-biome-atmosphere.png` plus whole-stage screenshots.

## AI animation Skills status
The user mentioned adding an exact **"AI animation skills"** skill, but the available installed skill list in this session does not contain that exact skill. Higgsfield is connected but generation balance currently reports **0 credits**, with no eligible unlimited trial. Therefore **no Higgsfield-generated video or animated sprite asset is falsely claimed**. The working animation upgrade is authored in native Android Canvas code. The separate infographic created for illustration does NOT count as an integrated game asset or proof of completed animations.

Example realistic prompt that could be used with a true sprite animation pipeline after a suitable generator becomes available:

> Create an original, weapon-free fantasy Warden animation for Android game RiftGuard. Maintain one identical armored character across a 4-column × 4-row transparent sprite atlas. Row 1: breathing idle; row 2: weight-bearing run with alternating planted feet; row 3: shoulder-driven melee anticipation/contact/recovery, empty hands; row 4: spell-casting preparation/active/recovery, empty hands. Separate arms, body and cape, do not add or paint any fixed weapons, no rectangular card backgrounds. Use consistent silhouette, real metal/leather textures, clean transparency and exact shared anchor points. The final deliverable must be an evenly spaced indexed sprite sheet with no background, labels or borders.

## Remaining gaps (DO NOT mark complete)
- Current high-detail vector art is scalable at high resolution, **but not a fully illustrated hand-painted 4-directional 60-frame motion library**. Some transitions are still procedural. Realism is improved but cannot be certified by green CI or a generated concept illustration.
- World environments still rely primarily on the prior 10× procedural terrain atlas with a new lighting/weather layer; full unique high-resolution landscape art and separately animated architecture are not shipped.
- Enemy and boss character atlas needs comparable overhaul (bespoke hit, death, attack anticipation, directional states).
- Professionally animated six weapon sprites and bespoke attacks need true new per-frame authored cutouts. Existing 6 weapon models remain transparent Canvas geometry.
- No physical device FPS, Vulkan/GPU benchmark or extended human playtest in this phase.

## Acceptance gates
- New rig shows **no fixed weapon**, and selected six items are independent.
- Idle/running/attack/cast/hurt are visibly different at 1280×720, not just metadata state changes.
- Ten biomes are visibly and dynamically distinct, without hiding enemies/HUD or blocking touch.
- Android :app:testDebugUnitTest + :app:assembleDebug, screenshot gallery artifacts, installable debug APK.
- Physical phone playtest and high-resolution art review required before changing overall status from Partial to Achieved.
