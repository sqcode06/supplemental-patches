package io.github.jedlimlx.supplemental_patches.shaders

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

val _dimensionMap: Object2IntOpenHashMap<ResourceKey<Level>> = Object2IntOpenHashMap<ResourceKey<Level>>().apply {
    defaultReturnValue(-1)
}

fun getDimensionMap(): Object2IntOpenHashMap<ResourceKey<Level>> {
    _dimensionMap.clear()

    val connection: ClientPacketListener? = Minecraft.getInstance()?.connection
    if (connection != null) {
        val levelStems = connection.registryAccess().registry(Registries.LEVEL_STEM).orElse(null)

        if (levelStems != null) {
            var currentId = 0
            levelStems.entrySet().sortedWith(
                compareBy({ it.key.location().path }, { it.key.location().namespace })
            ).forEach { stemEntry ->
                val levelKey = ResourceKey.create(Registries.DIMENSION, stemEntry.key.location())
                _dimensionMap[levelKey] = currentId++
            }
        }
    }

    return _dimensionMap
}
