package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiConfig
import com.example.domain.model.Restaurant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePickerSheet(
    allRestaurants: List<Restaurant>,
    currentFavoriteIds: Set<Int>,
    onSaveFavorites: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedIds by remember {
        mutableStateOf(currentFavoriteIds.map { com.example.data.mapper.RestaurantCanonicalMapper.getCanonicalId(it) }.toMutableSet())
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("favorite_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Choose Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pick up to ${ApiConfig.MAX_FAVORITES} restaurants for your home screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_favorite_picker_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedIds.size == ApiConfig.MAX_FAVORITES)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${selectedIds.size} / ${ApiConfig.MAX_FAVORITES} selected",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedIds.size == ApiConfig.MAX_FAVORITES)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onSaveFavorites(selectedIds) },
                    enabled = selectedIds.isNotEmpty(),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("save_favorites_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()

            SnackbarHost(hostState = snackbarHostState)

            // Group restaurants by Campus
            val grouped = remember(allRestaurants) {
                allRestaurants.groupBy { it.campus }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 8.dp)
            ) {
                grouped.forEach { (campus, restaurants) ->
                    item(key = "header_$campus") {
                        Text(
                            text = campus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                        )
                    }

                    items(restaurants, key = { it.id }) { restaurant ->
                        val canonicalId = com.example.data.mapper.RestaurantCanonicalMapper.getCanonicalId(restaurant.id, restaurant.slug)
                        val isChecked = selectedIds.contains(canonicalId)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) {
                                        selectedIds = (selectedIds - canonicalId).toMutableSet()
                                    } else {
                                        if (selectedIds.size < ApiConfig.MAX_FAVORITES) {
                                            selectedIds = (selectedIds + canonicalId).toMutableSet()
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("You can only select up to ${ApiConfig.MAX_FAVORITES} favorites.")
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (selectedIds.size < ApiConfig.MAX_FAVORITES) {
                                            selectedIds = (selectedIds + canonicalId).toMutableSet()
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("You can only select up to ${ApiConfig.MAX_FAVORITES} favorites.")
                                            }
                                        }
                                    } else {
                                        selectedIds = (selectedIds - canonicalId).toMutableSet()
                                    }
                                },
                                modifier = Modifier.testTag("checkbox_${restaurant.id}")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = restaurant.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = restaurant.status.hoursDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
