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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.BuildConfig
import com.example.taskrabbit.R
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.taskrabbit.data.BackgroundImage // Import BackgroundImage
import com.example.taskrabbit.ui.components.BackgroundItem // Import BackgroundItem

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val appState by settingsViewModel.appState.collectAsState()

    // List of custom background images (replace with your actual data)
    val customBackgrounds = remember {
        mutableStateListOf(
            BackgroundImage(
                id = 1,
                name = "Custom 1",
                uri = "https://example.com/custom1.jpg", // Replace with your image URI
                isAsset = false
            ),
            BackgroundImage(
                id = 2,
                name = "Custom 2",
                uri = "https://example.com/custom2.jpg", // Replace with your image URI
                isAsset = false
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color(0x80FFFFFF)) // Semi-transparent background
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState) // Make the screen scrollable
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
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

            // First row with Default (White) and Butterfly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Add Default (White) option without requiring an image
                DefaultBackgroundOption(
                    name = stringResource(id = R.string.default_option),
                    isSelected = appState.backgroundChoice == BackgroundChoice.WHITE,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.WHITE) }
                )

                // Butterfly option
                BackgroundImageOption(
                    imageResId = R.drawable.bg_butterfly,
                    name = stringResource(id = R.string.butterfly),
                    choice = BackgroundChoice.BUTTERFLY,
                    isSelected = appState.backgroundChoice == BackgroundChoice.BUTTERFLY,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.BUTTERFLY) }
                )

                // Colorful option
                BackgroundImageOption(
                    imageResId = R.drawable.bg_colorful,
                    name = stringResource(id = R.string.colorful),
                    choice = BackgroundChoice.COLORFUL,
                    isSelected = appState.backgroundChoice == BackgroundChoice.COLORFUL,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.COLORFUL) }
                )
            }

            // Second row of background images
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BackgroundImageOption(
                    imageResId = R.drawable.bg_cute,
                    name = stringResource(id = R.string.cute),
                    choice = BackgroundChoice.CUTE,
                    isSelected = appState.backgroundChoice == BackgroundChoice.CUTE,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.CUTE) }
                )

                BackgroundImageOption(
                    imageResId = R.drawable.bg_flowers,
                    name = stringResource(id = R.string.flowers),
                    choice = BackgroundChoice.FLOWERS,
                    isSelected = appState.backgroundChoice == BackgroundChoice.FLOWERS,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.FLOWERS) }
                )

                BackgroundImageOption(
                    imageResId = R.drawable.bg_rainbow,
                    name = stringResource(id = R.string.rainbow),
                    choice = BackgroundChoice.RAINBOW,
                    isSelected = appState.backgroundChoice == BackgroundChoice.RAINBOW,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.RAINBOW) }
                )
            }

            // Third row with Stars and Skeleton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BackgroundImageOption(
                    imageResId = R.drawable.bg_shooting_star,
                    name = stringResource(id = R.string.stars),
                    choice = BackgroundChoice.SHOOTING_STAR,
                    isSelected = appState.backgroundChoice == BackgroundChoice.SHOOTING_STAR,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.SHOOTING_STAR) }
                )

                BackgroundImageOption(
                    imageResId = R.drawable.bg_skeleton_head,
                    name = stringResource(id = R.string.skull),
                    choice = BackgroundChoice.SKELETON_HEAD,
                    isSelected = appState.backgroundChoice == BackgroundChoice.SKELETON_HEAD,
                    onClick = { settingsViewModel.setBackgroundChoice(BackgroundChoice.SKELETON_HEAD) }
                )

                // Empty box to keep the grid balanced
                Spacer(modifier = Modifier.width(70.dp))
            }

            // Custom Background Images
            Text(
                text = "Custom Backgrounds",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(customBackgrounds) { background ->
                    BackgroundItem(
                        background = background,
                        isSelected = false, // You'll need to implement selection logic
                        onSelectBackground = {
                            // Handle selection of custom background
                            // You'll need to create a new BackgroundChoice for custom images
                            // or find a way to store the selected image URI
                            println("Selected custom background: ${background.name}")
                        }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Notifications toggle
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
                    checked = appState.notificationsEnabled,
                    onCheckedChange = {
                        settingsViewModel.setNotificationsEnabled(it)
                    }
                )
            }

            // Language selection
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
                    // English option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                settingsViewModel.setLanguage("English")
                            }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (appState.language == "English") 2.dp else 1.dp,
                                    color = if (appState.language == "English")
                                        MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .background(if (appState.language == "English")
                                    MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EN")
                            if (appState.language == "English") {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.english),
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (appState.language == "English")
                                MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Estonian option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                settingsViewModel.setLanguage("Estonian")
                            }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (appState.language == "Estonian") 2.dp else 1.dp,
                                    color = if (appState.language == "Estonian")
                                        MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .background(if (appState.language == "Estonian")
                                    MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ET")
                            if (appState.language == "Estonian") {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.estonian),
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (appState.language == "Estonian")
                                MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }

            // Dark mode toggle
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
                    checked = appState.darkModeEnabled,
                    onCheckedChange = {
                        settingsViewModel.setDarkModeEnabled(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Version 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
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
                        .background(Color(0x66FFFFFF))
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
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
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66FFFFFF))
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Text(
            text = name,
            modifier = Modifier.padding(top = 4.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}