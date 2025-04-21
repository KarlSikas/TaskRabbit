package com.example.taskrabbit.ui.screens

import android.Manifest // <<< Import Manifest
import android.annotation.SuppressLint
import androidx.annotation.StringRes // Added for LanguageOption
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
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Keep this import
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
import com.example.taskrabbit.R
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.viewmodel.SettingsViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskrabbit.ui.theme.AppThemeSettings
import android.app.Application // Added for Preview
import androidx.annotation.DrawableRes // Added for BackgroundImageOption
import androidx.compose.material3.darkColorScheme // Added for Preview
import androidx.compose.material3.lightColorScheme // Added for Preview
import kotlinx.coroutines.flow.MutableStateFlow // Added for Preview
import kotlinx.coroutines.flow.StateFlow // Added for Preview
import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import com.example.taskrabbit.ui.theme.ThemeSettings // Keep for Preview state holder

// --- ADD Permission Handling Imports ---
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
// --- END Permission Handling Imports ---

// --- ADDED Imports for Restart Logic ---
import android.content.Context
import android.content.Intent
import com.example.taskrabbit.MainActivity
// --- END ADDED Imports ---

// --- ADDED Coroutine Scope Import ---
import kotlinx.coroutines.launch
// --- END ---


// Define language codes as constants
private const val LANG_EN = "en"
private const val LANG_ET = "et"

// Define permission constant (needed for Android 13+)
private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.POST_NOTIFICATIONS
} else {
    "" // Indicate no specific permission needed for older versions
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    Log.d("SettingsScreen", "SettingsScreen composable CALLED")

    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) { /* ... Locale Logging ... */ }

    // Collect theme state from global settings
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()

    // Collect language state from the ViewModel
    val currentLanguage by settingsViewModel.currentLanguagePreference.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current // Needed for restarting and other context uses

    // --- Coroutine Scope for suspend functions ---
    val scope = rememberCoroutineScope()
    // --- END ---

    // --- State for Notifications Toggle ---
    val notificationsEnabled = remember { mutableStateOf(false) }
    // --- END State ---

    // --- Permission Launcher for Notifications ---
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            notificationsEnabled.value = true
            Log.d("SettingsScreen", "Notification permission GRANTED.")
            // TODO: Persist this preference via ViewModel if needed
        } else {
            notificationsEnabled.value = false
            Log.w("SettingsScreen", "Notification permission DENIED.")
            // TODO: Show rationale or guide user to settings if needed
            // TODO: Persist this preference via ViewModel if needed
        }
    }
    // --- END Permission Launcher ---

    // --- Effect to Check Initial Notification Permission Status ---
    LaunchedEffect(key1 = context) {
        if (notificationPermission.isNotEmpty()) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                notificationPermission
            ) == PackageManager.PERMISSION_GRANTED
            notificationsEnabled.value = isGranted
            Log.d("SettingsScreen", "Initial notification permission check: isGranted=$isGranted")
        } else {
            Log.d("SettingsScreen", "Skipping initial notification permission check (pre-Android 13). Loading preference instead.")
            // TODO: Load preference from ViewModel if persisted
        }
    }
    // --- END Initial Check Effect ---


    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundPainter != null) {
                Image(
                    painter = backgroundPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // --- Top bar (Keep as is) ---
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
                        text = stringResource(id = R.string.settings).also {
                            Log.d("SettingsScreen", "Looked up R.string.settings: '$it'")
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Background image choices (Keep as is) ---
                Text(
                    stringResource(id = R.string.background_images),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DefaultBackgroundOption(
                        name = stringResource(id = R.string.default_option),
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.WHITE,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.WHITE)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_butterfly, name = stringResource(id = R.string.butterfly), choice = BackgroundChoice.BUTTERFLY,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.BUTTERFLY,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.BUTTERFLY)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_colorful, name = stringResource(id = R.string.colorful), choice = BackgroundChoice.COLORFUL,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.COLORFUL,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.COLORFUL)) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_cute, name = stringResource(id = R.string.cute), choice = BackgroundChoice.CUTE,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.CUTE,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.CUTE)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_flowers, name = stringResource(id = R.string.flowers), choice = BackgroundChoice.FLOWERS,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.FLOWERS,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.FLOWERS)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_rainbow, name = stringResource(id = R.string.rainbow), choice = BackgroundChoice.RAINBOW,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.RAINBOW,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.RAINBOW)) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_shooting_star, name = stringResource(id = R.string.stars), choice = BackgroundChoice.SHOOTING_STAR,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SHOOTING_STAR,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SHOOTING_STAR)) }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_skeleton_head, name = stringResource(id = R.string.skull), choice = BackgroundChoice.SKELETON_HEAD,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SKELETON_HEAD,
                        onClick = { settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SKELETON_HEAD)) }
                    )
                    Spacer(modifier = Modifier.width(70.dp)) // Consider making this more robust if needed
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Dark mode toggle (Keep as is) ---
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
                        checked = currentThemeSettings.darkModeEnabled,
                        onCheckedChange = { isChecked ->
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(darkModeEnabled = isChecked))
                        }
                    )
                }

                // --- Notifications toggle (Keep as is) ---
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
                        onCheckedChange = { shouldBeEnabled ->
                            if (shouldBeEnabled) {
                                // --- Turning ON ---
                                if (notificationPermission.isNotEmpty()) { // Check needed only on Android 13+
                                    when (ContextCompat.checkSelfPermission(context, notificationPermission)) {
                                        PackageManager.PERMISSION_GRANTED -> {
                                            Log.d("SettingsScreen", "Notification toggle ON: Permission already granted.")
                                            notificationsEnabled.value = true
                                            // TODO: Persist via ViewModel: settingsViewModel.updateNotificationPreference(true)
                                        }
                                        else -> {
                                            Log.d("SettingsScreen", "Notification toggle ON: Permission needed, launching request.")
                                            notificationPermissionLauncher.launch(notificationPermission)
                                        }
                                    }
                                } else {
                                    Log.d("SettingsScreen", "Notification toggle ON: Pre-Android 13, enabling directly.")
                                    notificationsEnabled.value = true
                                    // TODO: Persist via ViewModel: settingsViewModel.updateNotificationPreference(true)
                                }
                            } else {
                                // --- Turning OFF ---
                                Log.d("SettingsScreen", "Notification toggle OFF.")
                                notificationsEnabled.value = false
                                // TODO: Persist via ViewModel: settingsViewModel.updateNotificationPreference(false)
                            }
                        }
                    )
                }
                // --- END Notifications toggle ---

                // --- *** MODIFIED Language selection BLOCK (Use Coroutine Scope) *** ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.language).also {
                            Log.d("SettingsScreen", "Looked up R.string.language: '$it'")
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageOption(
                            languageCode = LANG_EN,
                            languageNameResId = R.string.english,
                            isSelected = currentLanguage == LANG_EN,
                            onClick = {
                                // Only act if the language is actually changing
                                if (currentLanguage != LANG_EN) {
                                    Log.d("SettingsScreen", "English selected. Launching coroutine to update and restart.")
                                    // --- Use Coroutine Scope ---
                                    scope.launch {
                                        // 1. Await ViewModel update (suspend function)
                                        settingsViewModel.updateLanguage(LANG_EN)
                                        // 2. Trigger restart AFTER update completes
                                        Log.d("SettingsScreen", "Language update complete, now restarting.")
                                        restartApp(context) // <<< RESTART CALL MOVED INSIDE COROUTINE
                                    }
                                } else {
                                    Log.d("SettingsScreen", "English already selected. No action needed.")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        LanguageOption(
                            languageCode = LANG_ET,
                            languageNameResId = R.string.estonian,
                            isSelected = currentLanguage == LANG_ET,
                            onClick = {
                                // Only act if the language is actually changing
                                if (currentLanguage != LANG_ET) {
                                    Log.d("SettingsScreen", "Estonian selected. Launching coroutine to update and restart.")
                                    // --- Use Coroutine Scope ---
                                    scope.launch {
                                        // 1. Await ViewModel update (suspend function)
                                        settingsViewModel.updateLanguage(LANG_ET)
                                        // 2. Trigger restart AFTER update completes
                                        Log.d("SettingsScreen", "Language update complete, now restarting.")
                                        restartApp(context) // <<< RESTART CALL MOVED INSIDE COROUTINE
                                    }
                                } else {
                                    Log.d("SettingsScreen", "Estonian already selected. No action needed.")
                                }
                            }
                        )
                    }
                }
                // --- *** END of MODIFIED Language selection BLOCK *** ---


                Spacer(modifier = Modifier.height(24.dp))

                // --- Version Text (Keep as is) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Version 1.0", // Consider making this a string resource if needed
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

            } // End Content Column
        } // End Box
    } // End Surface
}

// --- Helper Function to Restart MainActivity (Unchanged) ---
private fun restartApp(context: Context) {
    Log.i("SettingsScreen", "Restarting MainActivity to apply locale change.")
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
    if (context is android.app.Activity) {
        context.finishAffinity()
    }
}
// --- END Helper Function ---


// --- LanguageOption Composable (Unchanged) ---
@Composable
private fun LanguageOption(
    languageCode: String,
    @StringRes languageNameResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayCode = languageCode.uppercase()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = CircleShape
                )
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(displayCode, fontWeight = FontWeight.Bold)
        }
        Text(
            text = stringResource(id = languageNameResId),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.8f)
        )
    }
}


// --- DefaultBackgroundOption Composable (Unchanged) ---
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
                .background(Color.White), // Explicitly White for default
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Selection overlay
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected), // Accessibility
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray // Match selection state
        )
    }
}

// --- BackgroundImageOption Composable (Unchanged) ---
@Composable
fun BackgroundImageOption(
    @DrawableRes imageResId: Int,
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
                contentDescription = name, // Use name for accessibility
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isSelected) {
                Box( // Selection overlay
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)) // Semi-transparent overlay
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected), // Accessibility
                    tint = Color.White, // Checkmark visible on images
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray // Match selection state
        )
    }
}


// --- Preview (ViewModel interaction needs update for suspend fun) ---
@Preview(showBackground = true, name = "Settings Preview Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Settings Preview Dark")
@Composable
private fun SettingsScreenPreview() {
    // Fake state holder and ViewModel setup
    class FakeSettingsStateHolder {
        private val _themeSettings = MutableStateFlow(ThemeSettings(darkModeEnabled = false, backgroundChoice = BackgroundChoice.BUTTERFLY))
        val themeSettings: StateFlow<ThemeSettings> = _themeSettings
        private val _currentLanguagePreference = MutableStateFlow(LANG_EN)
        val currentLanguagePreference: StateFlow<String> = _currentLanguagePreference
        fun updateThemeSettings(newSettings: ThemeSettings) {
            _themeSettings.value = newSettings
            AppThemeSettings.updateThemeSettings(newSettings)
        }
        // Fake suspend fun
        suspend fun updateLanguage(languageCode: String) { _currentLanguagePreference.value = languageCode }
    }
    val fakeState = remember { FakeSettingsStateHolder() }
    val context = LocalContext.current
    val previewViewModel = remember {
        object : SettingsViewModel(context.applicationContext as Application) {
            override val currentLanguagePreference: StateFlow<String> get() = fakeState.currentLanguagePreference
            override fun updateThemeSettings(newSettings: ThemeSettings) { fakeState.updateThemeSettings(newSettings) }
            // Override the suspend function
            override suspend fun updateLanguage(languageCode: String) { fakeState.updateLanguage(languageCode) }
        }
    }
    val currentSettings by AppThemeSettings.themeSettings.collectAsState()

    MaterialTheme(
        colorScheme = if (currentSettings.darkModeEnabled) darkColorScheme() else lightColorScheme()
    ) {
        SettingsScreen(
            onNavigateBack = {},
            settingsViewModel = previewViewModel
        )
    }
}