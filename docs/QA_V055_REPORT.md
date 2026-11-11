# RiftGuard v0.5.5 – QA repairs and real-layered campaign UI

## Confirmed repairs
- Removed fake UI labels/buttons that were baked into Home, Atlas and Fortress background screenshots.
- Home + Fortress now draw a living, independently animated castle/wood outpost, hero, creature and light particles over clean world art.
- The World Atlas is drawn from individual biomes, roads, water, nodes and fog layers with independent interactive hitboxes.
- One-route combat: monsters enter from screen-right, castle stays visible on the left, hero moves within a defined frontline zone.
- Map A/B/C each has a different approach geometry and visible road.
- Added deterministic stage- and map-wide collision/spawn/interaction tests, plus Android runtime tests.
- Fixed projectile overshoot and waypoint stand-off stopping early.
- Updated the Android screenshot capture suite to place test monsters on the right as in real gameplay.

## Tests / limitations
- Android Gradle CI compiles the APK and runs JVM/Robolectric graphics/gameplay tests.
- Offline base art, optional downloaded-art manifest verification, saving, armory unlock/equip, waves and combat remain verified by tests.
- Device-specific FPS, touch latency, and 300 individually authored level visuals are still not proven by these tests.
- Events, quests, achievements, daily rewards and shop are **not yet functional game modules**; a mock button/image is not counted as implementation.
- Replacing fake screenshots does not deliver full cinematic 3D animation or 1000+ bespoke art assets; those remain content-production milestones.
