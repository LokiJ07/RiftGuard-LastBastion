# RiftGuard v0.78.0-alpha1 — Warden RPG Level & Attributes

**Character Level 1–100 is SEPARATE from the original 100 campaign levels /
300 stages**. Stacked draft from v0.77 PR #37; not merged to main.

## EXP economy and balance

Only a genuinely won campaign stage grants Warden XP. Normal first-clear
EXP is `115 + 4 × stageIndex + (map C ? 30 : 0)`. Difficulty multipliers:
Easy **0.78x**, Normal **1.0x**, Hard **1.25x**, Expert **1.55x**.
A replay pays only **22%** of its full clear EXP (rounded). A loss or
uncommitted save grants zero. Normal first-clear progression over all 300
stages yields ~216,900 EXP; reaching level 100 costs **215,985 EXP**.
Hard/Expert reach level 100 earlier, Easy requires a modest number of replays.

Every new level costs more EXP than the previous:
`round(155 + 14 × currentLevel + 0.40 × currentLevel²)`.
LV1→LV2 costs 169 XP; LV49→50 costs 1,801 XP;
LV99→LV100 costs 5,461 XP. XP stops at LV100 and cannot overflow.

## Level-up upgrade points

- Level 2–19: **+2 points per level**.
- Level 20–39: **+3 points per level**.
- Level 40–59: **+4 points per level**.
- Level 60–79: **+5 points per level**.
- Level 80–100: **+6 points per level**.

Lifetime total at LV100 = **402 attribute points**. Unspent balance is
computed from lifetime earned minus spent—never a second unverified wallet.
Allocate +1 or +5 at a time into four stats, max 120 per category:

| Attribute | Actual existing combat effect at 1 point | Max |
|---|---|---|
| Vitality | +2 permanent Warden max HP | +240 HP |
| Power | +0.16% Warden attacks | +19.2% damage |
| Agility | +0.09% Warden movement | +10.8% movement |
| Arcana | -0.015 sec Warden skill cooldown | -1.8 seconds |

Stats stack additively/multiplicatively with existing paid Fortress,
Weapon Mastery, Ascension and Kingdom upgrades. They do not alter turret
firepower, enemy spawn counts, defense rank, bought gear ownership or saved
weapon models. Individual category and level caps prevent unlimited farming.

## Save integrity and existing players

v0.78 EXP is persisted in the **SAME SharedPreferences transaction** as
campaign victory rewards, unlocked stage and removal of recovery checkpoint.
A failed write does not award EXP or show a successful victory. Existing
old saves are migrated once from already-cleared stage count with **Normal**
first-clear EXP; no loss of supplies, equipment, saves or existing upgrades.
On resume, level and unspent points are derived from saved total XP and stat
allocation. Character stats spend in their own durable single transaction.

## Native UI

- New fully illustrated **WARDEN LV** tile on HOME (the original Armory,
  Fortress, Campaign, Chronicle, Resources and Kingdom remain accessible).
- Character screen with same actual equipped animated Warden rig + armor
  and weapon, real EXP bar and full number readout, four selectable stat cards,
  remaining points, bonus summaries and +1 / +5 / Home / Armory actions.
- Battle top HUD displays Level, XP progress and unspent-points alert,
  outside draggable touch-control regions.
- Campaign map shows difficulty-specific EXP preview for the selected stage.
- Victory screen shows EXP gained, old/new level and actual newly awarded
  points, plus direct Warden stats button without replacing Next Stage.

## Verification

Pure tests audit all 99 XP thresholds, all 300 stages × 4 difficulties,
all 100 level point awards and stat caps. Robolectric verifies actual
last-wave victory, atomic EXP save and no repeat payout on outro, replays,
old-save migration, physical CHARACTER card+buttons, actual Warden HP effect,
and max-level corrupt-save clamping. Android 10/16 emulator QA additionally
captures a real Warden leveling screen.

Release status: development alpha, draft PR, no signed production release,
full physical-phone playtest / long campaign pacing still outstanding.
