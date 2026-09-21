package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DietaryFilter
import com.example.domain.model.Meal
import com.example.domain.model.MyDietPreference
import com.example.domain.model.Restaurant
import com.example.ui.components.BudgetHistorySheet
import com.example.ui.components.DietaryFilterChips
import com.example.ui.components.FavoritePickerSheet
import com.example.ui.components.FoodDetailSheet
import com.example.ui.components.LeavingForLunchSheet
import com.example.ui.components.RestaurantCard
import com.example.ui.components.StaleDataBanner
import com.example.ui.components.SurpriseMeSheet
import com.example.ui.state.HomeUiState
import com.example.ui.theme.BrandBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefreshClicked: () -> Unit,
    onFilterSelected: (DietaryFilter) -> Unit,
    onOpenFavoritePicker: () -> Unit,
    onDismissFavoritePicker: () -> Unit,
    onSaveFavorites: (Set<Int>) -> Unit,
    onOpenLeavingForLunch: () -> Unit,
    onDismissLeavingForLunch: () -> Unit,
    onOpenSurpriseMe: () -> Unit,
    onDismissSurpriseMe: () -> Unit,
    onOpenBudgetHistory: () -> Unit,
    onDismissBudgetHistory: () -> Unit,
    onToggleFavoriteMeal: (String) -> Unit,
    onRecordEatenMeal: (Meal, String) -> Unit,
    onDeleteEatenMeal: (String) -> Unit,
    onSetMyDiet: (MyDietPreference) -> Unit,
    onToggleHideNonMatching: (Boolean) -> Unit,
    onDismissMenuNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMealForDetail by remember { mutableStateOf<Pair<Meal, Restaurant>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandBlue,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "UniCafe Daily",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 19.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Manage Favorites Button with count badge
                    Surface(
                        onClick = onOpenFavoritePicker,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .testTag("choose_favorites_button")
                            .semantics { contentDescription = "Choose favorite restaurants" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.favoriteRestaurants.size}/3",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Refresh Button with progress indicator
                    IconButton(
                        onClick = onRefreshClicked,
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier
                            .testTag("refresh_button")
                            .semantics { contentDescription = "Refresh menu" }
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stale Data Warning Banner (when offline / using cache)
            if (uiState.isDataStale || (uiState.errorMessage != null && uiState.favoriteRestaurants.isNotEmpty())) {
                StaleDataBanner(
                    lastUpdatedText = uiState.lastUpdatedText,
                    onRetry = onRefreshClicked
                )
            }

            // Menu Notice Banner (e.g., changes detected)
            if (uiState.menuChangeNotice != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.menuChangeNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissMenuNotice,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notice",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Quick Search / Dish Finder Bar (Fixed UI: Uses BasicTextField to eliminate top clipping)
            if (uiState.favoriteRestaurants.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search today's dishes (e.g. tofu, lohi, pasta)...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_meals_input")
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Options & Shortcuts Row: Equal-sized, clean pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. "Leaving for lunch?" Quick View Pill
                val openCount = uiState.favoriteRestaurants.count { it.status.isOpenNow }
                QuickOptionPill(
                    icon = Icons.Default.DirectionsWalk,
                    title = "Leaving for lunch?",
                    badgeText = if (openCount > 0) "$openCount Open" else "Closed",
                    badgeColor = if (openCount > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                    onClick = onOpenLeavingForLunch,
                    testTag = "pill_leaving_for_lunch"
                )

                // 2. "Surprise me" Random Picker Pill
                QuickOptionPill(
                    icon = Icons.Default.Casino,
                    title = "Surprise Me",
                    badgeText = "Dice",
                    badgeColor = BrandBlue,
                    onClick = onOpenSurpriseMe,
                    testTag = "pill_surprise_me"
                )

                // 3. Lunch Budget Tracker & Meal History Pill
                val spentFormatted = String.format(Locale.US, "€%.2f", uiState.monthlySpentEur)
                QuickOptionPill(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Budget Tracker",
                    badgeText = spentFormatted,
                    badgeColor = Color(0xFFF59E0B),
                    onClick = onOpenBudgetHistory,
                    testTag = "pill_budget_tracker"
                )

                // 4. "My diet" quick mode pill
                val dietLabel = when (uiState.myDietPreference) {
                    MyDietPreference.NONE -> "My Diet: None"
                    MyDietPreference.VEGAN -> "Diet: Vegan"
                    MyDietPreference.GLUTEN_FREE -> "Diet: Gluten-free"
                    MyDietPreference.MILK_FREE -> "Diet: Milk-free"
                }
                QuickOptionPill(
                    icon = Icons.Default.FilterList,
                    title = dietLabel,
                    badgeText = if (uiState.hideNonMatchingMeals) "Relevant Only" else "All shown",
                    badgeColor = if (uiState.hideNonMatchingMeals) BrandBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        // Toggle hide non-matching
                        onToggleHideNonMatching(!uiState.hideNonMatchingMeals)
                    },
                    testTag = "pill_my_diet"
                )
            }

            // Dietary Filter Chips Row (All, Veg, G, M)
            DietaryFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = onFilterSelected
            )

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading && uiState.favoriteRestaurants.isEmpty() -> {
                        // Full Screen Loading State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BrandBlue)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Loading University of Helsinki menus...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    uiState.favoriteRestaurants.isEmpty() && uiState.errorMessage != null -> {
                        // Network Error with no cache
                        NoNetworkErrorState(
                            message = uiState.errorMessage,
                            onRetry = onRefreshClicked
                        )
                    }

                    uiState.favoriteRestaurants.isEmpty() -> {
                        // Empty Favorites State
                        EmptyFavoritesState(onSelectFavorites = onOpenFavoritePicker)
                    }

                    else -> {
                        // Filter restaurants and meals if search query active or hideNonMatchingMeals active
                        val displayedRestaurants = uiState.favoriteRestaurants.map { rest ->
                            var meals = rest.todaysMeals

                            // Filter by search query
                            if (searchQuery.isNotBlank()) {
                                meals = meals.filter { meal ->
                                    meal.name.contains(searchQuery, ignoreCase = true) ||
                                            meal.category.contains(searchQuery, ignoreCase = true) ||
                                            meal.dietaryBadges.any { it.contains(searchQuery, ignoreCase = true) }
                                }
                            }

                            // Filter by hideNonMatchingMeals if enabled
                            if (uiState.hideNonMatchingMeals && uiState.selectedFilter != DietaryFilter.ALL) {
                                meals = meals.filter { meal ->
                                    uiState.selectedFilter.matches(meal.dietaryBadges)
                                }
                            }

                            rest.copy(todaysMeals = meals)
                        }

                        val eatenNames = remember(uiState.eatenMeals) {
                            uiState.eatenMeals.map { it.mealName }.toSet()
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                items = displayedRestaurants,
                                key = { it.id }
                            ) { restaurant ->
                                RestaurantCard(
                                    restaurant = restaurant,
                                    selectedFilter = uiState.selectedFilter,
                                    favoriteMealNames = uiState.favoriteMealNames,
                                    eatenMealNames = eatenNames,
                                    onToggleFavoriteMeal = onToggleFavoriteMeal,
                                    onMealClick = { clickedMeal, rest ->
                                        selectedMealForDetail = Pair(clickedMeal, rest)
                                    }
                                )
                            }

                            // Footer info
                            item {
                                FooterNote(
                                    lastUpdatedText = uiState.lastUpdatedText,
                                    onEditFavorites = onOpenFavoritePicker
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for selecting up to 3 favorites
    if (uiState.showFavoritePicker) {
        FavoritePickerSheet(
            allRestaurants = uiState.allRestaurants,
            currentFavoriteIds = uiState.favoriteIds,
            onSaveFavorites = onSaveFavorites,
            onDismiss = onDismissFavoritePicker
        )
    }

    // "Leaving for lunch?" dedicated comparison sheet
    if (uiState.showLeavingForLunchSheet) {
        LeavingForLunchSheet(
            favoriteRestaurants = uiState.favoriteRestaurants,
            onSelectRestaurant = {
                onDismissLeavingForLunch()
            },
            onDismiss = onDismissLeavingForLunch
        )
    }

    // "Surprise me" random meal picker sheet
    if (uiState.showSurpriseMeSheet) {
        SurpriseMeSheet(
            favoriteRestaurants = uiState.favoriteRestaurants.ifEmpty { uiState.allRestaurants },
            onRecordEaten = onRecordEatenMeal,
            onDismiss = onDismissSurpriseMe
        )
    }

    // Lunch budget & meal history sheet
    if (uiState.showBudgetHistorySheet) {
        BudgetHistorySheet(
            eatenMeals = uiState.eatenMeals,
            onDeleteRecord = onDeleteEatenMeal,
            onDismiss = onDismissBudgetHistory
        )
    }

    // Interactive Food Detail Modal Sheet
    selectedMealForDetail?.let { (meal, restaurant) ->
        val isFav = uiState.favoriteMealNames.contains(meal.name)
        val isEaten = uiState.eatenMeals.any { it.mealName == meal.name && it.restaurantName == restaurant.name }

        FoodDetailSheet(
            meal = meal,
            restaurantName = restaurant.name,
            isFavoriteMeal = isFav,
            onToggleFavoriteMeal = { onToggleFavoriteMeal(meal.name) },
            isEatenToday = isEaten,
            onRecordEaten = { onRecordEatenMeal(meal, restaurant.name) },
            onDismiss = { selectedMealForDetail = null }
        )
    }
}

@Composable
private fun QuickOptionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(38.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(
    onSelectFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Select Your UniCafes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pick up to 3 UniCafe restaurants (e.g. Kaivopiha, Exactum, Chemicum) to check lunch menus instantly from your home screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSelectFavorites,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("empty_state_select_favorites_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Select Favorites", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoNetworkErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unable to load today's menu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("error_retry_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Try Again", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FooterNote(
    lastUpdatedText: String?,
    onEditFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onEditFavorites,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("footer_edit_favorites_button")
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Change Favorite UniCafes (max 3)")
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (lastUpdatedText != null)
                "Synced at $lastUpdatedText • Student lunch €3.10"
            else "UniCafe public menu • Student lunch €3.10",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            fontSize = 11.sp
        )
    }
}
