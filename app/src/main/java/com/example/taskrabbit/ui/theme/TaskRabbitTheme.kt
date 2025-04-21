package com.example.taskrabbit.ui.theme

import Pink40
import Pink80
import Purple40
import Purple80
import PurpleGrey40
import PurpleGrey80
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.example.taskrabbit.R
import com.example.taskrabbit.ui.theme.Typography // Ensure Typography is imported

// --- REMOVE ENUM Redeclarations ---
// enum class BackgroundChoice { WHITE, BUTTERFLY, COLORFUL, CUTE, FLOWERS, RAINBOW, SHOOTING_STAR, SKELETON_HEAD } // REMOVED
// enum class DarkModePref { LIGHT, DARK, SYSTEM } // REMOVED
// --- Ensure BackgroundChoice and DarkModePref are imported from their central definition location ---


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    /* Other default colors to override... */
)

// --- Helper function to get the Painter for the background ---
@Composable
fun getBackgroundImagePainter(choice: BackgroundChoice): Painter? {
    return when (choice) {
        BackgroundChoice.WHITE -> null // No image for white background
        BackgroundChoice.BUTTERFLY -> painterResource(id = R.drawable.bg_butterfly)
        BackgroundChoice.COLORFUL -> painterResource(id = R.drawable.bg_colorful)
        BackgroundChoice.CUTE -> painterResource(id = R.drawable.bg_cute)
        BackgroundChoice.FLOWERS -> painterResource(id = R.drawable.bg_flowers)
        BackgroundChoice.RAINBOW -> painterResource(id = R.drawable.bg_rainbow)
        BackgroundChoice.SHOOTING_STAR -> painterResource(id = R.drawable.bg_shooting_star)
        BackgroundChoice.SKELETON_HEAD -> painterResource(id = R.drawable.bg_skeleton_head)
        // Add cases for any other backgrounds you might add
    }
}
// ----------------------------------------------------------

@Composable
fun TaskRabbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    backgroundChoice: BackgroundChoice = BackgroundChoice.WHITE, // Accepts parameter
    dynamicColor: Boolean = true, // Keep if you use dynamic colors
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

    val backgroundPainter = getBackgroundImagePainter(choice = backgroundChoice)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Use the imported Typography object
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (backgroundPainter != null) {
                Image(
                    painter = backgroundPainter,
                    contentDescription = null, // Decorative background
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            content()
        }
    }
}