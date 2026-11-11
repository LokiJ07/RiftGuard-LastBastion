# RiftGuard V0.8.1 responsive-control QA
The original battle HUD used fixed x coordinates irrespective of actual device width. Rendering in centered 16:9 mode on an ultra-wide display made joysticks and attack buttons sit too far inward from the physical edges.

The HUD now computes a bounded side-bleed from view dimensions and shifts four action controls toward both screen edges. The touch handler uses exactly the same coordinates, including multi-touch joystick updates. 16:9 remains backward compatible. The original non-stretched artwork and safe palette gutter backgrounds remain.

Robolectric tests cover a 1780×720 viewport, actual input coordinates, held/moving controls, build and pulse, plus 16:9 fallback. Still not a fully responsive rebuilt native UI.
