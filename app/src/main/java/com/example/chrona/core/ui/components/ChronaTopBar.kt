package com.example.chrona.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.ui.theme.ChronaThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronaTopBar(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val extendedColors = ChronaThemeTokens.extended
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = extendedColors.textPrimary
            )
        },
        actions = {
            actions()

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = extendedColors.textSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(
                        extendedColors.cardBackground,
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Settings",
                                color = extendedColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToSettings()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Screensaver",
                                color = extendedColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.NightlightRound,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToScreensaver()
                        }
                    )

                    HorizontalDivider(
                        color = extendedColors.divider,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "About Chrona",
                                color = extendedColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = extendedColors.textSecondary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToAbout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)
    )
}
