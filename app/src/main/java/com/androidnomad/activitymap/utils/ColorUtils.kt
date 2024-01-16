package com.androidnomad.activitymap.utils

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

object ColorUtils {

    fun getColorFromHex(colorString: String): Color {
        return Color(android.graphics.Color.parseColor(colorString))
    }

    fun lighten(@ColorInt base: Int, @FloatRange(from = 0.0, to = 1.0) amount: Float): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(base, hsl)
        if (hsl[2] < 0.5) {
            hsl[2] = (hsl[2] + amount).coerceAtMost(1f)
        } else {
            hsl[2] = (hsl[2] - amount).coerceAtLeast(0f)
        }
        return ColorUtils.HSLToColor(hsl)
    }

    fun forceLighten(@ColorInt base: Int, @FloatRange(from = 0.0, to = 1.0) amount: Float): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(base, hsl)
        hsl[2] = amount
        return ColorUtils.HSLToColor(hsl)
    }

    fun hexcodeToHSL(colorString: String, offset: Float): Int {
        val hsl = getHSLColorFromHex(colorString)
        if (hsl[2] > 0.5f) {
            hsl[2] = (hsl[2] - offset).coerceAtLeast(0f)
        } else {
            hsl[2] = (hsl[2] + offset).coerceAtMost(1f)
        }
        return android.graphics.Color.HSVToColor(hsl)
    }

    private fun getHSLColorFromHex(colorString: String): FloatArray {
        val color = android.graphics.Color.parseColor(colorString)
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)
        return hsl
    }

    fun getColorFromHexString(colorString: String): android.graphics.Color {
        return android.graphics.Color.valueOf(android.graphics.Color.parseColor(colorString))
    }
}