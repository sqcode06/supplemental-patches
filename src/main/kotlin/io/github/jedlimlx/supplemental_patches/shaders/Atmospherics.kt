package io.github.jedlimlx.supplemental_patches.shaders

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

val ATMOSPHERICS = mutableListOf<Atmospherics>()
data class Atmospherics(val libPath: String, val libCode: String, val mainCode: String, val conditions: List<String>)

const val COMPOSITE_PATH = "/shaders/program/composite1.glsl"
fun generateAtmospherics(directory: Path) {
    val file = File(directory.absolutePathString() + COMPOSITE_PATH)

    val compositeCode = StringBuilder().apply {
        val indent = "    "
        ATMOSPHERICS.forEach {
            append("$indent#if ${it.conditions.conditions()}\n")
            append(it.mainCode.prependIndent(indent.repeat(2)) + "\n")
            append("$indent#endif\n")
            append("\n")
        }
    }.toString()

    val compositeIncludes = StringBuilder().apply {
        val indent = "    "
        ATMOSPHERICS.forEach {
            append("#if ${it.conditions.conditions()}\n")
            append("$indent#include \"/lib/atmospherics/${it.libPath}\"\n")
            append("#endif\n")
            append("\n")
        }
    }.toString()

    file.writeText(
        insertBeforeAnchorOnce(
            file.readText(),
            "    if (isEyeInWater == 1) {",
            "$compositeCode\n",
            COMPOSITE_PATH
        )
    )

    file.writeText(
        insertAfterAnchorOnce(
            file.readText(),
            "//Includes//",
            "\n$compositeIncludes",
            COMPOSITE_PATH
        )
    )

    ATMOSPHERICS.forEach {
        val file = File(directory.absolutePathString() + "/shaders/lib/atmospherics/${it.libPath}")
        file.writeText(it.libCode)
    }

    val conditions = ATMOSPHERICS.map { "(${it.conditions.conditions()})" }.joinToString(" || ")
    file.writeText(
        insertBeforeAnchorOnce(
            file.readText(),
            "defined NETHER_STORM || defined COLORED_LIGHT_FOG",
            "$conditions || ",
            COMPOSITE_PATH
        )
    )
}
