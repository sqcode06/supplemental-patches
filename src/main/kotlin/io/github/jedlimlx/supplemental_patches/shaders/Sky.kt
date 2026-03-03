package io.github.jedlimlx.supplemental_patches.shaders

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

val SKIES = mutableListOf<Sky>()

data class Sky(
    val name: String,
    val code: String,
    val dimension: String,
    val deferred: String,
    val deferredTarget: String,
    val reflection: String,
    val reflectionTarget: String,
    val conditions: List<String>
)

const val DEFERRED_PATH = "/shaders/program/deferred1.glsl"
const val REFLECTION_PATH = "/shaders/lib/materials/materialMethods/reflectionBackground.glsl"
const val REFLECTION_PATH_2 = "/shaders/lib/materials/materialMethods/reflections.glsl"

private const val DEFERRED_IMPORT_ANCHOR = "#if defined END && defined END_STARS\n    #include \"/lib/atmospherics/enderStars.glsl\""
private const val REFLECTIONS_IMPORT_ANCHOR = "#ifdef ATM_COLOR_MULTS"
private const val OVERWORLD_DEFERRED_ANCHOR = "color.rgb += nightNebula;"
private const val NETHER_DEFERRED_ANCHOR = "color.rgb = netherColor * (1.0 - maxBlindnessDarkness);"
private const val END_DEFERRED_ANCHOR = "color.rgb = endSkyColor;"
private const val OVERWORLD_REFLECTION_ANCHOR = "skyReflection += (DrawOverworldBeams(RVdotU, playerPos, viewPos) * 0.4 + 0.6).rgb * 0.08;"
private const val END_REFLECTION_ANCHOR = "vec3 skyReflection = endSkyColor * shadowMult;"

private val SKY_ANCHOR_FIXTURES = mapOf(
    "shader_anchors/complementary_r5_5/deferred1.glsl" to listOf(
        DEFERRED_IMPORT_ANCHOR,
        OVERWORLD_DEFERRED_ANCHOR,
        NETHER_DEFERRED_ANCHOR,
        END_DEFERRED_ANCHOR
    ),
    "shader_anchors/complementary_r5_5/reflectionBackground.glsl" to listOf(
        OVERWORLD_REFLECTION_ANCHOR,
        END_REFLECTION_ANCHOR
    ),
    "shader_anchors/complementary_r5_5/reflections.glsl" to listOf(
        REFLECTIONS_IMPORT_ANCHOR
    ),
    "shader_anchors/complementary_r5_6/deferred1.glsl" to listOf(
        DEFERRED_IMPORT_ANCHOR,
        OVERWORLD_DEFERRED_ANCHOR,
        NETHER_DEFERRED_ANCHOR,
        END_DEFERRED_ANCHOR
    ),
    "shader_anchors/complementary_r5_6/reflectionBackground.glsl" to listOf(
        OVERWORLD_REFLECTION_ANCHOR,
        END_REFLECTION_ANCHOR
    ),
    "shader_anchors/complementary_r5_6/reflections.glsl" to listOf(
        REFLECTIONS_IMPORT_ANCHOR
    )
)

private fun validateSkyAnchorFixtures() {
    SKY_ANCHOR_FIXTURES.forEach { (resourcePath, anchors) ->
        val fixture = Sky::class.java.classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
            ?: throw MinecraftError("Missing sky anchor fixture resource '$resourcePath'.", resourcePath)

        anchors.forEach { anchor ->
            requireAnchorCount(fixture, anchor, 1, resourcePath)
        }
    }
}

fun generateSkies(directory: Path) {
    validateSkyAnchorFixtures()

    // generate atmospheric libraries within atmospherics folder
    SKIES.forEach {
        val file = File(directory.absolutePathString() + "/shaders/lib/atmospherics/${it.name}")
        file.writeText(it.code)
    }

    val importCode = StringBuilder().apply {
        SKIES.forEach {
            append("#if ${(it.conditions + listOf("defined ${it.dimension}")).conditions()}\n")
            append("    #include \"/lib/atmospherics/${it.name}\"\n")
            append("#endif\n")
            append("\n")
        }
    }.toString()

    // injecting code into deferred1.glsl
    val deferredFile = File(directory.absolutePathString() + DEFERRED_PATH)
    val normalisedDeferredImports = deferredFile.readText().replace("\r\n", "\n")
    deferredFile.writeText(
        insertBeforeAnchorOnce(
            normalisedDeferredImports,
            DEFERRED_IMPORT_ANCHOR,
            "$importCode\n",
            DEFERRED_PATH
        )
    )

    // injecting code into gbuffers_water.glsl / dh_water.glsl
    val reflectionPath = File(directory.absolutePathString() + REFLECTION_PATH_2)
    reflectionPath.writeText(
        insertBeforeAnchorOnce(
            reflectionPath.readText(),
            REFLECTIONS_IMPORT_ANCHOR,
            importCode.prependIndent("    ") + "\n",
            REFLECTION_PATH_2
        )
    )

    SKIES.forEach {
        val indent = it.deferredTarget.inferIndentation()
        val code = StringBuilder().apply {
            if (it.conditions.isNotEmpty()) {
                append("$indent\n")
                append("$indent#if ${it.conditions.conditions()}\n")
                append("$indent    ${it.deferred}\n")
                append("$indent#endif\n")
            } else {
                append("$indent\n")
                append("$indent${it.deferred}\n")
            }
        }.toString()

        val anchor = when (it.dimension) {
            "OVERWORLD" -> OVERWORLD_DEFERRED_ANCHOR
            "NETHER" -> NETHER_DEFERRED_ANCHOR
            "END" -> END_DEFERRED_ANCHOR
            else -> throw MinecraftError("Unsupported sky dimension '${it.dimension}' for deferred injection.", DEFERRED_PATH)
        }

        deferredFile.writeText(
            insertAfterAnchorOnce(
                deferredFile.readText(),
                anchor,
                code,
                DEFERRED_PATH
            )
        )
    }

    val reflectionFile = File(directory.absolutePathString() + REFLECTION_PATH)
    SKIES.forEach {
        val indent = it.reflectionTarget.inferIndentation()
        val code = StringBuilder().apply {
            if (it.conditions.isNotEmpty()) {
                append("$indent\n")
                append("$indent#if ${it.conditions.conditions()}\n")
                append("$indent    ${it.reflection}\n")
                append("$indent#endif\n")
            } else {
                append("$indent\n")
                append("$indent${it.reflection}\n")
            }
        }.toString()

        val anchor = when (it.dimension) {
            "OVERWORLD" -> OVERWORLD_REFLECTION_ANCHOR
            "END" -> END_REFLECTION_ANCHOR
            else -> null
        }

        if (anchor != null) {
            reflectionFile.writeText(
                insertAfterAnchorOnce(
                    reflectionFile.readText(),
                    anchor,
                    code,
                    REFLECTION_PATH
                )
            )
        }
    }
}
