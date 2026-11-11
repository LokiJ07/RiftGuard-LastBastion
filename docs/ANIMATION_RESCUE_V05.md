# V0.5 Animation & Battlefield Rescue

## Built
- The previous single static Warden card is replaced with a real 4x4 pose atlas: 4 idle, 4 run, 4 attack, 4 spell frames.
- The seven enemy slots use an animated frame atlas with distinct locomotion and hit/action frames. Flying enemies continue using their separate animated wing renderer.
- Six weapon images are separate from the character's arm and rotate around a hand pivot. Sword slashes have visible curved impact trails, while ranged/magic weapon attacks display glows.
- 10 biome-specific ground textures × 4 textures per biome are bundled and composed once per mission as a performance-friendly cached world layer.
- Live castle, tower, enemy, projectile, character, combat particles and mobile HUD remain separate scene layers.
- Robolectric tests render actual Android Canvas output and compare distinct animation frame pixels; CI produces screenshots of running and attacking in addition to the three stage maps.

## Visual honesty / asset limitations
These sprites are **pose variations reconstructed from original generated concept imagery**, not hand-drawn skeletal frame-by-frame 3D animation. The source artwork's detail is still limited, and the 300 hand-authored maps and >1,000 bespoke game-ready assets remain unfinished. CI proves rendering and logic, not subjective quality or physical-device framerate. Additional source-directed animation artwork, separate limbs and polished textured structures would be needed to reach the cinematic examples.

## Test route
On phone: enter Level 1-A, move in every direction, hold attack using Warden Crossbow or equip Iron Greatsword/Ember Sabre, compare the run and attack cycles, observe frame-based monster walk and strike states, check textural environment across Map A/B/C.
