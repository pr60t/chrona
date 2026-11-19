package com.example.chrona.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chrona.ui.theme.ChronaThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ChronaThemeTokens.extended

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
                // App Logo / Title
                Text(
                    text = "CHRONA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Precision Time & World Intelligence",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Feature items
                FeatureRow(
                    icon = Icons.Default.Public,
                    iconTint = Color(0xFFA7E8D8),
                    title = "Natural Earth World Map",
                    description = "Geographically accurate projection with responsive marker collapse."
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureRow(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFFF2DF9B),
                    title = "Cross-Timezone Alarms",
                    description = "Set alarms in Tokyo or Warsaw time; Chrona automatically calculates your local trigger."
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureRow(
                    icon = Icons.Default.Alarm,
                    iconTint = Color(0xFFE7A19A),
                    title = "Daylight Saving Resilience",
                    description = "Calculates exact transition instances, resolving gaps and overlaps automatically."
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
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
                        text = "Get started",
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
                Text(
                    text = "About Chrona",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Version 1.0.0\nAudited Material 3 Design",
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
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text = title,
                color = extendedColors.textPrimary,
                fontSize = 14.sp,
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
