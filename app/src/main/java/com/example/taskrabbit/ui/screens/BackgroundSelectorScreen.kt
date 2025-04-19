package com.example.taskrabbit.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.filled.ArrowBack
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
import android.app.Application
import androidx.compose.material.icons.filled.PhotoCamera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundSelectorScreen(
    onNavigateBack: () -> Unit,
    onBackgroundSelected: (BackgroundImage) -> Unit, // Added this parameter
    backgroundViewModel: BackgroundViewModel = viewModel(factory = BackgroundViewModel.Factory(LocalContext.current.applicationContext as Application))
) {
    val freeBackgrounds by backgroundViewModel.freeBackgrounds.collectAsState()
    val premiumBackgrounds by backgroundViewModel.premiumBackgrounds.collectAsState()
    val userBackgrounds by backgroundViewModel.userBackgrounds.collectAsState()
    val selectedBackground by backgroundViewModel.selectedBackground.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.select_background)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        BackgroundCategoryTabs(
            onSelectCategory = { category -> }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(freeBackgrounds) { background ->
                BackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    onSelectBackground = {
                        backgroundViewModel.selectBackground(background)
                        onBackgroundSelected(background) // Notify the parent of the selected background
                    }
                )
            }

            items(premiumBackgrounds) { background ->
                BackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    isPremium = true,
                    onSelectBackground = {
                        backgroundViewModel.selectBackground(background)
                        onBackgroundSelected(background) // Notify the parent of the selected background
                    }
                )
            }

            items(userBackgrounds) { background ->
                BackgroundItem(
                    background = background,
                    isSelected = selectedBackground?.id == background.id,
                    onSelectBackground = {
                        backgroundViewModel.selectBackground(background)
                        onBackgroundSelected(background) // Notify the parent of the selected background
                    }
                )
            }

            item {
                AddFromGalleryItem(
                    onImageSelected = { uri ->
                        val newBackground = backgroundViewModel.addBackgroundFromUri(uri)
                        newBackground?.let {
                            onBackgroundSelected(it) // Notify the parent of the new background
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BackgroundCategoryTabs(
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
fun BackgroundItem(
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
fun AddFromGalleryItem(
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
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = stringResource(id = R.string.add_from_gallery),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.add_from_gallery),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}