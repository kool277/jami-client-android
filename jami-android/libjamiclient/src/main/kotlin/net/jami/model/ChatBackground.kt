/*
 *  Copyright (C) 2004-2026 Savoir-faire Linux Inc.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.jami.model

/**
 * The background of the conversations' message list, chosen in the settings: the theme's own, one
 * of the presets, or a color of the user's.
 */
sealed class ChatBackground {
    /** The theme's background: the message list looks as it does without the setting. */
    data object Default : ChatBackground()

    data class Preset(val preset: ChatBackgroundPreset) : ChatBackground()

    /** A color picked by the user, as an opaque ARGB color, used as is in both themes. */
    data class Custom(val color: Int) : ChatBackground()

    /** What the message list is painted with in the light or the dark theme; null keeps the theme's. */
    fun fill(darkTheme: Boolean): ChatBackgroundFill? = when (this) {
        Default -> null
        is Preset -> preset.fill(darkTheme)
        is Custom -> ChatBackgroundFill.Solid(color)
    }

    /** The value kept in the settings: "default", the preset's id, or the custom color as #RRGGBB. */
    fun serialize(): String = when (this) {
        Default -> DEFAULT
        is Preset -> preset.id
        is Custom -> String.format("#%06X", 0xFFFFFF and color)
    }

    companion object {
        private const val DEFAULT = "default"
        private val CUSTOM_COLOR = Regex("#[0-9A-Fa-f]{6}")

        /** The background kept in the settings by [serialize]; [Default] for a value it cannot use. */
        fun parse(value: String?): ChatBackground {
            if (value == null) return Default
            ChatBackgroundPreset.fromId(value)?.let { return Preset(it) }
            return if (CUSTOM_COLOR.matches(value)) Custom(value.substring(1).toInt(16) or OPAQUE) else Default
        }
    }
}

/** How the message list is painted. */
sealed class ChatBackgroundFill {
    data class Solid(val color: Int) : ChatBackgroundFill()

    /** A gradient from [start], in the top-left corner, to [end], in the bottom-right one. */
    data class Gradient(val start: Int, val end: Int) : ChatBackgroundFill()
}

/**
 * The chat backgrounds offered by every Jami client, each with a light and a dark variant so that
 * the text drawn on the background (dates, events) stays readable in both themes.
 */
enum class ChatBackgroundPreset(val id: String, val light: ChatBackgroundFill, val dark: ChatBackgroundFill) {
    SKY("sky", solid(0xdceefb), solid(0x0f2a3d)),
    MINT("mint", solid(0xdcf2e3), solid(0x0f2e22)),
    SAND("sand", solid(0xf5ead7), solid(0x2e2614)),
    ROSE("rose", solid(0xf8dee5), solid(0x33161f)),
    LAVENDER("lavender", solid(0xe7e1f7), solid(0x221a3a)),
    SLATE("slate", solid(0xdde2e8), solid(0x1c232b)),
    SUNRISE("sunrise", gradient(0xfde2c8, 0xf8c8dc), gradient(0x3a2418, 0x3a1a2a)),
    OCEAN("ocean", gradient(0xcde8f6, 0xd9d4f5), gradient(0x0e2a3a, 0x211c3e)),
    MEADOW("meadow", gradient(0xdff2d8, 0xcfeaf0), gradient(0x13301a, 0x0f2c33)),
    DUSK("dusk", gradient(0xe3d9f2, 0xf6d9e4), gradient(0x2b2445, 0x3b2236));

    fun fill(darkTheme: Boolean): ChatBackgroundFill = if (darkTheme) dark else light

    companion object {
        fun fromId(id: String): ChatBackgroundPreset? = entries.firstOrNull { it.id == id }
    }
}

private const val OPAQUE = 0xFF000000.toInt()

private fun solid(rgb: Int) = ChatBackgroundFill.Solid(rgb or OPAQUE)

private fun gradient(startRgb: Int, endRgb: Int) = ChatBackgroundFill.Gradient(startRgb or OPAQUE, endRgb or OPAQUE)
