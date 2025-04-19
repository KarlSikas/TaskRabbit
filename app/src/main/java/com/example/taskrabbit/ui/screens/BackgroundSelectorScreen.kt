package com.example.taskrabbit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskrabbit.R
import com.example.taskrabbit.data.BackgroundImage
import com.example.taskrabbit.viewmodel.BackgroundViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
// Import shared components
import com.example.taskrabbit.ui.components.BackgroundCategoryTabs
// Change this to match your actual component name
import com.example.taskrabbit.ui.components.BackgroundItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundSelectorScreen(
    onNavigateBack: () -> Unit,
    backgroundViewModel: BackgroundViewModel = viewModel()
) {
    // Collecting states from the ViewModel
    val freeBackgrounds by backgroundViewModel.freeBackgrounds.collectAsState()
    val premiumBackgrounds by backgroundViewModel.premiumBackgrounds.collectAsState()
    val userBackgrounds by backgroundViewModel.userBackgrounds.collectAsState()
    val selectedBackground by backgroundViewModel.selectedBackground.collectAsState()

    // State to track selected category (for filtering)
    var selectedCategory by remember { mutableStateOf("all") }

    // Function to filter backgrounds based on selected category
    val filteredBackgrounds: List<BackgroundImage> = when (selectedCategory) {
        "free" -> freeBackgrounds
        "premium" -> premiumBackgrounds
        "user" -> userBackgrounds
        else -> freeBackgrounds + premiumBackgrounds + userBackgrounds
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with back navigation
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(R.string.select_background)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Background category tabs to switch between free, premium, and user backgrounds
        BackgroundCategoryTabs(
            onSelectCategory = { category ->
                selectedCategory = category
            }
        )

        // Background Gallery - Grid layout to display backgrounds
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Explicitly typing the items collection
            items(
                items = filteredBackgrounds,
                key = { background -> background.id } // Adding key for better performance
            ) { background ->
                // Change this to match your actual component name
                BackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    isPremium = selectedCategory == "premium" ||
                            (selectedCategory == "all" && background in premiumBackgrounds),
                    onSelectBackground = { backgroundViewModel.selectBackground(background) }
                )
            }
        }
    }
}