package com.example.otriviafan.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ResponsiveSizes(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val buttonHeight: Dp,
    val fontSizeLarge: TextUnit,
    val fontSizeMedium: TextUnit,
    val fontSizeSmall: TextUnit,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp
)

@Composable
fun rememberResponsiveSizes(): ResponsiveSizes {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return ResponsiveSizes(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        buttonHeight = screenHeight * 0.08f,
        fontSizeLarge = (screenWidth.value * 0.07f).sp,
        fontSizeMedium = (screenWidth.value * 0.05f).sp,
        fontSizeSmall = (screenWidth.value * 0.04f).sp,
        spacingSmall = screenHeight * 0.01f,
        spacingMedium = screenHeight * 0.02f,
        spacingLarge = screenHeight * 0.03f
    )
}
