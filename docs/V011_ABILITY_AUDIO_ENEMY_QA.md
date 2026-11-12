# RiftGuard v0.11.0 — Skills, Enemy Behavior, Fortress Damage, Offline Sound

## Actual playable changes
- **Six independent unlockable skills** in Home → Armory → Equip Combat Abilities:
  - Rank 1: **Arc Pulse** damages and knocks enemies back (original behavior).
  - Rank 2: **Ember Cleave** hits a forward cone, with 3.5s burning damage over time.
  - Rank 3: **Frost Nova** hurts and slows enemies for 3.8 seconds.
  - Rank 3: **Healing Banner** is a placed object lasting 10 seconds. Heals nearby hero and castle when close enough.
  - Rank 4: **Spike Trap** places a persistent object ahead of the hero; enemies detonate it and receive damage and slow.
  - Rank 5: **Meteor Strike** marks a real world-space impact point, then explodes after a visible delay.
- The chosen skill is saved to SharedPreferences, reloads across launches, and is unavailable until its fortress rank is unlocked. Pulse capacitor upgrades reduce skill cooldowns. The existing HUD slot shows the selected skill and cooldown.
- Distinct in-game geometric animations for the skill classes replace the one-size-fits-all pulse ring. Active field props and impact zones exist in battle simulation, not as static pictures.
- **Seven original synthesized short combat sounds** for projectile, blade, spell, impact, construction, victory, and hero damage. The game generates PCM audio locally and uses SoundPool. Home has a persistent SOUND ON/OFF toggle. Audio subsystem failure is nonfatal. No internet/audio permission or downloaded music is required.
- **Fortress damage layers:** core-health fraction drives structural cracks, broken battlements, smoke and ember intensity. Restoration of HP naturally removes damage layers. Rank-1 wood and rank-3+ stone have distinct visual damage silhouettes.
- **New AI actions**: boars warn before a burst-speed charge with collision damage; shamans cast simulated, dodgeable magic bolts toward the hero's previous position, with a visual projectile and hit reaction.

## Validation
- Android JVM tests verify real game mutations for six skills, rank unlocking, persistence, healing, burning, frost slow, trap detonation, meteor delay, ability cooldowns and locked skill rejection.
- Enemy tests verify boar telegraph / dash state, shaman bolt creation and collision.
- All 7 WAV files have unit tests for RIFF headers, sample rate, nonzero audible PCM and distinct content; sound/mute setting persists.
- Robolectric pixel tests verify the structure physically changes appearance at low core health.
- Gallery captures ability loadout, all six casts, and a low-health fortress, in addition to prior gameplay screenshots.

## Not yet production-complete
- **Realism:** original character atlas still needs properly authored weapon-free multi-directional animation sheets; existing composite art remains limited. Six clean hand-painted held weapon sprites are not yet shipped.
- **Maps:** 300 stage IDs and 10 biome texture groups are procedural; no 300 fully hand-art-directed 2.5D mission worlds. More bespoke terrain art and interactive background depth are required.
- **Enemies:** 7 visual groups have additional behavior, but not enough distinct HD monsters, boss silhouettes or animator-authored attacks for AAA-quality combat.
- **Fortress:** cracks, banners, tower upgrades, ranks are functional, but detailed NPC construction workers and authored destruction frames are not shipped.
- **Audio:** distinct offline effects exist, but there is no full layered ambient soundtrack or authored soundtrack yet.
- **Mobile:** CI screen gallery and Robolectric are not substitutes for on-device FPS, touchscreen accessibility, save migrations, performance, or a complete 300-stage human playtest.

No claim of photorealism, a finished premium game, full HD art pack, or 300 individually illustrated levels.
