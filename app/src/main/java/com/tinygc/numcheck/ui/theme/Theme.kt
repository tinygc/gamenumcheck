package com.tinygc.numcheck.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.tinygc.numcheck.presentation.theme.StockSimColors
import com.tinygc.numcheck.presentation.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = StockSimColors.Primary,
    secondary = StockSimColors.Secondary,
    tertiary = StockSimColors.Warning,
    background = StockSimColors.OnSurface,
    surface = StockSimColors.OnSurface,
    error = StockSimColors.Error,
    onPrimary = StockSimColors.OnPrimary,
    onSecondary = StockSimColors.OnSecondary,
    onBackground = StockSimColors.Background,
    onSurface = StockSimColors.Background
)

private val LightColorScheme = lightColorScheme(
    primary = StockSimColors.Primary,
    secondary = StockSimColors.Secondary,
    tertiary = StockSimColors.Warning,
    background = StockSimColors.Background,
    surface = StockSimColors.Surface,
    error = StockSimColors.Error,
    onPrimary = StockSimColors.OnPrimary,
    onSecondary = StockSimColors.OnSecondary,
    onBackground = StockSimColors.OnBackground,
    onSurface = StockSimColors.OnSurface
)

@Composable
fun StockSimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 株式アプリなので固定カラーを使用
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// 下位互換性のため
@Composable
fun NumcheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StockSimTheme(darkTheme, dynamicColor, content)
}