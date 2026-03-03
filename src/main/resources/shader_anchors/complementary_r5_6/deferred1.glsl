// Complementary r5.6-compatible anchor sample
#if defined END && defined END_STARS
    #include "/lib/atmospherics/enderStars.glsl"
#endif

void drawSkiesV56() {
    color.rgb += nightNebula;
    color.rgb = netherColor * (1.0 - maxBlindnessDarkness);
    color.rgb = endSkyColor;
}
