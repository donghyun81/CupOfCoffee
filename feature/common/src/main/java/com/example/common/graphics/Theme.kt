package com.example.common.graphics
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cupofcoffee0801.ui.graphics.AppTypography
import com.cupofcoffee0801.ui.graphics.backgroundDark
import com.cupofcoffee0801.ui.graphics.backgroundLight
import com.cupofcoffee0801.ui.graphics.errorContainerDark
import com.cupofcoffee0801.ui.graphics.errorContainerLight
import com.cupofcoffee0801.ui.graphics.errorDark
import com.cupofcoffee0801.ui.graphics.errorLight
import com.cupofcoffee0801.ui.graphics.inverseOnSurfaceDark
import com.cupofcoffee0801.ui.graphics.inverseOnSurfaceLight
import com.cupofcoffee0801.ui.graphics.inversePrimaryDark
import com.cupofcoffee0801.ui.graphics.inversePrimaryLight
import com.cupofcoffee0801.ui.graphics.inverseSurfaceDark
import com.cupofcoffee0801.ui.graphics.inverseSurfaceLight
import com.cupofcoffee0801.ui.graphics.onBackgroundDark
import com.cupofcoffee0801.ui.graphics.onBackgroundLight
import com.cupofcoffee0801.ui.graphics.onErrorContainerDark
import com.cupofcoffee0801.ui.graphics.onErrorContainerLight
import com.cupofcoffee0801.ui.graphics.onErrorDark
import com.cupofcoffee0801.ui.graphics.onErrorLight
import com.cupofcoffee0801.ui.graphics.onPrimaryContainerDark
import com.cupofcoffee0801.ui.graphics.onPrimaryContainerLight
import com.cupofcoffee0801.ui.graphics.onPrimaryDark
import com.cupofcoffee0801.ui.graphics.onPrimaryLight
import com.cupofcoffee0801.ui.graphics.onSecondaryContainerDark
import com.cupofcoffee0801.ui.graphics.onSecondaryContainerLight
import com.cupofcoffee0801.ui.graphics.onSecondaryDark
import com.cupofcoffee0801.ui.graphics.onSecondaryLight
import com.cupofcoffee0801.ui.graphics.onSurfaceDark
import com.cupofcoffee0801.ui.graphics.onSurfaceLight
import com.cupofcoffee0801.ui.graphics.onSurfaceVariantDark
import com.cupofcoffee0801.ui.graphics.onSurfaceVariantLight
import com.cupofcoffee0801.ui.graphics.onTertiaryContainerDark
import com.cupofcoffee0801.ui.graphics.onTertiaryContainerLight
import com.cupofcoffee0801.ui.graphics.onTertiaryDark
import com.cupofcoffee0801.ui.graphics.onTertiaryLight
import com.cupofcoffee0801.ui.graphics.outlineDark
import com.cupofcoffee0801.ui.graphics.outlineLight
import com.cupofcoffee0801.ui.graphics.outlineVariantDark
import com.cupofcoffee0801.ui.graphics.outlineVariantLight
import com.cupofcoffee0801.ui.graphics.primaryContainerDark
import com.cupofcoffee0801.ui.graphics.primaryContainerLight
import com.cupofcoffee0801.ui.graphics.primaryDark
import com.cupofcoffee0801.ui.graphics.primaryLight
import com.cupofcoffee0801.ui.graphics.scrimDark
import com.cupofcoffee0801.ui.graphics.scrimLight
import com.cupofcoffee0801.ui.graphics.secondaryContainerDark
import com.cupofcoffee0801.ui.graphics.secondaryContainerLight
import com.cupofcoffee0801.ui.graphics.secondaryDark
import com.cupofcoffee0801.ui.graphics.secondaryLight
import com.cupofcoffee0801.ui.graphics.surfaceBrightDark
import com.cupofcoffee0801.ui.graphics.surfaceBrightLight
import com.cupofcoffee0801.ui.graphics.surfaceContainerDark
import com.cupofcoffee0801.ui.graphics.surfaceContainerHighDark
import com.cupofcoffee0801.ui.graphics.surfaceContainerHighLight
import com.cupofcoffee0801.ui.graphics.surfaceContainerHighestDark
import com.cupofcoffee0801.ui.graphics.surfaceContainerHighestLight
import com.cupofcoffee0801.ui.graphics.surfaceContainerLight
import com.cupofcoffee0801.ui.graphics.surfaceContainerLowDark
import com.cupofcoffee0801.ui.graphics.surfaceContainerLowLight
import com.cupofcoffee0801.ui.graphics.surfaceContainerLowestDark
import com.cupofcoffee0801.ui.graphics.surfaceContainerLowestLight
import com.cupofcoffee0801.ui.graphics.surfaceDark
import com.cupofcoffee0801.ui.graphics.surfaceDimDark
import com.cupofcoffee0801.ui.graphics.surfaceDimLight
import com.cupofcoffee0801.ui.graphics.surfaceLight
import com.cupofcoffee0801.ui.graphics.surfaceVariantDark
import com.cupofcoffee0801.ui.graphics.surfaceVariantLight
import com.cupofcoffee0801.ui.graphics.tertiaryContainerDark
import com.cupofcoffee0801.ui.graphics.tertiaryContainerLight
import com.cupofcoffee0801.ui.graphics.tertiaryDark
import com.cupofcoffee0801.ui.graphics.tertiaryLight

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> darkScheme
      else -> lightScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}

