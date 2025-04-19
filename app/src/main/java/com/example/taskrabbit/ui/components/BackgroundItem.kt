package com.example.taskrabbit.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.padding

// Import BackgroundImage class
import com.example.taskrabbit.data.BackgroundImage

@Composable
fun BackgroundItem(
    background: BackgroundImage,
    isSelected: Boolean,
    isPremium: Boolean = false,
    onSelectBackground: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectBackground), // More efficient click handling
        shape = MaterialTheme.shapes.medium,
        // Fix: Use cardElevation instead of elevation
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.size(180.dp)) {
            // Display the background image
            if (background.isAsset) {
                // Using resource ID - simplified modifier usage
                Image(
                    painter = painterResource(id = background.assetResId ?: 0),
                    contentDescription = background.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Using AsyncImage instead of rememberAsyncImagePainter for better performance
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(background.uri))
                        .crossfade(true)
                        .build(),
                    contentDescription = background.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            // Display a selection indicator (checkbox) if the item is selected
            if (isSelected) {
                Checkbox(
                    checked = true,
                    onCheckedChange = null, // No action, just visually indicating selected
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // Optionally display a "Premium" label
            if (isPremium) {
                Text(
                    text = "Premium",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp), // Padding for label
                    color = Color.White
                )
            }
        }
    }
}