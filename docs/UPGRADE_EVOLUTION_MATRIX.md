# RiftGuard: Upgrade Evolution — Verified Asset and Change Matrix

**Build reference:** `v0.51.0-rc1`, commit `1ad29fab068fabe835643dc8ef9a4b61d8d6cf09`.  
**Scope:** This document describes the actual native Java renderer and save-backed unlock rules. An item labeled **next polish** is a proposed improvement, not a released feature.

## A. Fortress architecture (five real rank-specific silhouettes)

| Fortress rank | Existing visible form | Major physical differences | Purchase effect | Next polish target |
|---|---|---|---|---|
| 1 — Starter outpost | Timber palisade | Wooden wall perimeter, scaffolding, narrow watch posts, fabric/banner accent | Base fortress, basic construction | More timber variation, readable gate animation |
| 2 — Reinforced garrison | Stone-supported stronghold | Thicker towers, fortified gateway, side buttresses and reinforced battlements | Existing rank-stat upgrades | Distinct masonry joints and working drawbridge effect |
| 3 — Rune stronghold | Stone/iron military fort | Stronger corner bastions, heavier structure, visible rune/weapon staging | New high-rank tower-node thresholds become eligible | Animated furnace/forge and distinct mounted launch platform |
| 4 — Royal bastion | Tall broad citadel | Taller front towers, multi-level battlements, grand central tower roof | Higher-rank permanent defenses possible | Lighting/flag motion and full structure transition sequence |
| 5 — Arc citadel | Tall magical final castle | Crystal spires, illuminated high roof, large prism-facing weapon silhouette | Final fortress equipment rank gate | Bigger transformation reveal and bespoke boss-siege damage states |

**Actual renderer:** `NativeDeployedMapArt.fort()`, `FortressStructures.rankStructures()`, `FortressSilhouetteArt.draw()`, `FortressEvolutionArt.draw()` and `FortressWeaponShowcaseArt.draw()`. The same physical fortress family is rendered in battle and workshop; environmental props, collision, actual 5–8 tower pads and enemy routes remain separate.

## B. Fortress weapons — purchases, not free rank-only unlocks

The fortress **rank alone does not automatically grant the next arsenal**. The player must own enough **TURRET POWER** Ascension nodes and satisfy the fortress rank gate. `EvolutionProgress.fortressArsenal()` is the source of truth.

| Arsenal form | Mounted weapon | Minimum fortress rank | Minimum purchased TURRET nodes | Actual look | Gameplay |
|---|---|---:|---:|---|---|
| 0 | Timber Ballista (baseline) | 1 | 0 | No extra phantom arsenal mount | Normal existing pad/tower weapons; extra fortress armament is inactive |
| 1 | Ironbolt Battery | 2 | 1 | Two iron-framed magazine/bolt launchers | Independently fires projectile attacks, cooldown/range/damage scale by arsenal |
| 2 | Ember Mortar | 3 | 2 | Paired forged mortar barrels with glowing mouths | Real targeted projectiles with Ember-specific combat/VFX |
| 3 | Storm Conductor | 4 | 3 | Tall central multi-prong induction mast + side equipment | Real ranged storm/arc attack |
| 4 | Guardian Prism | 5 | 4 | Larger prism/crystal optical turret mounted over the final keep | High-tier real ranged attack with its defined effects |

All armament shots must originate from **the visible weapon muzzle**: `FortressArmamentRules.muzzleX/Y()`. The live `RiftGameView` combat simulation handles actual shot entities and effects. An unowned arsenal must not appear as a fake, purchasable gun.

**Purchase feedback:** Upgrade banner + offline sound; Workshop **PREVIEW NEXT** displays a potential future shape without modifying rank, purchased node bits or supplies; the actual **UPGRADE FORT** button performs a real purchase.

## C. Six permanent weapon families × five visible forms

These names and form milestones come from `EvolutionProgress.formName(weapon, tier)`. They depend on the highest **owned** mastery node tier; preview selection never changes the real equipped form.

| Family / weapon ID | Form 0 (Origin) | Form 1 (tier 1) | Form 2 (tier 2) | Form 3 (tier 3) | Form 4 (mastery) |
|---|---|---|---|---|---|
| 0 — Bow/Crossbow | Scout Recurve | Ranger Bow | Warhorn Crossbow | Phantom Arc Bow | Eclipse Longbow |
| 1 — Greatsword | Iron Sword | Steel Greatsword | Oathbreaker Blade | Royal Cleaver | Titan Ruinblade |
| 2 — Ember blade | Ember Saber | Molten Falchion | Flamebrand | Phoenix Talon | Inferno Sovereign |
| 3 — Arcane staff | Oak Channeler | Runed Staff | Arc Scepter | Storm Spire | Tempest Crown |
| 4 — Repeater bow | Scout Repeater | Double Rail | Siege Arbalest | Drum Battery | Thunder Repeater |
| 5 — Frost lance | Frost Spear | Glacier Pike | Crystal Lance | Winter Trident | Absolute-Zero Lance |

**Physical evolution rules already in source:** `EvolvedWeaponArt.draw()` and `WeaponSilhouettePolish.draw()` change blade widths/guards/teeth, bow arms/cams/magazines, staff heads/crystal forks, and lance tips. The same earned form is visible in the Armory, node-preview UI and the Warden's moving held-weapon transform.

**Weapon mastery tree:** 11 nodes per weapon, 66 total. Tier 0 Origin is owned by default. Tier 1–3 use three branching lanes (Power, Technique/Cadence and Special). To purchase the final tier-4 mastery node, the player must **own all three tier-3 nodes** (IDs 7, 8, 9); fortress rank and supplies also apply. Save key: `weapon.mastery.<weapon-id>.v1`.

## D. All Ascension categories have a visible payoff

| Tree category | Saved unlock visualization | Combat/stat anchor |
|---|---|---|
| WEAPON POWER / ATTACK | Extra weapon / Warden combat accent and armor contribution | `AscensionTree.attackMultiplier()` |
| CORE INTEGRITY / CORE | Fortress core structural/energy visual | `AscensionTree.coreHp()` |
| TURRET POWER / TOWER | **Physical new fortress arsenal** — Ironbolt, Ember, Storm, Guardian | `AscensionTree.towerMultiplier()` and `EvolutionProgress.fortressArsenal()` |
| WARDEN VITALITY / HERO | Warden armor/helmet/shoulder progression | `AscensionTree.heroHp()` and `EvolutionProgress.armorForm()` |
| STARTING GOLD / GOLD | Treasury/supply visual progression | `AscensionTree.startingGold()` |
| SPELL RECHARGE / SKILL | Evolving cast effect/sigil | `AscensionTree.spellCooldownReduction()` and `EvolutionProgress.skillForm()` |
| MOVEMENT SPEED / SPEED | Warden mobility appearance accents | `AscensionTree.moveMultiplier()` |

**Ascension tree:** 40 real nodes, with parent prerequisites, fortress-rank gates, costs and `ascension.purchased.v1` saved ownership. Appearance must always derive from purchased bits, not hovering or previewing. Existing saved data stays compatible after installing an updated APK using the same Android app identity/signature.

## E. Upgrade UI contract (must remain consistent)

1. Title and rank/current form are always readable without spilling beyond screen bounds.
2. **Current vs Next** live preview must show the actual native geometry and label whether the next look is LOCKED or affordable.
3. **PREVIEW NEXT** never deducts supplies, changes real equipped items or alters saved stats.
4. **UPGRADE / UNLOCK** is a separate, visible action with the same bottom-row touch target placement across Fortress, Mastery and Ascension screens.
5. Purchase confirmation shows the newly owned form, associated functional ability (if any) and a short offline sound/reveal.
6. Closing/reopening the game must restore the same visual forms from existing `SharedPreferences`; avoid generating new duplicated cosmetic flags.
7. World-map and mission preview screens must remain synchronized with the active stage, region and realm art.

## F. QA evidence and remaining gates

- **Android CI #225 (main):** succeeded for commit `1ad29fab`.
- **Downloaded JUnit reports:** 206 tests passed, 0 failed, 0 errors, 0 skipped.
- **Android emulator QA #16 (feature branch):** succeeded and saved workshop, mastery, ascension, battle and **PREVIEW NEXT** screenshots; no detected application crash.
- **Robolectric visual artifacts:** images `100-v051-fortress-rank-{1..5}`, `101-v051-fortress-arsenal-{0..4}`, `102-v051-weapon-{0..5}-form-{0..4}`, `103-v051-workshop-current`, `104-v051-workshop-next-preview`, and `105-v051-workshop-upgrade-reveal`.
- **Emulator metrics (NOT physical-phone FPS):** 120 sampled frames, median 132.65 ms and p95 173.38 ms on virtual Android API 29 main build. This indicates a graphics performance issue on that emulator, not a proven physical phone frame rate.
- **Still needed:** real Android hardware tests (input, FPS, thermals, memory), optional high-resolution hand-crafted artwork, more prominent purchase reveal animation and performance optimization.

### Release acceptance matrix

| Area | Evidence required | v0.51 checkpoint |
|---|---|---|
| 5 fortress rank models | Different real screenshot hashes/rendered shape | Verified |
| 5 fortress arsenal forms | Different mounted structures and muzzle origin | Verified in tests |
| 30 weapon appearances | Six families with five unique actual renderings | Verified |
| Purchases vs preview | No supply changes on preview; purchase changes save and form | Verified |
| Save persistence | Same purchased-bit-based forms after reload | Automated regression coverage |
| Android build & emulator | Green Actions plus screenshot captures | Passed for tested runs |
| Real physical-device performance | Measured on attached Android handset | **Not yet verified** |

**Do not introduce a parallel cosmetic system** when working on the next update: extend `EvolutionProgress` and the existing renderers instead.
