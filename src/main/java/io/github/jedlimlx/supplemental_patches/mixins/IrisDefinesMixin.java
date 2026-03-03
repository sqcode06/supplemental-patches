package io.github.jedlimlx.supplemental_patches.mixins;


import io.github.jedlimlx.supplemental_patches.shaders.BiomeUniformsKt;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.IrisDefines;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

@Mixin(IrisDefines.class)
public class IrisDefinesMixin {
    private static final boolean EMIT_LEGACY_BIOME_DEFINES = true;
    private static final Pattern NON_ALNUM_PATTERN = Pattern.compile("[^A-Z0-9]");

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
            (biome, id) -> {
                String namespace = sanitizeDefinePart(biome.location().getNamespace());
                String path = sanitizeDefinePart(biome.location().getPath());
                String value = String.valueOf(id);

                lst.add(new StringPair("MOD_BIOME_" + namespace + "_" + path, value));

                if (EMIT_LEGACY_BIOME_DEFINES) {
                    lst.add(new StringPair("MOD_BIOME_" + biome.location().getPath().toUpperCase(Locale.ROOT), value));
                }
            }
        );

        return lst;
    }

    private static String sanitizeDefinePart(String part) {
        return NON_ALNUM_PATTERN.matcher(part.toUpperCase(Locale.ROOT)).replaceAll("_");
    }
}
