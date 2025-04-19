package com.example.taskrabbit.ui.theme

data class AppState(
    val backgroundChoice: BackgroundChoice = BackgroundChoice.WHITE,
    val notificationsEnabled: Boolean = true,
    val language: String = "English",
    val darkModeEnabled: Boolean = false
)