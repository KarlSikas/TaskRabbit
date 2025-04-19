package com.example.taskrabbit.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.taskrabbit.R
import com.example.taskrabbit.data.BackgroundImage
import com.example.taskrabbit.viewmodel.BackgroundViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Renamed from BackgroundSelectorScreen to CalendarBackgroundSelector
fun CalendarBackgroundSelector(
    onNavigateBack: () -> Unit,
    backgroundViewModel: BackgroundViewModel = viewModel()
) {
    val freeBackgrounds by backgroundViewModel.freeBackgrounds.collectAsState()
    val premiumBackgrounds by backgroundViewModel.premiumBackgrounds.collectAsState()
    val userBackgrounds by backgroundViewModel.userBackgrounds.collectAsState()
    val selectedBackground by backgroundViewModel.selectedBackground.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar - Navigation
        CenterAlignedTopAppBar(
            // FIXED: Use the existing string resource instead of a non-existent one
            title = { Text(text = stringResource(R.string.select_background)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Background Categories Tabs
        CalendarBackgroundCategoryTabs(
            onSelectCategory = { category ->
                // Handle category selection if needed
            }
        )

        // Image Grid - Display Backgrounds
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            // Free backgrounds
            items(freeBackgrounds) { background ->
                CalendarBackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    onSelectBackground = { backgroundViewModel.selectBackground(background) }
                )
            }

            // Premium backgrounds
            items(premiumBackgrounds) { background ->
                CalendarBackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    isPremium = true,
                    onSelectBackground = { backgroundViewModel.selectBackground(background) }
                )
            }

            // User backgrounds
            items(userBackgrounds) { background ->
                CalendarBackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    onSelectBackground = { backgroundViewModel.selectBackground(background) }
                )
            }

            // Option to add image from gallery
            item {
                CalendarAddFromGalleryItem(
                    onImageSelected = { uri -> backgroundViewModel.addBackgroundFromUri(uri) }
                )
            }
        }
    }
}

@Composable
// Renamed from BackgroundCategoryTabs to CalendarBackgroundCategoryTabs
fun CalendarBackgroundCategoryTabs(
    onSelectCategory: (String) -> Unit
) {
    val categories = listOf("All", "Free", "Premium", "My Photos")
    var selectedCategory by remember { mutableStateOf(0) }

    TabRow(selectedTabIndex = selectedCategory) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = selectedCategory == index,
                onClick = {
                    selectedCategory = index
                    onSelectCategory(category)
                }
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
// Renamed from BackgroundItem to CalendarBackgroundItem
fun CalendarBackgroundItem(
    background: BackgroundImage,
    isSelected: Boolean,
    isPremium: Boolean = false,
    onSelectBackground: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(0.75f)
            .clip(MaterialTheme.shapes.medium)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onSelectBackground)
    ) {
        // Background Image
        if (background.isAsset) {
            Image(
                painter = painterResource(id = background.assetResId!!),
                contentDescription = background.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(background.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = background.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Premium Badge
        if (isPremium) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Premium",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Title Overlay at the Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            Text(
                text = background.name,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
// Renamed from AddFromGalleryItem to CalendarAddFromGalleryItem
fun CalendarAddFromGalleryItem(
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(0.75f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { galleryLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Add from gallery",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.add_from_gallery),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}