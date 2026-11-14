# RiftGuard v0.79 — Warden Oaths (progression milestone pass)

This branch stacks on `feature/v078-warden-level-100` (draft PR #38).
**Not merged to main.** The original level 1–100 XP, point economy, character
upgrade screen, fortress upgrades, weapon mastery and 300-stage campaign stay
unchanged.

## Automatic unlocks (no new currency, save migration, or claims)

| Warden level | Oath | Actual in-battle benefit |
|---|---|---|
| 10 | Guardian Heart | +12 max Warden HP |
| 25 | Rift Siphon | +6 Warden HP every 8 credited enemy kills when alive and injured (capped at max HP) |
| 40 | Battle Focus | +4% Warden attack; animated subtle Warden aura |
| 60 | Arc Conduit | -0.20 seconds skill cooldown, subject to existing 3-second floor |
| 80 | Vanguard Stride | +4% Warden movement speed |
| 100 | Rift Sovereign | Further +6% Warden attack (10% total oath attack) and enhanced aura |

- Bonuses are modest, additive to **only the Warden's** existing purchased
  attributes. Fortress turrets, enemies, difficulty and supply economy are not
  modified. In particular, no oath buys, replaces, or circumvents any weapon.
- Rift Siphon triggers once on every eighth globally credited enemy death;
  it does not trigger per shot/attack, when down, on a loss, or at full HP.
  Heals are explicitly clamped to `heroMaxHp()`.
- All six oaths derive from current `WardenLevelsV078.level()`; older saves
  instantly receive the oaths already earned under the existing migration.
  No new persisted keys or wallet to corrupt/duplicate.
- Existing maximum Level 100, 402 lifetime spendable attribute points and
  XP curve remain exactly as in v0.78.

## UI and animation

- The character sheet adds a next-oath preview and current-oath explanation
  without moving the shared buy buttons or their touch rectangles.
- On a win that crosses an oath threshold, the victory screen shows the
  newly unlocked oath and the actual benefit.
- LV40 gives the live Warden a restrained animated ring; LV100 upgrades it.
  This is additive over real equipped Warden armor / weapon artwork, not a
  replacement sprite. The permanent healing pulse uses existing VFX.

## QA targets

- XP, first-clear and replay balance tests from v0.78 must remain green.
- New `WardenMilestonesV079Test` checks every level boundary, all six
  thresholds, all stat bonuses and kill-heal cadence over 300 credited kills.
- Run Android unit tests, release build and emulator screenshot QA on this
  exact branch; also verify a real physical-device playtest for pacing,
  controls and UI legibility before any production release.
- Potential later iteration: player choice between milestone perks, more
  boss-specific interactions, and skills with bespoke animations, provided
  they pass performance and fairness QA. Those are **not** in this patch.
