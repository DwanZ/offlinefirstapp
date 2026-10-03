package com.insigniaempresarial.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class InsigniaExtraColors(
    val profit: Color,
    val loss: Color,
    val pending: Color,
    val chart: Color,
    val brandSecondary: Color,
)

val LocalInsigniaExtraColors = staticCompositionLocalOf {
    InsigniaExtraColors(
        profit = ProfitGreen,
        loss = LossRed,
        pending = PendingAmber,
        chart = CyanAccent,
        brandSecondary = ElectricIndigo,
    )
}

private val DarkScheme = darkColorScheme(
    primary = EmeraldMint,
    onPrimary = OnBrand,
    secondary = ElectricIndigo,
    onSecondary = OnBrand,
    tertiary = CyanAccent,
    onTertiary = OnBrand,
    background = Slate900,
    onBackground = TextPrimaryDark,
    surface = Slate800,
    onSurface = TextPrimaryDark,
    surfaceVariant = Slate700,
    onSurfaceVariant = TextSecondaryDark,
    error = LossRed,
    onError = OnBrand,
    outline = Slate700,
)

private val LightScheme = lightColorScheme(
    primary = EmeraldMint,
    onPrimary = OnBrand,
    secondary = ElectricIndigo,
    onSecondary = OnBrand,
    tertiary = CyanAccent,
    onTertiary = OnBrand,
    background = Slate50,
    onBackground = TextPrimaryLight,
    surface = PureWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = Slate200,
    onSurfaceVariant = TextSecondaryLight,
    error = LossRed,
    onError = OnBrand,
    outline = Slate200,
)

private val ExtraColors = InsigniaExtraColors(
    profit = ProfitGreen,
    loss = LossRed,
    pending = PendingAmber,
    chart = CyanAccent,
    brandSecondary = ElectricIndigo,
)

@Composable
fun InsigniaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalInsigniaExtraColors provides ExtraColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = InsigniaTypography,
            content = content,
        )
    }
}

object InsigniaThemeExtras {
    val colors: InsigniaExtraColors
        @Composable
        get() = LocalInsigniaExtraColors.current
}
