package com.example.taskrabbit.ui.theme

data class AppState(
    val notificationsEnabled: Boolean = false,
    val language: String = "English",
    val darkModeEnabled: Boolean = false,
    val backgroundChoice: BackgroundChoice = BackgroundChoice.WHITE
)