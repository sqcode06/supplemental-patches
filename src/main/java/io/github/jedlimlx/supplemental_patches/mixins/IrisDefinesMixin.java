package io.github.jedlimlx.supplemental_patches.mixins;


import io.github.jedlimlx.supplemental_patches.shaders.BiomeUniformsKt;
import io.github.jedlimlx.supplemental_patches.shaders.DimensionUniformsKt;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.IrisDefines;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Locale;

@Mixin(IrisDefines.class)
public class IrisDefinesMixin {
    @ModifyVariable(
        method = "createIrisReplacements()Lcom/google/common/collect/ImmutableList;",
        at = @At("STORE"),
        ordinal = 0,
        remap = false
    )
    private static ArrayList<StringPair> createStandardEnvironmentDefines(ArrayList<StringPair> lst) {
        for (IModInfo mod : ModList.get().getMods()) {
            lst.add(new StringPair("MOD_" + mod.getModId().toUpperCase(), ""));
        }

        BiomeUniformsKt.getBiomeMap().forEach(
            (biome, id) -> lst.add(
                new StringPair(
                    "MOD_BIOME_" + biome.location().getPath().toUpperCase(Locale.ROOT),
                    String.valueOf(id)
                )
            )
        );

        DimensionUniformsKt.getDimensionMap().forEach(
            (dimension, id) -> lst.add(
                new StringPair(
                    "MOD_DIMENSION_" + normalizeDimensionKey(dimension.location()),
                    String.valueOf(id)
                )
            )
        );

        return lst;
    }

    private static String normalizeDimensionKey(ResourceLocation resourceLocation) {
        String normalized = (resourceLocation.getNamespace() + "_" + resourceLocation.getPath())
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]", "_")
            .replaceAll("_+", "_");

        if (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }

        if (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
