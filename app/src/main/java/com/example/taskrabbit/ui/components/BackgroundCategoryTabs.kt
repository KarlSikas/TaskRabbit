package com.example.taskrabbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun BackgroundCategoryTabs(
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val categories = listOf("all", "free", "premium", "user")

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 4.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
            androidx.compose.material3.Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        },
        edgePadding = 0.dp
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    onSelectCategory(category)
                },
                text = {
                    Text(
                        text = category.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}