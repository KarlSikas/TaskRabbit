package com.example.taskrabbit.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Corrected import
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import android.app.Application
import androidx.annotation.DrawableRes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import com.example.taskrabbit.ui.theme.ThemeSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import android.content.Context
import android.content.Intent
import com.example.taskrabbit.MainActivity
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility // Added for conditional text

private const val LANG_EN = "en"
private const val LANG_ET = "et"

private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.POST_NOTIFICATIONS
} else {
    ""
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    val currentLanguage by settingsViewModel.currentLanguagePreference.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notificationsEnabled = remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        notificationsEnabled.value = isGranted
        if (isGranted) {
            Log.d("SettingsScreen", "Notification permission GRANTED.")
            // Persist preference if needed: settingsViewModel.updateNotificationPreference(true)
        } else {
            Log.w("SettingsScreen", "Notification permission DENIED.")
            // Persist preference if needed: settingsViewModel.updateNotificationPreference(false)
        }
    }

    LaunchedEffect(key1 = context) {
        if (notificationPermission.isNotEmpty()) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                notificationPermission
            ) == PackageManager.PERMISSION_GRANTED
            notificationsEnabled.value = isGranted
        } else {
            // Load preference from ViewModel if persisted
        }
    }

    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)

    // Condition for enabling Dark Mode
    val isDarkModeAllowed = currentThemeSettings.backgroundChoice == BackgroundChoice.WHITE

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    stringResource(id = R.string.background_images),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                // --- Background Options Row 1 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DefaultBackgroundOption(
                        name = stringResource(id = R.string.default_option),
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.WHITE,
                        onClick = {
                            // Selecting default does NOT force dark mode off
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.WHITE))
                        }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_butterfly, name = stringResource(id = R.string.butterfly), choice = BackgroundChoice.BUTTERFLY,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.BUTTERFLY,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.BUTTERFLY, darkModeEnabled = false))
                        }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_colorful, name = stringResource(id = R.string.colorful), choice = BackgroundChoice.COLORFUL,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.COLORFUL,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.COLORFUL, darkModeEnabled = false))
                        }
                    )
                }
                // --- Background Options Row 2 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_cute, name = stringResource(id = R.string.cute), choice = BackgroundChoice.CUTE,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.CUTE,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.CUTE, darkModeEnabled = false))
                        }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_flowers, name = stringResource(id = R.string.flowers), choice = BackgroundChoice.FLOWERS,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.FLOWERS,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.FLOWERS, darkModeEnabled = false))
                        }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_rainbow, name = stringResource(id = R.string.rainbow), choice = BackgroundChoice.RAINBOW,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.RAINBOW,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.RAINBOW, darkModeEnabled = false))
                        }
                    )
                }
                // --- Background Options Row 3 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_shooting_star, name = stringResource(id = R.string.stars), choice = BackgroundChoice.SHOOTING_STAR,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SHOOTING_STAR,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SHOOTING_STAR, darkModeEnabled = false))
                        }
                    )
                    BackgroundImageOption(
                        imageResId = R.drawable.bg_skeleton_head, name = stringResource(id = R.string.skull), choice = BackgroundChoice.SKELETON_HEAD,
                        isSelected = currentThemeSettings.backgroundChoice == BackgroundChoice.SKELETON_HEAD,
                        onClick = {
                            // *** FIXED: Set darkModeEnabled to false when selecting this background ***
                            settingsViewModel.updateThemeSettings(currentThemeSettings.copy(backgroundChoice = BackgroundChoice.SKELETON_HEAD, darkModeEnabled = false))
                        }
                    )
                    Spacer(modifier = Modifier.width(70.dp)) // Keep spacer for layout
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Dark mode toggle (logic inside remains the same) ---
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(id = R.string.dark_mode),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = currentThemeSettings.darkModeEnabled,
                            enabled = isDarkModeAllowed, // Enable based on condition
                            onCheckedChange = { isChecked ->
                                // Only update if allowed
                                if (isDarkModeAllowed) {
                                    settingsViewModel.updateThemeSettings(currentThemeSettings.copy(darkModeEnabled = isChecked))
                                }
                            }
                        )
                    }
                    AnimatedVisibility(visible = !isDarkModeAllowed) {
                        Text(
                            text = stringResource(R.string.dark_mode_requires_default_background),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 8.dp)
                        )
                    }
                }
                // --- End Dark mode toggle ---


                // --- Notifications toggle (logic remains the same) ---
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
                                if (notificationPermission.isNotEmpty()) {
                                    when (ContextCompat.checkSelfPermission(context, notificationPermission)) {
                                        PackageManager.PERMISSION_GRANTED -> {
                                            notificationsEnabled.value = true
                                            // Persist via ViewModel if needed
                                        }
                                        else -> {
                                            notificationPermissionLauncher.launch(notificationPermission)
                                        }
                                    }
                                } else {
                                    notificationsEnabled.value = true
                                    // Persist via ViewModel if needed
                                }
                            } else {
                                notificationsEnabled.value = false
                                // Persist via ViewModel if needed
                            }
                        }
                    )
                }
                // --- End Notifications toggle ---

                // --- Language selection (logic remains the same) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageOption(
                            languageCode = LANG_EN,
                            languageNameResId = R.string.english,
                            isSelected = currentLanguage == LANG_EN,
                            onClick = {
                                if (currentLanguage != LANG_EN) {
                                    scope.launch {
                                        settingsViewModel.updateLanguage(LANG_EN)
                                        restartApp(context)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        LanguageOption(
                            languageCode = LANG_ET,
                            languageNameResId = R.string.estonian,
                            isSelected = currentLanguage == LANG_ET,
                            onClick = {
                                if (currentLanguage != LANG_ET) {
                                    scope.launch {
                                        settingsViewModel.updateLanguage(LANG_ET)
                                        restartApp(context)
                                    }
                                }
                            }
                        )
                    }
                }
                // --- End Language selection ---

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.app_version, "1.0"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

            }
        }
    }
}

// --- Helper composables and functions (remain unchanged) ---

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
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected),
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
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected),
                    tint = Color.White,
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

// --- Preview (remains unchanged) ---
@Preview(showBackground = true, name = "Settings Preview Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Settings Preview Dark")
@Composable
private fun SettingsScreenPreview() {
    class FakeSettingsStateHolder {
        private val _themeSettings = MutableStateFlow(ThemeSettings(darkModeEnabled = false, backgroundChoice = BackgroundChoice.BUTTERFLY))
        val themeSettings: StateFlow<ThemeSettings> = _themeSettings
        private val _currentLanguagePreference = MutableStateFlow(LANG_EN)
        val currentLanguagePreference: StateFlow<String> = _currentLanguagePreference
        fun updateThemeSettings(newSettings: ThemeSettings) {
            _themeSettings.value = newSettings
            AppThemeSettings.updateThemeSettings(newSettings)
        }
        suspend fun updateLanguage(languageCode: String) { _currentLanguagePreference.value = languageCode }
    }
    val fakeState = remember { FakeSettingsStateHolder() }
    val context = LocalContext.current
    val previewViewModel = remember {
        object : SettingsViewModel(context.applicationContext as Application) {
            override val currentLanguagePreference: StateFlow<String> get() = fakeState.currentLanguagePreference
            override fun updateThemeSettings(newSettings: ThemeSettings) { fakeState.updateThemeSettings(newSettings) }
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