package com.example.chrona.feature.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.core.data.CityRepository
import com.example.chrona.core.model.City
import com.example.chrona.ui.theme.ChronaThemeTokens
import com.example.chrona.ui.theme.TABULAR_NUMS
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimezoneSearchSheet(
    onDismiss: () -> Unit,
    onCitySelected: (City) -> Unit,
    is24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val savedCities by CityRepository.savedCities.collectAsState()
    val savedIds = remember(savedCities) { savedCities.map { it.geonameId }.toSet() }

    val filteredCities = remember(searchQuery, savedIds) {
        CityRepository.searchCities(searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = extendedColors.cardBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(extendedColors.outline)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add place",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = extendedColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search city, country, or timezone...", color = extendedColors.textMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = extendedColors.textMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = extendedColors.surfaceHigh,
                    unfocusedContainerColor = extendedColors.surfaceHigh,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = extendedColors.textPrimary,
                    unfocusedTextColor = extendedColors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of Cities
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredCities, key = { it.geonameId }) { city ->
                    val isSaved = savedIds.contains(city.geonameId)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                CityRepository.toggleSaveCity(city.geonameId)
                                onCitySelected(city)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = extendedColors.surfaceLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(city.identityColor)
                                )
                                Column {
                                    Text(
                                        text = city.cityName,
                                        color = extendedColors.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${city.countryName} · ${city.formattedOffset()}",
                                        color = extendedColors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = city.formattedTime(is24Hour),
                                    color = extendedColors.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = TABULAR_NUMS)
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = if (isSaved) city.identityColor else extendedColors.surfaceHigh,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isSaved) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Added",
                                                tint = Color(0xFF0B1320),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
