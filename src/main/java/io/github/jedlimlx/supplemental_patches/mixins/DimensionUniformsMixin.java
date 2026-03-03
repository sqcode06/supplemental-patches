package io.github.jedlimlx.supplemental_patches.mixins;

import io.github.jedlimlx.supplemental_patches.shaders.DimensionUniformsKt;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.uniforms.BiomeUniforms;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

@Mixin(value = BiomeUniforms.class, remap = false)
public class DimensionUniformsMixin {
    @Shadow
    static IntSupplier playerI(ToIntFunction<LocalPlayer> function) {
        return () -> -1;
    }

    @Inject(
        method = "addBiomeUniforms",
        at = @At("HEAD")
    )
    private static void addDimensionUniforms(UniformHolder uniforms, CallbackInfo ci) {
        uniforms.uniform1i(
            UniformUpdateFrequency.PER_TICK,
            "moddedDimension",
            playerI((player) -> {
                ResourceKey<Level> dimensionKey = player.level().dimension();
                if (dimensionKey == null) {
                    return -1;
                }

                return DimensionUniformsKt.get_dimensionMap().getInt(dimensionKey);
            })
        );
    }
}
