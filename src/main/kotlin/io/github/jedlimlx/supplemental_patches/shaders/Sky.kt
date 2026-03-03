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

private fun String.inferIndentation(): String {
    val line = lines().lastOrNull() ?: ""
    return " ".repeat(line.indexOfFirst { !it.isWhitespace() }.takeIf { it >= 0 } ?: 0)
}

private fun injectAtTarget(file: File, target: String, insertion: String, sourceName: String, targetName: String) {
    val contents = file.readText()
    val occurrences = Regex(Regex.escape(target)).findAll(contents).count()

    if (occurrences == 0) {
        throw MinecraftError("Sky '$sourceName' target '$targetName' was not found in ${file.path}", null)
    }

    if (occurrences > 1) {
        throw MinecraftError("Sky '$sourceName' target '$targetName' is ambiguous in ${file.path} (found $occurrences matches)", null)
    }

    file.writeText(contents.replaceFirst(target, "$target$insertion"))
}

fun generateSkies(directory: Path) {
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
    deferredFile.writeText(
        deferredFile.readText().replace(
            Regex("#if defined END && defined END_STARS\r?\n    #include \"/lib/atmospherics/enderStars.glsl\""),
            "$importCode\n#if defined END && defined END_STARS\n    #include \"/lib/atmospherics/enderStars.glsl\""
        )
    )

    // injecting code into gbuffers_water.glsl / dh_water.glsl
    val reflectionPath = File(directory.absolutePathString() + REFLECTION_PATH_2)
    reflectionPath.writeText(
        reflectionPath.readText().replace(
            "#ifdef ATM_COLOR_MULTS",
            importCode.prependIndent("    ") + "\n#ifdef ATM_COLOR_MULTS"
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

        injectAtTarget(deferredFile, it.deferredTarget, code, it.name, "deferredTarget")
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

        injectAtTarget(reflectionFile, it.reflectionTarget, code, it.name, "reflectionTarget")
    }
}
