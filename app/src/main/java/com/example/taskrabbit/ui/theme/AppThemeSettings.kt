package com.example.taskrabbit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.material3.MaterialTheme // Import if using MaterialTheme colors
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.taskrabbit.R // Make sure R is imported for drawable resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppThemeSettings {
    // StateFlow for theme settings remains the same
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    /**
     * Returns the Painter for the selected background image based on ThemeSettings.
     * Returns null if the choice is WHITE or any other non-image background.
     */
    @Composable
    fun getBackgroundImagePainter(themeSettings: ThemeSettings, context: Context): Painter? {
        return when (themeSettings.backgroundChoice) {
            BackgroundChoice.WHITE -> null // No image for default white background
            BackgroundChoice.BUTTERFLY -> painterResource(id = R.drawable.bg_butterfly)
            BackgroundChoice.COLORFUL -> painterResource(id = R.drawable.bg_colorful)
            BackgroundChoice.CUTE -> painterResource(id = R.drawable.bg_cute)
            BackgroundChoice.FLOWERS -> painterResource(id = R.drawable.bg_flowers)
            BackgroundChoice.RAINBOW -> painterResource(id = R.drawable.bg_rainbow)
            BackgroundChoice.SHOOTING_STAR -> painterResource(id = R.drawable.bg_shooting_star)
            BackgroundChoice.SKELETON_HEAD -> painterResource(id = R.drawable.bg_skeleton_head)
            // Add other BackgroundChoice cases for images here
            else -> null // Default to no image
        }
    }

    /**
     * Returns the appropriate background color for content areas.
     * Returns Color.Transparent if an image background is selected in ThemeSettings,
     * otherwise returns a solid color based on the choice and dark mode.
     */
    @Composable // Make @Composable if using MaterialTheme colors inside
    fun getBackgroundColor(themeSettings: ThemeSettings, context: Context): Color {
        return when (themeSettings.backgroundChoice) {
            // Handle solid background choices explicitly
            BackgroundChoice.WHITE -> {
                if (themeSettings.darkModeEnabled) {
                    // Use a specific dark color or a color from your MaterialTheme
                    // MaterialTheme.colorScheme.background
                    Color.DarkGray // Example dark background
                } else {
                    // Use a specific light color or a color from your MaterialTheme
                    // MaterialTheme.colorScheme.background
                    Color.White // Example light background
                }
            }

            // For all other choices (assumed to be image backgrounds based on getBackgroundImagePainter)
            // return Transparent so the Image behind the content Column shows through.
            else -> {
                Color.Transparent
            }
        }
    }

    /**
     * Updates the global theme settings state. Usually called by the ViewModel.
     */
    fun updateThemeSettings(newSettings: ThemeSettings) {
        _themeSettings.value = newSettings
    }
}