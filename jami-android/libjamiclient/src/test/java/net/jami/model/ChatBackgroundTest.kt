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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatBackgroundTest {
    @Test
    fun defaultKeepsTheThemeBackground() {
        assertNull(ChatBackground.Default.fill(darkTheme = false))
        assertNull(ChatBackground.Default.fill(darkTheme = true))
    }

    @Test
    fun presetTakesTheVariantOfTheTheme() {
        val sky = ChatBackground.Preset(ChatBackgroundPreset.SKY)
        assertEquals(ChatBackgroundFill.Solid(0xFFDCEEFB.toInt()), sky.fill(darkTheme = false))
        assertEquals(ChatBackgroundFill.Solid(0xFF0F2A3D.toInt()), sky.fill(darkTheme = true))
    }

    @Test
    fun presetsAreSixColorsThenFourGradients() {
        assertEquals(
            listOf("sky", "mint", "sand", "rose", "lavender", "slate", "sunrise", "ocean", "meadow", "dusk"),
            ChatBackgroundPreset.entries.map { it.id }
        )
        for (preset in ChatBackgroundPreset.entries) {
            val isGradient = preset.ordinal >= 6
            assertEquals(preset.id, isGradient, preset.light is ChatBackgroundFill.Gradient)
            assertEquals(preset.id, isGradient, preset.dark is ChatBackgroundFill.Gradient)
        }
    }

    @Test
    fun gradientGoesFromTheFirstColorToTheSecond() {
        assertEquals(
            ChatBackgroundFill.Gradient(0xFFCDE8F6.toInt(), 0xFFD9D4F5.toInt()),
            ChatBackground.Preset(ChatBackgroundPreset.OCEAN).fill(darkTheme = false)
        )
        assertEquals(
            ChatBackgroundFill.Gradient(0xFF0E2A3A.toInt(), 0xFF211C3E.toInt()),
            ChatBackground.Preset(ChatBackgroundPreset.OCEAN).fill(darkTheme = true)
        )
    }

    @Test
    fun customColorIsUsedAsIsInBothThemes() {
        val custom = ChatBackground.Custom(0xFF336699.toInt())
        assertEquals(ChatBackgroundFill.Solid(0xFF336699.toInt()), custom.fill(darkTheme = false))
        assertEquals(ChatBackgroundFill.Solid(0xFF336699.toInt()), custom.fill(darkTheme = true))
    }

    @Test
    fun everySwatchChoiceIsReadBackAsStored() {
        val choices = listOf(ChatBackground.Default, ChatBackground.Custom(0xFFA1B2C3.toInt())) +
            ChatBackgroundPreset.entries.map { ChatBackground.Preset(it) }
        for (choice in choices) {
            assertEquals(choice, ChatBackground.parse(choice.serialize()))
        }
    }

    @Test
    fun presetIsStoredByIdAndCustomColorAsRgb() {
        assertEquals("default", ChatBackground.Default.serialize())
        assertEquals("mint", ChatBackground.Preset(ChatBackgroundPreset.MINT).serialize())
        assertEquals("#336699", ChatBackground.Custom(0xFF336699.toInt()).serialize())
        assertEquals(ChatBackground.Custom(0xFFA1B2C3.toInt()), ChatBackground.parse("#a1b2c3"))
    }

    @Test
    fun unknownOrInvalidStoredValueIsDefault() {
        for (stored in listOf(null, "", "gone", "#12345", "#1234567", "#12345g", "336699", "#FF336699")) {
            assertEquals(stored, ChatBackground.Default, ChatBackground.parse(stored))
        }
    }
}
