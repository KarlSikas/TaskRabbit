package com.example.taskrabbit.ui.theme

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun AppThemeSettings(): StateFlow<ThemeSettings> {
    // Example: Replace this with your actual theme settings logic
    val _themeSettings = MutableStateFlow(ThemeSettings())
    return _themeSettings.asStateFlow()
}