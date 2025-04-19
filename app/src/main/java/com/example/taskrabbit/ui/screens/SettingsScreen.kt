package com.example.taskrabbit.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*      // Keep existing imports
import androidx.compose.runtime.getValue // Import collectAsState's extension function
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.R
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.viewmodel.SettingsViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskrabbit.ui.theme.ThemeSettings
import com.example.taskrabbit.ui.theme.AppThemeSettings // Import AppThemeSettings

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    // themeSettings parameter is still passed but we'll primarily use the collected state
    themeSettings: ThemeSettings,
    settingsViewModel: SettingsViewModel
) {
    // --- Collect the state flow ---
    // `currentThemeSettings` will automatically update when AppThemeSettings.themeSettings changes.
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    // --- End state collection ---

    val scrollState = rememberScrollState()
    val notificationsEnabled = remember { mutableStateOf(false) } // Local state for notifications
    val language = remember { mutableStateOf("English") }         // Local state for language
    val context = LocalContext.current

    // --- Use collected state for background ---
    // Use `currentThemeSettings` to get the background painter and color
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)
    // --- End background usage ---

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Fallback background
    ) {
        Box(modifier = Modifier.fillMaxSize()) { // Use Box for layering

            // Apply background image if one is selected
            if (backgroundPainter != null) {
                Image(
                    painter = backgroundPainter,
                    contentDescription = null, // Decorative background
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Apply solid background color *only* if there's no image painter
                    .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
                    .padding(16.dp)
                    .verticalScroll(scrollState) // Make the content scrollable over the background
            ) {
                // Top bar with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Background image choices title
                Text(
                    stringResource(id = R.string.background_images),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // --- Background Image Rows (Use collected state) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DefaultBackgroundOption(
                        name = stringResource(id = R.string.default_option),
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.WHITE, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.WHITE)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_butterfly, name = stringResource(id = R.string.butterfly), choice = BackgroundChoice.BUTTERFLY,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.BUTTERFLY, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.BUTTERFLY)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_colorful, name = stringResource(id = R.string.colorful), choice = BackgroundChoice.COLORFUL,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.COLORFUL, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.COLORFUL)) }
                    )
                }
                // Second row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_cute, name = stringResource(id = R.string.cute), choice = BackgroundChoice.CUTE,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.CUTE, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.CUTE)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_flowers, name = stringResource(id = R.string.flowers), choice = BackgroundChoice.FLOWERS,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.FLOWERS, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.FLOWERS)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_rainbow, name = stringResource(id = R.string.rainbow), choice = BackgroundChoice.RAINBOW,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.RAINBOW, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.RAINBOW)) }
                    )
                }
                // Third row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_shooting_star, name = stringResource(id = R.string.stars), choice = BackgroundChoice.SHOOTING_STAR,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SHOOTING_STAR, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SHOOTING_STAR)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_skeleton_head, name = stringResource(id = R.string.skull), choice = BackgroundChoice.SKELETON_HEAD,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SKELETON_HEAD, // Use collected state
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SKELETON_HEAD)) }
                    )
                    Spacer(modifier = Modifier.width(70.dp)) // Balance layout
                }
                // --- End Background Image Rows ---

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Dark mode toggle (Use collected state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(id = R.string.dark_mode),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = currentThemeSettings.darkModeEnabled, // Use collected state
                        onCheckedChange = { isChecked ->
                            // Update based on the *current* collected state
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(darkModeEnabled = isChecked))
                        }
                    )
                }

                // Notifications toggle (Uses local state - no change needed here for theme updates)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(id = R.string.notifications),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = notificationsEnabled.value,
                        onCheckedChange = { notificationsEnabled.value = it }
                    )
                }

                // Language selection (Uses local state - no change needed here for theme updates)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(id = R.string.language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // English Option Column... (using local `language` state)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { language.value = "English" }.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .border(
                                        width = if (language.value == "English") 2.dp else 1.dp,
                                        color = if (language.value == "English") MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .background(if (language.value == "English") MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) { Text("EN") }
                            Text(
                                text = stringResource(id = R.string.english), modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (language.value == "English") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Estonian Option Column... (using local `language` state)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { language.value = "Estonian" }.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .border(
                                        width = if (language.value == "Estonian") 2.dp else 1.dp,
                                        color = if (language.value == "Estonian") MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .background(if (language.value == "Estonian") MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) { Text("ET") }
                            Text(
                                text = stringResource(id = R.string.estonian), modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (language.value == "Estonian") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Space before version

                // Version Text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Version 1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

            } // End Content Column
        } // End Box
    } // End Surface
}

// --- DefaultBackgroundOption and BackgroundImageOption composables remain unchanged ---
// (They receive `isSelected` as a parameter, which is now derived from the collected state)
@Composable
fun DefaultBackgroundOption(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(Color.White), // Default background is White
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Subtle overlay
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected), // Use string resource
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

@Composable
fun BackgroundImageOption(
    imageResId: Int,
    name: String,
    choice: BackgroundChoice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = name, // Use name for content desc of the image itself
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isSelected) {
                Box( // Overlay for checkmark visibility
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected), // Use string resource
                    tint = Color.White, // White checkmark stands out
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}


// --- Preview (Adjusted for global state dependency) ---
@Preview(showBackground = true, name = "Settings Preview")
@Composable
fun SettingsScreenPreview() {
    // Previewing components that rely on global StateFlow can be tricky.
    // This basic preview might not reflect runtime behavior perfectly.
    // Using LaunchedEffect to set an initial state for the preview run.
    LaunchedEffect(Unit) {
        AppThemeSettings.updateThemeSettings(ThemeSettings(backgroundChoice = BackgroundChoice.BUTTERFLY)) // Example initial state for preview
    }
    val currentSettings by AppThemeSettings.themeSettings.collectAsState()
    val dummyViewModel: SettingsViewModel = viewModel() // Use viewModel() or a mock/fake

    // It's good practice to wrap previews in your app's theme if applicable
    // YourAppTheme(darkTheme = currentSettings.darkModeEnabled) {
    SettingsScreen(
        onNavigateBack = {},
        themeSettings = currentSettings, // Pass the collected state
        settingsViewModel = dummyViewModel
    )
    // }

    // Optional: Reset state after preview if needed (less common for previews)
    // DisposableEffect(Unit) { onDispose { AppThemeSettings.updateThemeSettings(ThemeSettings()) } }
}
