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
package cx.ring.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.color.MaterialColors
import cx.ring.R
import cx.ring.utils.ChatBackgroundUtils
import net.jami.model.ChatBackground
import net.jami.model.ChatBackgroundFill
import net.jami.model.ChatBackgroundPreset

/**
 * The swatches of the chat background setting, in a row that wraps: the theme's background crossed
 * out for the default, the presets in the variant of the theme in use, then the custom color, a
 * rainbow until one is chosen. The chosen swatch is outlined in the accent color.
 */
class ChatBackgroundPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FlexboxLayout(context, attrs) {

    /** Called with the background of the swatch the user taps, or with null for the custom swatch. */
    var onSwatchClick: ((ChatBackground?) -> Unit)? = null

    private val darkTheme = ChatBackgroundUtils.isDarkTheme(context)
    private val borderColor =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutlineVariant, Color.GRAY)
    private val swatches: Map<ChatBackground, ImageView>
    private val customSwatch: ImageView

    init {
        swatches = buildMap<ChatBackground, ImageView> {
            put(ChatBackground.Default, addSwatch(R.string.pref_chat_background_default, ChatBackground.Default,
                ContextCompat.getDrawable(context, R.drawable.chat_background_default_swatch)))
            for (preset in ChatBackgroundPreset.entries) {
                val choice = ChatBackground.Preset(preset)
                put(choice, addSwatch(titleOf(preset), choice, circle(preset.fill(darkTheme))))
            }
        }
        customSwatch = addSwatch(R.string.pref_chat_background_custom, null, rainbow())
    }

    /** Outlines the swatch of [chosen]; the custom swatch shows the custom color while it is chosen. */
    fun setChosen(chosen: ChatBackground) {
        for ((choice, swatch) in swatches) select(swatch, choice == chosen)
        select(customSwatch, chosen is ChatBackground.Custom)
        customSwatch.setImageDrawable(
            if (chosen is ChatBackground.Custom) circle(ChatBackgroundFill.Solid(chosen.color)) else rainbow()
        )
    }

    private fun addSwatch(@StringRes label: Int, choice: ChatBackground?, image: Drawable?): ImageView {
        val swatch = ImageView(context).apply {
            layoutParams = FlexboxLayout.LayoutParams(dp(SWATCH_SIZE), dp(SWATCH_SIZE)).apply {
                marginEnd = dp(SWATCH_MARGIN)
                bottomMargin = dp(SWATCH_MARGIN)
            }
            dp(RING_SPACE).let { setPadding(it, it, it, it) }
            setImageDrawable(image)
            contentDescription = context.getString(label)
            tooltipText = contentDescription
            setOnClickListener { onSwatchClick?.invoke(choice) }
        }
        addView(swatch)
        return swatch
    }

    private fun select(swatch: ImageView, selected: Boolean) {
        swatch.isSelected = selected
        swatch.background = if (selected) GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(RING_WIDTH), ContextCompat.getColor(context, R.color.colorPrimary))
        } else null
    }

    private fun circle(fill: ChatBackgroundFill): GradientDrawable =
        ChatBackgroundUtils.getDrawable(fill).apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(BORDER_WIDTH), borderColor)
        }

    private fun rainbow() = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        gradientType = GradientDrawable.SWEEP_GRADIENT
        colors = RAINBOW
        setStroke(dp(BORDER_WIDTH), borderColor)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    companion object {
        // A swatch is a 40 dp circle; the 4 dp around it hold the 2 dp ring that outlines the chosen one.
        private const val SWATCH_SIZE = 48
        private const val RING_SPACE = 4
        private const val RING_WIDTH = 2
        private const val BORDER_WIDTH = 1
        private const val SWATCH_MARGIN = 4
        private val RAINBOW = intArrayOf(
            0xFFF44336.toInt(), 0xFFFFEB3B.toInt(), 0xFF4CAF50.toInt(), 0xFF00BCD4.toInt(),
            0xFF3F51B5.toInt(), 0xFFE91E63.toInt(), 0xFFF44336.toInt()
        )

        @StringRes
        private fun titleOf(preset: ChatBackgroundPreset): Int = when (preset) {
            ChatBackgroundPreset.SKY -> R.string.chat_background_sky
            ChatBackgroundPreset.MINT -> R.string.chat_background_mint
            ChatBackgroundPreset.SAND -> R.string.chat_background_sand
            ChatBackgroundPreset.ROSE -> R.string.chat_background_rose
            ChatBackgroundPreset.LAVENDER -> R.string.chat_background_lavender
            ChatBackgroundPreset.SLATE -> R.string.chat_background_slate
            ChatBackgroundPreset.SUNRISE -> R.string.chat_background_sunrise
            ChatBackgroundPreset.OCEAN -> R.string.chat_background_ocean
            ChatBackgroundPreset.MEADOW -> R.string.chat_background_meadow
            ChatBackgroundPreset.DUSK -> R.string.chat_background_dusk
        }
    }
}
