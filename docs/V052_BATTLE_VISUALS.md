# RiftGuard V0.5.2 — Live battle environment update

This iteration improves **gameplay rendering**, not just the title or campaign art.

### Implemented
- Generated 60 stand-alone transparent prop sprites in 10 biome-specific palettes.
- 6 per biome: leafy tree, alternate tree, rock, flowering bush, stone ruin and glowing crystal.
- Bundled optimized 1008×1960 WebP atlas, available offline from Android resources.
- Independent sway animation for trees and near-player alpha fading for obstructing props.
- Correct painter ordering based on world feet positions across hero, enemies, towers, fortress, pickups and props.
- Closer 1.14× battlefield framing without enlarging the HUD.
- Character attack/run/cast sprite states and real separate weapon animation from existing V0.5.1 preserved.
- Added automated Android tests for packed sprite rendering and occlusion rules.

### Quality limitation
This pass does **not** turn the app into a fully rendered 3D/isometric title. Props are procedural painted-style 2D textures, not high-fidelity AI renders or skeletal 3D character rigs. Generated backgrounds used for campaign UI are **not** gameplay objects. Do not mistake Green CI for a physical phone playtest. Every battle screenshot from the actual Android renderer must be reviewed before release.

### Next production goals
- Rigged swordless hero source with separate torso/limbs/cape and weapon socket.
- Proper frame animation per enemy with hurt/death and attack telegraphs.
- Hand-authored, textured architecture for every base tier, maps A/B/C and boss encounter.
- Event/quest/shop buttons only after real mechanics and progression backing exist.
