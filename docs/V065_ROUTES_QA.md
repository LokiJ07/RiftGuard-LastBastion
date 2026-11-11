# V0.6.5 — Stage-defined lane geometry audit

## Concrete root cause fixed
Before this change, all 300 stage IDs used one of exactly three curves (A, B, C). Procedural props and color alone could not change the route. The enemies also followed two straight hidden waypoints which could diverge from the painted S-curve.

## New rendering and AI contract
- `LaneRules.stagePathY(stage, x)` is a single deterministic physical road per stage.
- Road renderer draws samples of that geometry (not a separate hand-coded curve).
- Spawn positions use `stageSpawnY`; spawned monsters use the same sampled stage geometry.
- Monsters follow route samples until nearing the castle, then attack the real castle perimeter.
- Decorative assets no longer spawn inside the route corridor.
- Robolectric saves screenshots of stages 1-A, 5-A and 27-A and tests geometry consistency across all 300 stage IDs.

## What this DOES NOT fix
No unique hand-painted terrain layout per level yet. Paths are procedurally different but still use the same original ground-asset family within each region. Natural skeletal walk cycles, boss visuals, real weapon rig sockets, interactive Events/Quests/Achievements/Shop and native device-responsive HUD are still missing. Current online pack still mirrors bundled low-resolution art.
