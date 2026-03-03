#if defined END && defined END_STARS
    #include "/lib/atmospherics/enderStars.glsl"
#endif

void drawSkies() {
    color.rgb += nightNebula;
    color.rgb = netherColor * (1.0 - maxBlindnessDarkness);
    color.rgb = endSkyColor;
}
