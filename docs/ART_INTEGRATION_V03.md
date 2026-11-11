# RiftGuard — First AI-Painted Artwork Integration (V0.3)

## What is now **real and in the APK**
- `app/src/main/res/drawable-nodpi/riftguard_atlas.webp`: original AI-generated game fantasy artwork, resized/packed from freshly generated concept art; no search-result screenshots or third-party game art copied into the APK.
- `PaintedArt.java`: decodes the asset once, displays the map, hero-home splash illustration, fortress illustration, and three painted map A/B/C cards.
- `RiftGameView.java`: world map uses actual paintings behind ten **working** region hitboxes. Level- and map-selection logic is not baked into any art; attack movement and monsters still run over active Canvas rendering.
- Battle environments include a lightly blended illustrated atmosphere from the new art; playable entities remain drawn on top.

## Limitations and next asset pipeline
The **current packed atlas is 1024 × 768 pixels** (original AI-generated artwork resampled into one original WebP texture). This is significantly sharper than the first preview build. The full native-resolution concept sources remain available separately; the APK currently uses a 1024 × 768 art atlas.

The original concept art includes UI and characters as part of the illustration, so those pixels are not functional game objects. Further production should generate UI-free, character-free terrain tiles and separate character/tower/monster sprite atlases. Each must be separately integrated, animated, performance-tested and validated in the running APK before claiming completion.

## 3 maps × 100 levels
Map A/B/C thumbnails now use separate integrated images. The 300 stage indices remain available and procedural battleground configurations use different roads and landmarks. They are not yet 300 hand-built high-detail map scenes.

## Acceptance tests
- Verify `PaintedArt.available()` in Android runtime smoke tests.
- Tap a region marker -> selected 10-level region changes.
- Select A/B/C -> mission variant changes; locked missions cannot deploy.
- GitHub CI captures PNG screenshots for the home, atlas, armory, fortress and battle screens.

## V0.4.1 battle-art corrections
- The earlier low-opacity gameplay screenshot overlay was removed because it contained baked characters and HUD that were **not interactive**.
- Map A has a visibly wooden rank-1 / reinforced rank-2 scout outpost.
- Map B has a river approach and bridge-defense landmarks.
- Map C uses the siege fortress with an animated corrupted portal, banners and scorched ground.
- The Warden and multiple monster classes use actual separately rendered character sprites from `riftguard_characters.webp`.
- CI captures seven real Android-view screenshots, including all three combat map variants.

This is playable original production groundwork. Distinct high-resolution terrain tiles, fully rigged animations, and hundreds of authored levels remain incomplete.
