# V0.10.0 — Mission Briefings, Equipment Cleanup and Melee Contact

## Scope of this phase
- Author 10 world-specific chapter texts, 3 A/B/C mission orders and warning types, and computed stage objectives for 300 campaign stages.
- Show a true interactive illustrated mission briefing before deployment, with mission artwork, objective, and an explicit **BEGIN BATTLE** button.
- Freeze battle updates and block combat control hitboxes while the briefing is visible; Back returns to the atlas.
- Render the hero as painted layers plus independent in-hand weapon. The original lower-right sidearm crop is intentionally omitted from the live upper body. Correctly retained: face, chest armor, shoulder pauldron, illustrated greaves and cape. The separately animated right forearm and hand own equipment rendering.
- Sword and sabre aim through the same target-heading transform as combat checks. Sword damage occurs once when the blade passes its actual swing-contact window, with a forward cone rather than an immediate 360-degree radius.
- CI has tests for 300 story records, boss names, overlay taps, story screenshots, physical component render separation, blade direction, delayed hit, and exclusion of monsters behind the player.

## Still outstanding
- The original source hero sheets contain some baked-in equipment and do not provide artist-authored weapon-free source poses. This phase is **compositing-based cleanup**, not a newly authored HD character atlas. Inspect the real screenshot gallery and test a phone before claiming all duplicates are gone.
- Weapon silhouettes are transparent code-driven combat models; they are not fully painted, authored, per-frame weapon sprites.
- Ten regional map tiles and deterministic routes are still procedural. 300 manually illustrated stages, dungeons, cinematic narrative, authored audio, complex boss animations and full 2.5D background art are not shipped.
- A green Android CI is a compile/regression gate. It is not proof of polished visual quality, GPU FPS, physical touchscreen controls or complete feature acceptance.

## Acceptance checklist for future art pack
1. **Actual original weapon-free base sheet:** one centered character on transparent background, no embedded sword, shield, staff, bow or items anywhere on the body; multiple angles and animation states.
2. **Separate held weapon layers:** six transparent weapons sharing accurate grip/hilt and tip anchors; no rectangular icon badges or copied backdrops.
3. **Frame-linked VFX:** tip muzzle, recoil, blade trail and collision sparks stay fixed to hands at every animation frame and facing.
4. **Real mobile checks:** no ghost limbs, waist seams, visual duplicate equipment, off-screen UI or target-direction mismatches.
5. **Unique map atmosphere:** chapter-specific landscape features and handcrafted mission identities; path/props and enemy movement share coordinates.
