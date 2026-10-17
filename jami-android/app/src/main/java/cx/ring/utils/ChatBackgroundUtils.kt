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
package cx.ring.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import net.jami.model.ChatBackground
import net.jami.model.ChatBackgroundFill

object ChatBackgroundUtils {
    /** Whether [context] shows the dark theme, which takes the dark variant of the presets. */
    fun isDarkTheme(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /** What the message list is painted with in the theme of [context]; null keeps the theme's background. */
    fun getDrawable(context: Context, background: ChatBackground): GradientDrawable? =
        background.fill(isDarkTheme(context))?.let { getDrawable(it) }

    /** Paints [fill]: a plain color, or a gradient from the top-left to the bottom-right corner. */
    fun getDrawable(fill: ChatBackgroundFill): GradientDrawable = when (fill) {
        is ChatBackgroundFill.Solid -> GradientDrawable().apply { setColor(fill.color) }
        is ChatBackgroundFill.Gradient ->
            GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(fill.start, fill.end))
    }
}
