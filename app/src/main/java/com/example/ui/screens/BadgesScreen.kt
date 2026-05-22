package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.PrayerViewModel

data class StreakBadge(
    val name: String,
    val requiredStreakDays: Int,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

data class ConsistencyTierInfo(
    val name: String,
    val minPercentage: Int,
    val description: String,
    val tierColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak by viewModel.streakCount.collectAsState()
    val levelTitle by viewModel.consistencyLevelTitle.collectAsState()
    val consistencyMetrics by viewModel.consistencyMetrics.collectAsState()
    val haptic = LocalHapticFeedback.current

    val score30Day = consistencyMetrics[30] ?: 0.0
    val scorePercentage = (score30Day * 100).toInt()

    val streakBadges = listOf(
        StreakBadge("Novice Flame", 3, "Sustain a 3-day continuous prayer streak", Icons.Default.LocalFireDepartment, Color(0xFFF97316)),
        StreakBadge("Spark Initiate", 7, "Sustain a 7-day continuous prayer streak", Icons.Default.LocalFireDepartment, Color(0xFFEF4444)),
        StreakBadge("Torchbearer", 15, "Sustain a 15-day continuous prayer streak", Icons.Default.LocalFireDepartment, Color(0xFFEC4899)),
        StreakBadge("Blazing Beacon", 30, "Sustain a 30-day continuous prayer streak", Icons.Default.LocalFireDepartment, Color(0xFF3B82F6)),
        StreakBadge("Eternal Fire", 50, "Sustain a 50+ day continuous prayer streak", Icons.Default.LocalFireDepartment, Color(0xFF8B5CF6))
    )

    val consistencyTiers = listOf(
        ConsistencyTierInfo("Seeker of Light", 0, "Initial entry rank. Take your first steps to consistency.", Color(0xFF94A3B8)),
        ConsistencyTierInfo("Musalat", 40, "Completed at least 40% of standard prayers in the last 30 days.", Color(0xFF06B6D4)),
        ConsistencyTierInfo("Steadfast", 70, "Completed at least 70% of standard prayers in the last 30 days.", Color(0xFF10B981)),
        ConsistencyTierInfo("Guardian of the Pillar", 90, "Superb mastery: 90%+ completed in the last 30 days.", Color(0xFFD4AF37))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 96.dp) // Provide spacing to avoid overlapping floating dock
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .testTag("badges_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Rank & Achievements",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your spiritual journey achievements",
                    color = AppTheme.colors.grayMuted,
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Status Summary Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(RoundedCornerShape(24.dp), AppTheme.colors.primary)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT RANK",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = levelTitle,
                                color = AppTheme.colors.primary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Rank Icon",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE STREAK",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$streak Days",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ThemedFireAnimation(modifier = Modifier.size(22.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "30-DAY STATUS",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$scorePercentage% Consistent",
                                color = AppTheme.colors.secondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Streak Badges Header
            item {
                Text(
                    text = "STREAK ACHIEVEMENTS",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Streak Badges List
            items(streakBadges) { badge ->
                val isUnlocked = streak >= badge.requiredStreakDays
                val opacity = if (isUnlocked) 1.0f else 0.45f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(RoundedCornerShape(18.dp), if (isUnlocked) badge.color else Color.White.copy(alpha = 0.05f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isUnlocked) badge.color.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = badge.name,
                            tint = if (isUnlocked) badge.color else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = badge.name,
                                color = Color.White.copy(alpha = opacity),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = badge.description,
                            color = AppTheme.colors.grayMuted.copy(alpha = opacity),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (!isUnlocked) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Req: ${badge.requiredStreakDays}d",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Consistency Tiers Header
            item {
                Text(
                    text = "CONSISTENCY RANKS",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // Consistency Tiers List
            items(consistencyTiers) { tier ->
                val isActive = levelTitle == tier.name
                val borderModifier = if (isActive) {
                    Modifier.border(2.dp, tier.tierColor, RoundedCornerShape(18.dp))
                } else {
                    Modifier
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(borderModifier)
                        .liquidGlassCard(RoundedCornerShape(18.dp), if (isActive) tier.tierColor else Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = tier.tierColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tier.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tier.tierColor.copy(alpha = 0.2f))
                                    .border(1.dp, tier.tierColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = tier.tierColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Min: ${tier.minPercentage}%",
                                color = AppTheme.colors.grayMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = tier.description,
                        color = AppTheme.colors.grayMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            
            // Blank spacer to make sure no content gets covered by bottom floating navigation overlay
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
