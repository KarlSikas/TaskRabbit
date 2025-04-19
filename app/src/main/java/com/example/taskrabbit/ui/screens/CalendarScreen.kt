package com.example.taskrabbit.ui.screens

import androidx.compose.foundation.Image // Import Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Import collectAsState extension
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import com.example.taskrabbit.R
import com.example.taskrabbit.viewmodel.CalendarViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
// Remove unused painterResource import
// import androidx.compose.ui.res.painterResource
import com.example.taskrabbit.viewmodel.SettingsViewModel // Keep if needed
import com.example.taskrabbit.ui.theme.AppThemeSettings // Import AppThemeSettings
// Remove unused BackgroundChoice import
// import com.example.taskrabbit.ui.theme.BackgroundChoice
// Remove unused paint import
// import androidx.compose.ui.draw.paint
// Remove unused ThemeSettings import
// import com.example.taskrabbit.ui.theme.ThemeSettings
import androidx.compose.ui.graphics.painter.Painter
// Remove unused Context import
// import android.content.Context


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    // Remove themeSettings parameter
    // themeSettings: ThemeSettings,
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val context = LocalContext.current

    // --- Collect Theme State ---
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    // --- End Theme State Collection ---

    // --- Get Background ---
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)
    // --- End Background ---

    val today = LocalDate.now()
    val dates by calendarViewModel.dates.observeAsState(emptyList())
    val listState = rememberLazyListState()

    // LaunchedEffect remains the same
    LaunchedEffect(dates) { /* ... */ }

    // --- Root Box for Background Layering ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Apply solid background color ONLY if there's no image
            .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
    ) {
        // Apply background image if one is selected (drawn under Column content)
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = null, // Decorative background
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Or FillBounds
            )
        }

        // --- Content Column on top of the background ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Make Column transparent so Box background (or Image) shows through
                .background(Color.Transparent)
        ) {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.calendar)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) // Use string resource
                    }
                },
                // Make TopAppBar transparent
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                    // Adjust title/icon colors if needed for contrast
                    // titleContentColor = ...,
                    // navigationIconContentColor = ...
                )
            )

            // LazyColumn for dates - remove background logic from its container
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize() // Takes remaining space in the Column
                    .padding(horizontal = 8.dp), // Add some horizontal padding if needed
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp), // Padding for top/bottom of list
                verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
            ) {
                itemsIndexed(dates) { _, date ->
                    DateItem(
                        date = date,
                        isToday = date == today,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        } // End Content Column
    } // End Root Box
}

@Composable
fun DateItem(
    date: LocalDate,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    // Use a semi-transparent background for the item itself for better readability over images
    val itemBackgroundColor = if (isToday) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) // More distinct for today
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // Subtle background for others
    }
    // Ensure text color contrasts well with the item background
    val textColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium) // Add rounded corners
            .clickable { onClick() }
            .background(itemBackgroundColor) // Apply semi-transparent background
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = date.format(dateFormatter),
            color = textColor, // Use contrast-checked color
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
