# RiftGuard v0.66 alpha5 — Balanced Siege Design

**Intent:** Progressively tougher, smarter opponents, not ever-increasing monster spam. Keep the existing ten-world illustrations, ambient animation, world assets, Warden sprites, full tower and weapon visual tiers, camera cinematics, and destructible terrain **exactly intact**.

## What changed

| Battle | Previous Normal wave size | New Normal wave size |
|---|---:|---:|
| Level 1-A, wave 1 | 6 | 6 |
| Level 1-A, wave 4 | 12 | 9 |
| Level 10-C, wave 6 | 21 | 13 |
| Level 30-C, wave 8 | 29 | 17 |
| Level 50-C, wave 9 | 35 | 20 |
| Level 70-C, wave 11 | 43 | 24 |
| Level 100-C, wave 12 | 52 | 28 |

Normal size caps at 28 foes per complete wave. Hard adds 3, Expert adds 5, with global cap 34. These are the **total queued enemies per wave**, not all simultaneously on-screen.

Simultaneously active ordinary enemies: Easy 13, Normal 16, Hard 19, Expert 22. Once full, spawns **wait** until an enemy dies (remaining wave members are *never skipped*). Boss reinforcements remain real, with their own existing summon limits. Existing level wave counts remain (4–12 Normal); Hard +1 and Expert +2 bonus waves rather than +2/+5, for more focused encounters.

Enemy HP still increases sharply with campaign level via the existing `BattleRules.enemyHealth(stage,type)`. A modest **+2% per additional wave, max +22%** adds late-wave tension. Nightmare difficulty is no longer a flat 3.85× HP multiplier: Easy .68×, Normal 1.0×, Hard 1.75×, Expert 2.55×, plus real boss abilities and phases. Hard/Expert still differ through damage, speed, elite encounters, incoming wave count and spawn gap.

Gold *per kill* increases in inverse proportion to the reduced wave population, preserving approximately the earlier wave-wide upgrade budget. This is battle gold only: permanent victory supplies, story progression, fortress purchasing, tower/weapon art and user saves retain the same semantics. Expert starting gold rises slightly from 70 to 85 to avoid early one-tower softlocks.

Existing beta checkpoints in old Hard/Expert waves 15–17 are migrated to the highest current final wave, retaining towers, HP and battle gold; fake/corrupted waves beyond the previous legal cap are rejected.

## Testing / release limits

- Automated audit checks all 300 stages × 4 difficulties, all 10 world bosses, last-wave boss placement, every wave's active cap, gold budgets and graphic-world signatures.
- Robolectric/CI and Android virtual-device QA are **not** real player playtests; actual HP-to-kill, fun, mission duration, loadout economy and real-device frame pacing still require a representative playtest.
- Full illustration, particle weather, boss lighting, character art, animations, backgrounds, tower meshes and UI are preserved unchanged. **The performance optimization must not be achieved by removing art.**
