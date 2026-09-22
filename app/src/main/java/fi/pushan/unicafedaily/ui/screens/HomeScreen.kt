package fi.pushan.unicafedaily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.pushan.unicafedaily.R
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.ui.components.RestaurantCard
import fi.pushan.unicafedaily.ui.components.StaleDataBanner
import fi.pushan.unicafedaily.ui.state.HomeUiState
import fi.pushan.unicafedaily.ui.theme.BrandBlue

import androidx.compose.material.icons.filled.Tune
import fi.pushan.unicafedaily.ui.components.DietaryFilterChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefreshClicked: () -> Unit,
    onOpenFavoritePicker: () -> Unit,
    onOpenFilterSheet: () -> Unit = {},
    onDietaryFilterSelected: (DietaryFilter) -> Unit = {},
    onOpenDietaryLegend: () -> Unit = {},
    onOpenLeavingForLunch: () -> Unit = {},
    onOpenSurpriseMe: () -> Unit = {},
    onOpenBudgetHistory: () -> Unit = {},
    onOpenFavoriteDishes: () -> Unit = {},
    onToggleFavoriteMeal: (String) -> Unit = {},
    onOpenRestaurantDetail: (Restaurant) -> Unit = {},
    onOpenUniCafeInfo: () -> Unit = {},
    onMealClick: (Meal, String) -> Unit = { _, _ -> },
    onDismissMenuNotice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFavoriteRestId by remember { mutableStateOf<Int?>(null) }

    val displayDate = remember(uiState.formattedDate, uiState.appLanguage) {
        if (uiState.formattedDate.isNotBlank()) {
            uiState.formattedDate
        } else {
            val locale = when (uiState.appLanguage) {
                "fi" -> java.util.Locale("fi", "FI")
                "sv" -> java.util.Locale("sv", "SE")
                else -> java.util.Locale.ENGLISH
            }
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d.M.", locale))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandBlue,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = displayDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                actions = {
                    // UniCafe Guide & Rates Info
                    IconButton(
                        onClick = onOpenUniCafeInfo,
                        modifier = Modifier.testTag("home_rates_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "UniCafe rates, student discounts and dietary guide",
                            tint = BrandBlue
                        )
                    }

                    // Filter Action Button to open filters sheet
                    IconButton(
                        onClick = onOpenFilterSheet,
                        modifier = Modifier.testTag("home_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = stringResource(R.string.filter_button),
                            tint = if (uiState.selectedFilter != DietaryFilter.ALL || uiState.statusFilter != "ALL") BrandBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Favorite Dishes Screen
                    IconButton(
                        onClick = onOpenFavoriteDishes,
                        modifier = Modifier.testTag("home_favorite_dishes_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite dishes screen",
                                tint = Color(0xFFEF4444)
                            )
                            if (uiState.favoriteMealNames.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${uiState.favoriteMealNames.size}",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Manage Favorites Action
                    IconButton(
                        onClick = onOpenFavoritePicker,
                        modifier = Modifier.testTag("manage_favorites_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.choose_favorites),
                            tint = Color(0xFFD97706)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefreshClicked,
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = BrandBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh),
                                tint = BrandBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("home_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stale Data Notice Banner
            if (uiState.isDataStale && uiState.lastUpdatedText != null) {
                StaleDataBanner(
                    lastUpdatedText = uiState.lastUpdatedText,
                    onRetry = onRefreshClicked
                )
            }

            // Menu change notice banner if any
            uiState.menuChangeNotice?.let { notice ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("menu_change_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notice,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = onDismissMenuNotice,
                            modifier = Modifier.size(24.dp).testTag("dismiss_menu_change_notice")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notice",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Error View
            if (uiState.errorMessage != null && uiState.allRestaurants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Network connection error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onRefreshClicked,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                return@Scaffold
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
                return@Scaffold
            }

            val favorites = uiState.favoriteRestaurants

            // If user has NO favorite UniCafes selected
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.no_favorites_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = stringResource(R.string.no_favorites_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onOpenFavoritePicker,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                modifier = Modifier.testTag("home_choose_favorites_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.add_favorites_btn),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // If user has multiple favorites, offer a clean, minimal switcher bar
                if (favorites.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFavoriteRestId == null,
                                onClick = { selectedFavoriteRestId = null },
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.favorites)} (${favorites.size})",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedFavoriteRestId == null) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("fav_chip_all")
                            )
                        }

                        items(favorites, key = { "fav_tab_${it.id}" }) { restaurant ->
                            val isSelected = selectedFavoriteRestId == restaurant.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFavoriteRestId = if (isSelected) null else restaurant.id
                                },
                                label = {
                                    Text(
                                        text = restaurant.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("fav_chip_${restaurant.id}")
                            )
                        }
                    }
                }

                val restaurantsToDisplay = if (selectedFavoriteRestId != null) {
                    favorites.filter { it.id == selectedFavoriteRestId }
                } else {
                    favorites
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 84.dp)
                ) {
                    items(restaurantsToDisplay, key = { "fav_r_${it.id}" }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            selectedFilter = uiState.selectedFilter,
                            favoriteMealNames = uiState.favoriteMealNames,
                            eatenMealNames = uiState.eatenMeals.map { it.mealName }.toSet(),
                            dateFormatted = uiState.formattedDate,
                            onToggleFavoriteMeal = onToggleFavoriteMeal,
                            onOpenRestaurantDetail = onOpenRestaurantDetail,
                            onMealClick = { meal, rest -> onMealClick(meal, rest.name) }
                        )
                    }
                }
            }
        }
    }
}
