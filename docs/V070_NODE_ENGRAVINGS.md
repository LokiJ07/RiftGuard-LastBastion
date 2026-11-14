# RiftGuard v0.70 — Per-Node Weapon Evolution (development)

Stacks on v0.69. Each of six weapons gains visible, physical and subtly animated POWER/TECHNIQUE/SPECIAL engravings for every purchased permanent mastery node, beyond the five major silhouettes. These are glove-anchored on the real animated Warden and shared across Home, Armory, upgrade tree and cinematic scenes.

Exact art derives from the existing weapon mastery saved bitset. Previewing a locked node changes only the preview; no new save flags or hidden purchases. Idle animation caches include actual branch mask in their key (long key to avoid collisions), still capped at 64 frames.

Updated owned SPECIAL node tooltip describes the previously implemented real slow/ignite effect. This pass does not change combat hitboxes, wave counts, fortress weapons, economy or the ten realm environments.

Version 0.70.0-alpha1 / code 67. Requires exact-head Android CI, Android 10/16 emulators, art inspection and physical device testing; this is not a production release.