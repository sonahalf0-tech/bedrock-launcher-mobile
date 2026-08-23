package com.bedrock.launcher.domain.model

data class GameOptions(
    var maxFramerate: Int = 120,          // gfx_max_framerate:0 or number
    var viewDistance: Int = 192,          // gfx_viewdistance (in blocks: 192 = 12 chunks, 256 = 16 chunks)
    var guiScale: Int = 0,                // gfx_guiscale
    var fov: Float = 70.0f,               // gfx_field_of_view
    var vsync: Boolean = false,           // gfx_vsync:0 or 1
    var antiAliasing: Int = 1,            // gfx_msaa:1
    var fancyGraphics: Boolean = true,    // gfx_fancygraphics:1
    var smoothLighting: Boolean = true,   // gfx_smoothlighting:1
    var hideHud: Boolean = false,         // game_hidehud:0
    var hideHand: Boolean = false,        // game_hidehand:0
    var hidePaperDoll: Boolean = false,   // game_hidepaperdoll:0
    var showFps: Boolean = true,          // game_showfps:1
    var additionalRawOptions: MutableMap<String, String> = mutableMapOf()
) {
    fun getChunks(): Int = (viewDistance / 16).coerceAtLeast(4)
    fun setChunks(chunks: Int) {
        viewDistance = chunks * 16
    }
}
