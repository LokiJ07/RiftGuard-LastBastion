# V0.9.0 animation and VFX QA
Original issue: 4 running frames from an atlas were almost identical; full-image bob created a false walk. Now the illustrated hero and ground enemy torsos are layered over independent moving legs that have staggered knee/foot poses. The weapon is still a separate real atlas item, mounted at the active gauntlet. Two distinct run frames are captured for side-by-side review in generated Android screenshots. Flying enemy retains wings; knockback, damage, deaths, and projectiles have real timed effects. All logic remains offline, without external network dependencies.

Acceptance checks:
- alternating planted vs lifted feet,
- six weapon sockets move while running and remain finite during attack,
- at least eight distinct actual projectile silhouettes rendered,
- a defeated monster yields one kill, leaves a transient on-screen death echo and disappears automatically,
- full existing 300-stage pathing, shop, resource and UI regression tests.

Remaining: true painted frame-set of walking/attack/cast per creature, actual professionally authored 2.5D character art, 300 hand-painted maps, physical tablet and foldable touch tests.
