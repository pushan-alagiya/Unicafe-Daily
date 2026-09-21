package fi.pushan.unicafedaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import fi.pushan.unicafedaily.R
import fi.pushan.unicafedaily.domain.model.DietaryFilter
import fi.pushan.unicafedaily.domain.model.Meal
import fi.pushan.unicafedaily.domain.model.Restaurant
import fi.pushan.unicafedaily.domain.model.SearchEngine
import fi.pushan.unicafedaily.ui.components.MealItemView
import fi.pushan.unicafedaily.ui.components.RestaurantCard
import fi.pushan.unicafedaily.ui.state.HomeUiState
import fi.pushan.unicafedaily.ui.theme.BrandBlue
import fi.pushan.unicafedaily.ui.viewmodel.UniCafeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenusScreen(
    uiState: HomeUiState,
    viewModel: UniCafeViewModel,
    onMealClick: (Meal, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRestaurantId by remember { mutableStateOf<Int?>(null) }

    val today = remember { LocalDate.now() }
    val dates = remember {
        (0..13).map { today.plusDays(it.toLong()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.browse_menus_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onOpenFilterSheet() },
                        modifier = Modifier.testTag("menus_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = stringResource(R.string.filter_button),
                            tint = BrandBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("menus_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Date Bar
            DateSelectorBar(
                dates = dates,
                selectedDate = uiState.selectedMenuDate,
                onDateSelected = { viewModel.onMenuDateSelected(it) },
                language = uiState.appLanguage
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_dishes_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .testTag("menus_search_input")
            )

            // Quick Restaurant Selector Chips
            val restaurants = if (uiState.futureDateRestaurants.isNotEmpty()) {
                uiState.futureDateRestaurants
            } else {
                uiState.allRestaurants
            }

            if (restaurants.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRestaurantId == null,
                            onClick = { selectedRestaurantId = null },
                            label = { Text(stringResource(R.string.all_cafes), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    items(restaurants, key = { "chip_${it.id}" }) { r ->
                        val isSelected = selectedRestaurantId == r.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedRestaurantId = if (isSelected) null else r.id
                            },
                            label = { Text(r.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Menu List
            if (uiState.isFutureDateLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else {
                val candidateRestaurants = restaurants.filter {
                    selectedRestaurantId == null || it.id == selectedRestaurantId
                }

                val filteredRestaurants = SearchEngine.filterRestaurantsBySearch(
                    restaurants = candidateRestaurants,
                    query = searchQuery,
                    selectedFilter = uiState.selectedFilter
                )

                if (filteredRestaurants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_matches_search, searchQuery) else stringResource(R.string.no_menus_published_date),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.unicafe_schedule_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
                    ) {
                        items(filteredRestaurants, key = { "menu_r_${it.id}" }) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                selectedFilter = uiState.selectedFilter,
                                favoriteMealNames = uiState.favoriteMealNames,
                                eatenMealNames = uiState.eatenMeals.map { it.mealName }.toSet(),
                                dateFormatted = "",
                                onToggleFavoriteMeal = { viewModel.onToggleFavoriteMeal(it) },
                                onMealClick = { meal, rest -> onMealClick(meal, rest.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectorBar(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    language: String
) {
    val locale = when (language) {
        "fi" -> Locale("fi", "FI")
        "sv" -> Locale("sv", "SE")
        else -> Locale.ENGLISH
    }
    val dayOfWeekFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE", locale) }
    val dayNumFormatter = remember(locale) { DateTimeFormatter.ofPattern("d", locale) }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM", locale) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(dates, key = { it.toString() }) { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()

            Surface(
                onClick = { onDateSelected(date) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isToday && !isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandBlue) else null,
                modifier = Modifier
                    .width(62.dp)
                    .height(78.dp)
                    .testTag("date_pill_$date")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isToday) stringResource(R.string.today_pill) else date.format(dayOfWeekFormatter).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = date.format(dayNumFormatter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                    Text(
                        text = date.format(monthFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
