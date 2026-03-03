void reflectBackgroundV56() {
    skyReflection += (DrawOverworldBeams(RVdotU, playerPos, viewPos) * 0.4 + 0.6).rgb * 0.08;
    vec3 skyReflection = endSkyColor * shadowMult;
}
