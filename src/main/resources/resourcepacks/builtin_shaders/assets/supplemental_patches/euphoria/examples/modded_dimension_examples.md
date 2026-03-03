# Modded Dimension Shader Gating Examples

Use `moddedDimension` as a runtime integer uniform and `MOD_DIMENSION_*` as compile-time defines.

## Runtime uniform example

```glsl
// Runtime check (works even when multiple dimensions are bundled in one shader compile)
if (moddedDimension == MOD_DIMENSION_MINECRAFT_THE_NETHER) {
    cloudDensity *= 0.4;
}
```

## Compile-time define example

```glsl
#if defined MOD_DIMENSION_MINECRAFT_THE_END
    // Enable End-specific sky branch
    vec3 skyTint = vec3(0.35, 0.45, 0.9);
#endif
```

## Combined guard example

```glsl
#if defined MOD_DIMENSION_SCORCHFUL_ASH_BARRENS
if (moddedDimension == MOD_DIMENSION_SCORCHFUL_ASH_BARRENS) {
    // Reduce cloud rendering in this custom dimension
    cloudAlpha *= 0.2;
}
#endif
```

## Notes

- When no world / connection is present, `moddedDimension` falls back to `-1`.
- Dimension defines are emitted from `namespace:path` and normalized to uppercase with non-alphanumeric characters converted to underscores.
  - Example: `my-mod:sky/realm` -> `MOD_DIMENSION_MY_MOD_SKY_REALM`
