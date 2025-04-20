package com.example.taskrabbit.ui.screens

// Keep existing imports
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
// --- ADD Settings Icon Import ---
import androidx.compose.material.icons.filled.Settings // Imported Settings icon
// --- END Settings Icon Import ---
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.R
import com.example.taskrabbit.ui.theme.AppThemeSettings
import com.example.taskrabbit.viewmodel.CalendarViewModel
import com.example.taskrabbit.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

private const val DATE_ITEM_CONTENT_TYPE = "DateItemType"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    // --- ADD Navigation Lambda for Settings ---
    onNavigateToSettings: () -> Unit, // Added parameter for navigation action
    // --- END Navigation Lambda ---
    calendarViewModel: CalendarViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel() // Keep taskViewModel if DateItem needs it
) {
    val context = LocalContext.current

    // --- Theme State (Keep as is) ---
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)

    // --- Calendar State (Keep as is) ---
    val originalDates: List<LocalDate?> by calendarViewModel.calendarGridDates.observeAsState(initial = emptyList())
    val reversedDates by remember(originalDates) {
        derivedStateOf { originalDates.filterNotNull().reversed() }
    }
    val today = remember { LocalDate.now() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var visuallySelectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // --- Effect to Scroll (Keep as is) ---
    LaunchedEffect(reversedDates) {
        if (reversedDates.isNotEmpty()) {
            val todayIndex = reversedDates.indexOfFirst { it.isEqual(today) }
            if (todayIndex != -1) {
                listState.scrollToItem(index = todayIndex)
                Log.d("CalendarScreen", "Scrolled to today's index: $todayIndex")
            } else {
                Log.d("CalendarScreen", "Today not found in the current date list.")
            }
        }
    }
    // --- END Effect ---

    // --- UI Structure ---
    Box( modifier = Modifier.fillMaxSize().background(if (backgroundPainter == null) backgroundColor else Color.Transparent) ) {
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = stringResource(R.string.background_image_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // --- TopAppBar (MODIFIED) ---
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.calendar)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                // --- ADD Settings Icon Button to Actions ---
                actions = {
                    IconButton(onClick = onNavigateToSettings) { // Added IconButton calling the new lambda
                        Icon(
                            imageVector = Icons.Default.Settings, // Used Settings icon
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                },
                // --- END Actions ---
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    // --- ADD Color for Action Icon ---
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface // Ensured action icon color is set
                    // --- END Color ---
                )
            )
            // --- END TopAppBar ---

            // --- LazyColumn for Dates (Keep as is) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                state = listState,
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = reversedDates,
                    key = { _, date -> date.toEpochDay() },
                    contentType = { _, _ -> DATE_ITEM_CONTENT_TYPE }
                ) { _, date ->
                    val isSelected = visuallySelectedDate?.isEqual(date) ?: false
                    val isTodayDate = today.isEqual(date)

                    DateItem(
                        date = date,
                        isToday = isTodayDate,
                        isSelected = isSelected,
                        taskViewModel = taskViewModel, // Pass down TaskViewModel
                        onClick = {
                            visuallySelectedDate = date
                            onDateSelected(date)
                        }
                    )
                }
            } // End LazyColumn
        } // End Column
    } // End Box
}

// --- DateItem Composable (Keep as is) ---
@Composable
fun DateItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    taskViewModel: TaskViewModel, // Receive TaskViewModel
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d") }

    val itemBackgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Query ViewModel for priority status
    val hasPriority by taskViewModel.hasPriorityTaskOnDate(date)
        .collectAsState(initial = false)

    // Query ViewModel for priority task titles
    val priorityTitles by taskViewModel.getPriorityTaskTitlesOnDate(date)
        .collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(itemBackgroundColor)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Date Text
            val dateText = when {
                isToday -> stringResource(R.string.today) + ", " + date.format(shortDateFormatter)
                else -> date.format(dateFormatter)
            }
            Text(
                text = dateText,
                color = textColor,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )

            // Priority Indicator and Task Titles
            if (hasPriority && priorityTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.priority),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Column(modifier = Modifier.padding(start = 0.dp)) {
                    priorityTitles.take(3).forEach { title ->
                        Text(
                            text = "- $title",
                            color = secondaryTextColor,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (priorityTitles.size > 3) {
                        Text(
                            text = "...",
                            color = secondaryTextColor.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}