# RiftGuard v0.15.0 — Animation & Battlefield Visual QA

## Scope (visual/game feel only)
This is an incremental production-rendering upgrade for Android, not an unrelated game's puzzles or upgrade menus.

- `AnimationBlend`: stable 4-frame transition control for the existing painterly Warden and 7 monster groups. Adjacent frames blend only during the final 22% of a frame, with a maximum 44% overlay. Attacks do not blend from the final frame back to the first.
- Player arms, weapon grips, projectile muzzle and combat collision stay on the original continuous weapon-specific IK rig, NOT duplicated in the frame blending pass.
- Species-specific animation curves: breathing displacement and anticipatory creature attack lunge; winged enemy keeps independent wing motion and next-pose blending.
- `BattlefieldLighting`: keep entrance light, physically located biome effects, soft grounded separate left/right Warden foot-contact shadows with gait-dependent weight transfer. Rendering remains behind HUD and above terrain, not a screen-space vignette obscuring combat.
- Visual review at 1280×720 found the existing Crossbow looked like a four-paddle spinner. `WorldArt` now draws an actual compact recurve silhouette, bowstring, bolt, and smaller Repeater feeder.
- `MotionArt` now masks and bends **genuine painted armor fragments along both IK arm segments** instead of two untextured flat rods; `WeaponAnimation.torsoShiftX/Y` and `WeaponRig` synchronize the torso lunge, shoulder, elbow, glove and physical muzzle during all six attack profiles.
- Screen gallery: `43-painted-sprite-transition-qa.png` uses 8 actual hero timestamps and the real rendered enemy classes; older gallery captures remain in CI to catch visual regressions.
- Automated Robolectric Native Graphics tests cover attack boundary/cycle math, actual pixel differences between hero and enemy frames, biomes across time and moving foot shadows.

## AI animation skill / generation transparency
The specific user-referenced **AI Animations skill** is not present in the installed skills catalog for this conversation. Higgsfield's current account balance reports **0 credits** and no active free trial. The available free Genjutsu generation type does not directly yield transparent 2D game sprite atlases. A separate local image-generation attempt returned a rendered promotional infographic, **not a transparent character atlas**; it is rejected and **is not an APK asset**. Therefore this phase cannot truthfully be reported as AI-generated HD character art.

## Still outstanding (do not mark complete)
- True newly painted, equipment-free, high-resolution multi-angle Warden with independent body and held-weapon frames.
- Fully illustrated HD sprites for every enemy, boss, attack, hit, death and movement direction.
- Rebuilt level art/2.5D architectural lighting and unique terrain per mission, rather than the existing 10-biome tile atlas.
- GPU hardware FPS profiling and physical touchscreen playtest with balanced complexity/pixel overdraw.
- The renderer still uses source painterly atlas frames and geometric weapon models. This version improves animation timing and world-light grounding, **not production-level realism**.

## Engineering gate
Do not merge unless latest branch-head GitHub Actions completes :app:testDebugUnitTest, :app:assembleDebug, and screenshot artifact creation. Review current CI screenshots for visible defects before claiming that art quality improved. No build result can certify realism by itself.
