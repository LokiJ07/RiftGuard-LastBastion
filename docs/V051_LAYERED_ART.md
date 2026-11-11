# V0.5.1 — Layered Art Corrections

**Actual visual regression caught two issues after V0.5.0 CI was green:** textures had sharp rectangular seams and fortress walls still looked like flat blocks.

Fixed:
- 40 raster terrain tiles now use 62-pixel feathered alpha and overlap on a 160px grid, blended into a biome base layer once at stage creation. The complete gameplay floor is cached as one bitmap, no hard tile seams.
- A genuine isolated painted stone castle asset is now an independently rendered environment object for Fortress Rank 3–5, with dynamic lantern light, upgrade glow and battle damage smoke. Ranks 1–2 retain a wooden outpost renderer.
- No character, UI, enemy, controls or collision is part of these building/terrain textures; they remain independent interactive layers.

Acceptance: Screenshots of A/B/C from Robolectric, actual frame tests, gameplay tests, and version 0.5.1 Android APK artifact.
Limitations: The remaining wooden fortress, tower art, terrain props and enemy animations still require production-grade art refinement.
