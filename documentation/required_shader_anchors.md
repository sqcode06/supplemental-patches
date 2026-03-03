# Required Shader Anchors (Complementary + Euphoria)

Sky and atmospherics injection now uses stable string anchors with strict count checks.
Every required anchor below **must exist exactly once** in the target shader files.

## Supported versions

- Complementary + Euphoria r5.5
- Complementary + Euphoria r5.6

## Required anchors by file

### `/shaders/program/deferred1.glsl`

- `#if defined END && defined END_STARS`
- `color.rgb += nightNebula;`
- `color.rgb = netherColor * (1.0 - maxBlindnessDarkness);`
- `color.rgb = endSkyColor;`

### `/shaders/lib/materials/materialMethods/reflections.glsl`

- `#ifdef ATM_COLOR_MULTS`

### `/shaders/lib/materials/materialMethods/reflectionBackground.glsl`

- `skyReflection += (DrawOverworldBeams(RVdotU, playerPos, viewPos) * 0.4 + 0.6).rgb * 0.08;`
- `vec3 skyReflection = endSkyColor * shadowMult;`

## Compatibility fixtures

Fixture snippets are committed under `src/main/resources/shader_anchors/` for r5.5 and r5.6.
`generateSkies` validates each fixture at runtime before patching shaders.
If a future Complementary/Euphoria release changes anchors, update these fixture files and the anchor constants in `Sky.kt`.
