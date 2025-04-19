package com.example.taskrabbit.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.taskrabbit.R
import com.example.taskrabbit.viewmodel.CalendarViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.ui.res.painterResource
import com.example.taskrabbit.viewmodel.SettingsViewModel // Corrected import
import com.example.taskrabbit.ui.theme.BackgroundChoice
import androidx.compose.ui.draw.paint // Import paint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dates by calendarViewModel.dates.observeAsState(emptyList())
    val listState = rememberLazyListState()

    // Get the SettingsViewModel to access the appState
    val settingsViewModel: SettingsViewModel = viewModel()
    val appState by settingsViewModel.appState.collectAsState()

    LaunchedEffect(dates) {
        val todayIndex = (dates as List<LocalDate>).indexOf(today)
        if (todayIndex >= 0) {
            listState.scrollToItem(todayIndex)
        }
    }

    // Function to get the background resource ID based on the selected choice
    fun getBackgroundResource(backgroundChoice: BackgroundChoice): Int? {
        return when (backgroundChoice) {
            BackgroundChoice.BUTTERFLY -> R.drawable.bg_butterfly
            BackgroundChoice.COLORFUL -> R.drawable.bg_colorful
            BackgroundChoice.CUTE -> R.drawable.bg_cute
            BackgroundChoice.FLOWERS -> R.drawable.bg_flowers
            BackgroundChoice.RAINBOW -> R.drawable.bg_rainbow
            BackgroundChoice.SHOOTING_STAR -> R.drawable.bg_shooting_star
            BackgroundChoice.SKELETON_HEAD -> R.drawable.bg_skeleton_head
            BackgroundChoice.WHITE -> null // No background for white
        }
    }

    val backgroundResource = getBackgroundResource(appState.backgroundChoice)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (backgroundResource != null) {
                    Modifier.paint(painterResource(id = backgroundResource))
                } else {
                    Modifier.background(Color.White)
                }
            )
    ) {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(R.string.calendar)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(dates) { _, date ->
                DateItem(
                    date = date,
                    isToday = date == today,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
fun DateItem(
    date: LocalDate,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    val backgroundColor = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
    val textColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = date.format(dateFormatter),
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}