package com.bedrock.launcher.data.util

import com.bedrock.launcher.domain.model.GameOptions
import java.io.File

object OptionsTxtParser {

    fun parse(file: File): GameOptions {
        if (!file.exists() || !file.canRead()) {
            return GameOptions()
        }

        val options = GameOptions()
        try {
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && trimmed.contains(":")) {
                    val parts = trimmed.split(":", limit = 2)
                    val key = parts[0].trim()
                    val value = parts[1].trim()

                    when (key) {
                        "gfx_max_framerate" -> options.maxFramerate = value.toIntOrNull() ?: 120
                        "gfx_viewdistance" -> options.viewDistance = value.toIntOrNull() ?: 192
                        "gfx_guiscale" -> options.guiScale = value.toIntOrNull() ?: 0
                        "gfx_field_of_view" -> options.fov = value.toFloatOrNull() ?: 70f
                        "gfx_vsync" -> options.vsync = value == "1"
                        "gfx_msaa" -> options.antiAliasing = value.toIntOrNull() ?: 1
                        "gfx_fancygraphics" -> options.fancyGraphics = value == "1"
                        "gfx_smoothlighting" -> options.smoothLighting = value == "1"
                        "game_hidehud" -> options.hideHud = value == "1"
                        "game_hidehand" -> options.hideHand = value == "1"
                        "game_hidepaperdoll" -> options.hidePaperDoll = value == "1"
                        "game_showfps" -> options.showFps = value == "1"
                        else -> options.additionalRawOptions[key] = value
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return options
    }

    fun parseFromString(content: String): GameOptions {
        val options = GameOptions()
        content.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && trimmed.contains(":")) {
                val parts = trimmed.split(":", limit = 2)
                val key = parts[0].trim()
                val value = parts[1].trim()

                when (key) {
                    "gfx_max_framerate" -> options.maxFramerate = value.toIntOrNull() ?: 120
                    "gfx_viewdistance" -> options.viewDistance = value.toIntOrNull() ?: 192
                    "gfx_guiscale" -> options.guiScale = value.toIntOrNull() ?: 0
                    "gfx_field_of_view" -> options.fov = value.toFloatOrNull() ?: 70f
                    "gfx_vsync" -> options.vsync = value == "1"
                    "gfx_msaa" -> options.antiAliasing = value.toIntOrNull() ?: 1
                    "gfx_fancygraphics" -> options.fancyGraphics = value == "1"
                    "gfx_smoothlighting" -> options.smoothLighting = value == "1"
                    "game_hidehud" -> options.hideHud = value == "1"
                    "game_hidehand" -> options.hideHand = value == "1"
                    "game_hidepaperdoll" -> options.hidePaperDoll = value == "1"
                    "game_showfps" -> options.showFps = value == "1"
                    else -> options.additionalRawOptions[key] = value
                }
            }
        }
        return options
    }

    fun serialize(options: GameOptions): String {
        val sb = StringBuilder()
        
        // Essential Bedrock performance/graphics options
        sb.appendLine("gfx_max_framerate:${options.maxFramerate}")
        sb.appendLine("gfx_viewdistance:${options.viewDistance}")
        sb.appendLine("gfx_guiscale:${options.guiScale}")
        sb.appendLine("gfx_field_of_view:${options.fov}")
        sb.appendLine("gfx_vsync:${if (options.vsync) 1 else 0}")
        sb.appendLine("gfx_msaa:${options.antiAliasing}")
        sb.appendLine("gfx_fancygraphics:${if (options.fancyGraphics) 1 else 0}")
        sb.appendLine("gfx_smoothlighting:${if (options.smoothLighting) 1 else 0}")
        sb.appendLine("game_hidehud:${if (options.hideHud) 1 else 0}")
        sb.appendLine("game_hidehand:${if (options.hideHand) 1 else 0}")
        sb.appendLine("game_hidepaperdoll:${if (options.hidePaperDoll) 1 else 0}")
        sb.appendLine("game_showfps:${if (options.showFps) 1 else 0}")

        // Preserve other vanilla options
        options.additionalRawOptions.forEach { (key, value) ->
            sb.appendLine("$key:$value")
        }

        return sb.toString()
    }

    fun saveToFile(options: GameOptions, file: File) {
        file.parentFile?.mkdirs()
        file.writeText(serialize(options))
    }
}
