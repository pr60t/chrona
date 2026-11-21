package com.example.chrona.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.R
import com.example.chrona.core.data.PreferencesRepository
import com.example.chrona.ui.theme.ChronaThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = ChronaThemeTokens.extended
    val isDark = ChronaThemeTokens.isDark

    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        PreferencesRepository.completeOnboarding(context)
        onDismiss()
    }

    fun handleGetStarted() {
        if (isProcessing) return
        isProcessing = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PreferencesRepository.completeOnboarding(context)
            onDismiss()
        }
    }

    BasicAlertDialog(
        onDismissRequest = {
            // Dismissing via outside tap also completes onboarding
            PreferencesRepository.completeOnboarding(context)
            onDismiss()
        },
        modifier = modifier.clip(RoundedCornerShape(28.dp))
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = extendedColors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Chrona brand symbol
                Image(
                    painter = painterResource(
                        if (isDark) R.drawable.ic_chrona_symbol else R.drawable.ic_chrona_symbol_light
                    ),
                    contentDescription = "Chrona",
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // App Title
                Text(
                    text = "Welcome to Chrona",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Precision World Clock & Cross-Timezone Alarms",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Feature items explaining purpose & notification alerts
                FeatureRow(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFFA7E8D8),
                    title = "Cross-Timezone Alarms",
                    description = "Schedule alarms by target city local time; Chrona automatically calculates your local wake time."
                )

                Spacer(modifier = Modifier.height(14.dp))

                FeatureRow(
                    icon = Icons.Default.Public,
                    iconTint = Color(0xFFF2DF9B),
                    title = "Natural Earth World Map",
                    description = "Geographically accurate projection with theme-adaptive continents and interactive time pins."
                )

                Spacer(modifier = Modifier.height(14.dp))

                FeatureRow(
                    icon = Icons.Default.Notifications,
                    iconTint = Color(0xFFE7A19A),
                    title = "Alerts & Notifications",
                    description = "Ensure alarms, snoozed intervals, and background timers notify you promptly."
                )

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = { handleGetStarted() },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color(0xFF0B1320)
                    )
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended
    val isDark = ChronaThemeTokens.isDark

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.clip(RoundedCornerShape(28.dp))
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = extendedColors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(
                        if (isDark) R.drawable.ic_chrona_symbol else R.drawable.ic_chrona_symbol_light
                    ),
                    contentDescription = "Chrona",
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "About Chrona",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Version 1.0.0 · Pixel Material 3",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Chrona is engineered with exact Java Time semantics (ZoneId, ZonedDateTime, ZoneRules, Instant) to provide mathematically verified cross-timezone alarm scheduling and Savric Natural Earth world mapping.",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.surfaceHigh,
                        contentColor = extendedColors.textPrimary
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    val extendedColors = ChronaThemeTokens.extended

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = extendedColors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = extendedColors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
