# RIFTGUARD: Last Bastion

A mobile-first, offline, 2D/2.5D fortress-defense action game for Android.

## Vision
Defend a living fortress against monster invasions, fight as a controllable hero, construct and upgrade turrets, and advance through 100 campaign levels with three stages per level (300 total).

## Status
**V0.1 playable foundation in development.** The campaign will begin with generated battlegrounds and expand into hand-directed unique art, bosses, animation systems and exploration over V1–V5. Asset and content targets are plans, not completed work.

## Build
Android Gradle Plugin 8.7.3 · Gradle 8.10.2 · JDK 17 · Android API 35.
Run `gradle :app:assembleDebug :app:testDebugUnitTest` after installing the Android SDK. GitHub Actions will build the APK on each push. The output will be available in workflow artifacts.

## Controls
Landscape mobile layout with a virtual joystick on the left and attack, pulse and build/upgrade controls on the right.

## Campaign model
10 themed worlds × 10 levels × 3 map stages. Levels are unlocked sequentially, and saved locally. Initial battlegrounds use deterministic procedural layouts, **not 300 hand-authored scenes**.

## License
Project code is not currently licensed for reuse; choose a license before external contributions.
