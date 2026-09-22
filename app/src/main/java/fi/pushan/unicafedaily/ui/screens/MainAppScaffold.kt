package fi.pushan.unicafedaily.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.pushan.unicafedaily.R
import fi.pushan.unicafedaily.data.mapper.RestaurantCanonicalMapper
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.ui.components.BudgetHistorySheet
import fi.pushan.unicafedaily.ui.components.DietaryLegendSheet
import fi.pushan.unicafedaily.ui.components.FavoriteDishesSheet
import fi.pushan.unicafedaily.ui.components.FavoritePickerSheet
import fi.pushan.unicafedaily.ui.components.FilterBottomSheet
import fi.pushan.unicafedaily.ui.components.FoodDetailSheet
import fi.pushan.unicafedaily.ui.components.LeavingForLunchSheet
import fi.pushan.unicafedaily.ui.components.RestaurantDetailSheet
import fi.pushan.unicafedaily.ui.components.SurpriseMeSheet
import fi.pushan.unicafedaily.ui.components.UniCafeInfoSheet
import fi.pushan.unicafedaily.ui.state.AppTab
import fi.pushan.unicafedaily.ui.state.HomeUiState
import fi.pushan.unicafedaily.ui.theme.BrandBlue
import fi.pushan.unicafedaily.ui.viewmodel.UniCafeViewModel

data class BottomNavItem(
    val tab: AppTab,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(AppTab.TODAY, R.string.tab_today, Icons.Filled.Today, Icons.Outlined.Today, "nav_today"),
    BottomNavItem(AppTab.MENUS, R.string.tab_menus, Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "nav_menus"),
    BottomNavItem(AppTab.SETTINGS, R.string.tab_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
)

@Composable
fun MainAppScaffold(
    uiState: HomeUiState,
    viewModel: UniCafeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedMealForDetail by remember { mutableStateOf<Pair<Meal, String>?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("main_app_scaffold")
    ) {
        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "screen_tab_transition",
            modifier = Modifier.fillMaxSize()
        ) { targetTab ->
            when (targetTab) {
                AppTab.TODAY -> {
                    HomeScreen(
                        uiState = uiState,
                        onRefreshClicked = viewModel::onRefreshClicked,
                        onOpenFavoritePicker = viewModel::onOpenFavoritePicker,
                        onOpenFilterSheet = viewModel::onOpenFilterSheet,
                        onDietaryFilterSelected = viewModel::onDietaryFilterSelected,
                        onOpenDietaryLegend = viewModel::onOpenDietaryLegend,
                        onOpenLeavingForLunch = viewModel::onOpenLeavingForLunch,
                        onOpenSurpriseMe = viewModel::onOpenSurpriseMe,
                        onOpenBudgetHistory = viewModel::onOpenBudgetHistory,
                        onToggleFavoriteMeal = viewModel::onToggleFavoriteMeal,
                        onMealClick = { meal, restName ->
                            viewModel.onViewMeal(meal, restName)
                            selectedMealForDetail = meal to restName
                        },
                        onDismissMenuNotice = viewModel::onDismissMenuNotice,
                        onOpenUniCafeInfo = viewModel::onOpenUniCafeInfo,
                        onOpenFavoriteDishes = viewModel::onOpenFavoriteDishes,
                        onSetCustomerCategory = viewModel::onSetCustomerCategory,
                        onSelectRestaurantForDetail = viewModel::onSelectRestaurantForDetail,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppTab.MENUS -> {
                    MenusScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onMealClick = { meal, restName ->
                            viewModel.onViewMeal(meal, restName)
                            selectedMealForDetail = meal to restName
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Floating Dock: Only occupies its rounded pill area
        ModernDockBottomBar(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.onTabSelected(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Global Modal Sheets (Accessible from ANY screen)
    if (uiState.showFavoritePicker) {
        FavoritePickerSheet(
            allRestaurants = uiState.allRestaurants,
            currentFavoriteIds = uiState.favoriteIds,
            onSaveFavorites = viewModel::onSaveFavorites,
            onDismiss = viewModel::onDismissFavoritePicker
        )
    }

    if (uiState.showDietaryLegendSheet) {
        DietaryLegendSheet(
            onDismiss = viewModel::onDismissDietaryLegend
        )
    }

    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            selectedDietaryFilter = uiState.selectedFilter,
            selectedStatusFilter = uiState.statusFilter,
            onDietaryFilterSelected = viewModel::onDietaryFilterSelected,
            onStatusFilterSelected = viewModel::onStatusFilterSelected,
            onOpenDietaryLegend = viewModel::onOpenDietaryLegend,
            onDismiss = viewModel::onDismissFilterSheet
        )
    }

    if (uiState.showSurpriseMeSheet) {
        SurpriseMeSheet(
            favoriteRestaurants = uiState.favoriteRestaurants,
            allRestaurants = uiState.allRestaurants,
            onRecordEaten = { meal, restName ->
                viewModel.onRecordEatenMeal(
                    mealName = meal.name,
                    restaurantName = restName,
                    priceEur = meal.studentPrice?.replace("€", "")?.trim()?.toDoubleOrNull() ?: 3.10,
                    badges = meal.dietaryBadges
                )
            },
            onDismiss = viewModel::onDismissSurpriseMe
        )
    }

    if (uiState.showBudgetHistorySheet) {
        BudgetHistorySheet(
            eatenMeals = uiState.eatenMeals,
            monthlyBudgetEur = uiState.monthlyBudgetEur,
            onSaveMonthlyBudget = viewModel::onSaveMonthlyBudget,
            onDeleteRecord = viewModel::onDeleteEatenMeal,
            onDismiss = viewModel::onDismissBudgetHistory
        )
    }

    if (uiState.showLeavingForLunchSheet) {
        LeavingForLunchSheet(
            favoriteRestaurants = uiState.favoriteRestaurants,
            onSelectRestaurant = { },
            onDismiss = viewModel::onDismissLeavingForLunch
        )
    }

    if (uiState.showUniCafeInfoSheet) {
        UniCafeInfoSheet(
            selectedCategory = uiState.customerCategory,
            onCategorySelected = viewModel::onSetCustomerCategory,
            onDismiss = viewModel::onDismissUniCafeInfo
        )
    }

    if (uiState.showFavoriteDishesSheet) {
        FavoriteDishesSheet(
            favoriteMealNames = uiState.favoriteMealNames,
            allRestaurants = uiState.allRestaurants,
            customerCategory = uiState.customerCategory,
            onToggleFavoriteMeal = viewModel::onToggleFavoriteMeal,
            onMealClick = { meal, rest ->
                viewModel.onViewMeal(meal, rest.name)
                selectedMealForDetail = meal to rest.name
            },
            onDismiss = viewModel::onDismissFavoriteDishes
        )
    }

    uiState.selectedRestaurantForDetail?.let { restaurant ->
        val isFav = uiState.favoriteIds.any {
            RestaurantCanonicalMapper.getCanonicalId(it) == RestaurantCanonicalMapper.getCanonicalId(restaurant.id, restaurant.slug)
        }
        RestaurantDetailSheet(
            restaurant = restaurant,
            isFavorite = isFav,
            customerCategory = uiState.customerCategory,
            favoriteMealNames = uiState.favoriteMealNames,
            onToggleFavoriteRestaurant = viewModel::onToggleFavoriteRestaurant,
            onToggleFavoriteMeal = viewModel::onToggleFavoriteMeal,
            onMealClick = { meal ->
                viewModel.onViewMeal(meal, restaurant.name)
                selectedMealForDetail = meal to restaurant.name
            },
            onDismiss = { viewModel.onSelectRestaurantForDetail(null) }
        )
    }

    selectedMealForDetail?.let { (meal, restaurantName) ->
        FoodDetailSheet(
            meal = meal,
            restaurantName = restaurantName,
            isFavoriteMeal = uiState.favoriteMealNames.any { it.trim().equals(meal.name.trim(), ignoreCase = true) },
            onToggleFavoriteMeal = { viewModel.onToggleFavoriteMeal(meal.name) },
            onRecordEaten = {
                viewModel.onRecordEatenMeal(
                    mealName = meal.name,
                    restaurantName = restaurantName,
                    priceEur = meal.studentPrice?.replace("€", "")?.trim()?.toDoubleOrNull() ?: 3.10,
                    badges = meal.dietaryBadges
                )
            },
            customerCategory = uiState.customerCategory,
            onDismiss = { selectedMealForDetail = null }
        )
    }
}

/**
 * Modern floating dock bar with minimal margins, sleek rounded pill container,
 * smooth pill active highlights and responsive ripple feedback.
 */
@Composable
private fun ModernDockBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(56.dp)
                .testTag("bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BOTTOM_NAV_ITEMS.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    val animatedContainerColor by animateColorAsState(
                        targetValue = if (isSelected) BrandBlue.copy(alpha = 0.16f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dock_item_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) BrandBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dock_item_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(animatedContainerColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 24.dp),
                                onClick = { onTabSelected(item.tab) }
                            )
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = stringResource(item.titleRes),
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

