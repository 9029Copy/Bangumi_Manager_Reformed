package com.copy9029.bangumimanagerreformed.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


data class BangumiColorScheme(
    val main: Color,
    val indexBorder: Color,
    val indexCardContainer: Color,
    val indexButtonContainer: Color,
    val indexPrimaryContent: Color,
    val indexSecondaryContent: Color,
    val calendarUnfinishedTagContainer: Color,
    val calendarFinishedTagContainer: Color,
)

/**
 * 根据主题色生成适合 Card 的颜色组。
 *
 * @param themeColorLong AARRGGBB 格式，例如 0xFFFF0000 表示不透明红色
 */
fun generateBangumiColorScheme(
    themeColorLong: Long,
): BangumiColorScheme {
    // 生成 Card 时通常希望最终颜色不透明，因此忽略输入主题色的 Alpha。
    val baseColor = themeColorLong.withOpaqueAlpha()

    /*
     * 浅色主题会少混入一些白色，防止颜色过淡；
     * 深色主题会多混入一些白色，避免背景过深。
     */
    val themeLuminance = relativeLuminance(baseColor)
    val whiteMixRatio = lerp(
        start = 0.72,
        end = 0.86,
        fraction = 1.0 - themeLuminance,
    )

    val background = blendArgb(
        foreground = baseColor,
        background = 0xFFFFFFFF,
        foregroundRatio = 1.0 - whiteMixRatio,
    )

    // 边框比主题色稍暗，同时向背景靠近一点，避免边框过于突兀。
    val darkenedTheme = multiplyRgb(baseColor, factor = 0.82)
    val border = blendArgb(
        foreground = darkenedTheme,
        background = background,
        foregroundRatio = 0.72,
    )

    val black = 0xFF171717
    val white = 0xFFF7F7F7

    // 使用 WCAG 对比度选择主要文字颜色。
    val blackContrast = contrastRatio(black, background)
    val whiteContrast = contrastRatio(white, background)

    val primaryText = when {
        blackContrast >= 4.5 -> black
        whiteContrast >= 4.5 -> white
        blackContrast >= whiteContrast -> black
        else -> white
    }

    /*
     * 次要文字向背景靠近。
     * 相比单纯降低 Alpha，这种方式在不同父背景上表现更稳定。
     */
    val secondaryText = blendArgb(
        foreground = primaryText,
        background = background,
        foregroundRatio = if (primaryText == black) 0.68 else 0.76,
    )

    val buttonContainer = blendArgb(
        foreground = baseColor,
        background = background,
        foregroundRatio = 0.32, // here
    )

    val doneBackground = blendArgb(
        foreground = themeColorLong,
        background = 0xFFFFFFFF,
        foregroundRatio = 0.43,
    )

    return BangumiColorScheme(
        main = Color(themeColorLong),
        indexCardContainer = Color(background),
        indexBorder = Color(border),
        indexPrimaryContent = Color(primaryText),
        indexSecondaryContent = Color(secondaryText),
        indexButtonContainer = Color(buttonContainer),
        calendarUnfinishedTagContainer = Color(themeColorLong),
        calendarFinishedTagContainer = Color(doneBackground),
    )
}

/**
 * 按比例混合两种 AARRGGBB 颜色。
 *
 * foregroundRatio = 1.0 时完全使用 foreground；
 * foregroundRatio = 0.0 时完全使用 background。
 */
private fun blendArgb(
    foreground: Long,
    background: Long,
    foregroundRatio: Double,
): Long {
    val ratio = foregroundRatio.coerceIn(0.0, 1.0)
    val backgroundRatio = 1.0 - ratio

    val a = (
            foreground.alpha() * ratio +
                    background.alpha() * backgroundRatio
            ).toInt()

    val r = (
            foreground.red() * ratio +
                    background.red() * backgroundRatio
            ).toInt()

    val g = (
            foreground.green() * ratio +
                    background.green() * backgroundRatio
            ).toInt()

    val b = (
            foreground.blue() * ratio +
                    background.blue() * backgroundRatio
            ).toInt()

    return argb(a, r, g, b)
}

/**
 * 将 RGB 通道按比例压暗。
 */
private fun multiplyRgb(
    color: Long,
    factor: Double,
): Long {
    return argb(
        alpha = color.alpha(),
        red = (color.red() * factor).toInt(),
        green = (color.green() * factor).toInt(),
        blue = (color.blue() * factor).toInt(),
    )
}

/**
 * 根据 WCAG 定义计算颜色的相对亮度。
 */
private fun relativeLuminance(color: Long): Double {
    fun linearize(channel: Int): Double {
        val value = channel / 255.0

        return if (value <= 0.04045) {
            value / 12.92
        } else {
            ((value + 0.055) / 1.055).pow(2.4)
        }
    }

    val r = linearize(color.red())
    val g = linearize(color.green())
    val b = linearize(color.blue())

    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * 计算两种颜色之间的 WCAG 对比度。
 */
private fun contrastRatio(
    first: Long,
    second: Long,
): Double {
    val firstLuminance = relativeLuminance(first)
    val secondLuminance = relativeLuminance(second)

    val lighter = max(firstLuminance, secondLuminance)
    val darker = min(firstLuminance, secondLuminance)

    return (lighter + 0.05) / (darker + 0.05)
}

private fun lerp(
    start: Double,
    end: Double,
    fraction: Double,
): Double {
    return start + (end - start) * fraction.coerceIn(0.0, 1.0)
}

private fun Long.withOpaqueAlpha(): Long {
    return this and 0x00FFFFFFL or 0xFF000000L
}

private fun Long.alpha(): Int = ((this shr 24) and 0xFF).toInt()

private fun Long.red(): Int = ((this shr 16) and 0xFF).toInt()

private fun Long.green(): Int = ((this shr 8) and 0xFF).toInt()

private fun Long.blue(): Int = (this and 0xFF).toInt()

private fun argb(
    alpha: Int,
    red: Int,
    green: Int,
    blue: Int,
): Long {
    return (
            (alpha.coerceIn(0, 255).toLong() shl 24) or
                    (red.coerceIn(0, 255).toLong() shl 16) or
                    (green.coerceIn(0, 255).toLong() shl 8) or
                    blue.coerceIn(0, 255).toLong()
            )
}
