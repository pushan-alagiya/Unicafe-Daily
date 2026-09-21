package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.UniCafeDailyTheme
import com.example.ui.viewmodel.UniCafeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UniCafeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniCafeDailyTheme {
                val uiState by viewModel.uiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    onRefreshClicked = viewModel::onRefreshClicked,
                    onFilterSelected = viewModel::onDietaryFilterSelected,
                    onOpenFavoritePicker = viewModel::onOpenFavoritePicker,
                    onDismissFavoritePicker = viewModel::onDismissFavoritePicker,
                    onSaveFavorites = viewModel::onSaveFavorites,
                    onOpenLeavingForLunch = viewModel::onOpenLeavingForLunch,
                    onDismissLeavingForLunch = viewModel::onDismissLeavingForLunch,
                    onOpenSurpriseMe = viewModel::onOpenSurpriseMe,
                    onDismissSurpriseMe = viewModel::onDismissSurpriseMe,
                    onOpenBudgetHistory = viewModel::onOpenBudgetHistory,
                    onDismissBudgetHistory = viewModel::onDismissBudgetHistory,
                    onToggleFavoriteMeal = viewModel::onToggleFavoriteMeal,
                    onRecordEatenMeal = { meal, restName ->
                        viewModel.onRecordEatenMeal(
                            mealName = meal.name,
                            restaurantName = restName,
                            priceEur = meal.studentPrice?.replace("€", "")?.trim()?.toDoubleOrNull() ?: 3.10,
                            badges = meal.dietaryBadges
                        )
                    },
                    onDeleteEatenMeal = viewModel::onDeleteEatenMeal,
                    onSetMyDiet = viewModel::onSetMyDiet,
                    onToggleHideNonMatching = viewModel::onToggleHideNonMatching,
                    onDismissMenuNotice = viewModel::onDismissMenuNotice,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
