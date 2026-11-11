# V0.8.0 stage hazards — functional/visual acceptance
- 10 biome-specific, visually distinct effects: bramble, spectral mist, venom, magma, frost, rockfall, crystal, storm, shadow and eclipse.
- Each of the 300 stages seeds its own geometry from the real enemy lane, with map C and world boss fights producing more hazards.
- Hazards animate and change collision combat behavior: knock-on slow, poison/heat damage and enemy attrition.
- All special effects are rendered as separate live Canvas objects, not embedded in a screenshot or background.
- Tests validate all stage geometries, bramble movement penalties, poison player damage, and hazard screenshot rendering.

Quality limitation: these are original procedural effects and modest gameplay hazards, not authored high-detail 2.5D animated terrain set pieces. Further professional sprite/frame art is required for the reference quality.
