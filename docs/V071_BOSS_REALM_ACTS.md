# RiftGuard v0.71 — Ten Realm Bosses + Campaign Acts (development)

**Scope:** 10 world-boss encounters at levels 10C, 20C, ..., 100C. Normal elites and the separate Eclipse Cave confrontation retain their older behavior and resources. Keep all actor/realm/fortress art and save keys intact.

## Real new boss actions
| Realm | Readable attack | Player counter |
|---|---|---|
| Greenhaven | Bramble crush circle at boss | Exit the disk; break WAR HORN |
| Whispering Woods | Locked root trap at prior hero position | Move outside the root mark |
| Venom Marsh | Locked venom pool + limited boss sustain | Exit poison; break DEEP CORE to prevent healing |
| Ember Canyon | Mortar disk at marked tower or keep | Dodge target, repair tower |
| Frostpeak | Straight narrow frost beam | Sidestep beam; avoid slow |
| Ironfall | Heavier aimed cannon salvo at built tower | Defend/repair selected turret, break shoulder cannon |
| Crystal Depths | Hollow annular prism ring + limited boss sustain | Stand within safe center or outside ring; break CORE |
| Stormbound Isles | Locked lightning marker | Leave marked location |
| Shadowfall | Beam plus brief skill suppression on hit | Dodge line, preserve skill timing |
| Eclipse Citadel | Wide eclipse nova | Escape area; break corrupted sword |

Every boss strikes using the same geometry shown by the world-space telegraph. Target locks once per charge so the warning cannot chase the player on the final frame. Breakable WEAPON cuts siege/hero strength to 48%; breaking CORE disables reinforcement and regen. Three visible acts (Discovery, Escalation, Final Siege) divide each realm 9/12/9 stages and change wave-two signature enemy tactical roles in realms 2–10, not the number of enemies.

Target QA: 300 stages act scan, 10 ability geometry tests, live real-hit + dodge + tower + sustain checks, rendered telegraph checks, Android 10/16 emulators, old save recovery and player telegraph comprehension. These tests do not prove long-term fun; blind first-run and boss playtests still needed.

**Not for commercial release:** draft stacked on v0.70 PR #30. The signed Play AAB, licensing, cross-device sustained FPS and retention certification are outstanding.