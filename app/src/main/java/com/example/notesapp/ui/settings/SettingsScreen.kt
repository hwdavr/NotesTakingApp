package com.example.notesapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.ui.theme.BorderSubtle
import com.example.notesapp.ui.theme.LavenderPrimary
import com.example.notesapp.ui.theme.LavenderSecondary
import com.example.notesapp.ui.theme.TextSecondary

// ─── Colours inferred from SVG ──────────────────────────────────────────────
private val ProBadgeGradientStart = Color(0xFF9B8CFF)
private val ProBadgeGradientEnd = Color(0xFFE06FD8)
private val HeroGradientStart = Color(0xFF7C6CF2)
private val HeroGradientEnd = Color(0xFFC569E0)
private val HeroAvatarOrange = Color(0xFFFF7F0B)
private val SparkleColor = Color(0xFFFFFFFF).copy(alpha = 0.29f)
private val SectionBorder = Color(0xFFEAEAEA)

@Composable
fun SettingsScreen(parentPadding: PaddingValues) {
    Scaffold(
        modifier = Modifier.padding(parentPadding),
        containerColor = Color(0xFFFFFFFF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top app bar ──────────────────────────────────────────────────
            SettingsTopBar()

            Spacer(modifier = Modifier.height(18.dp))

            // ── Hero subscription card ───────────────────────────────────────
            SubscriptionHeroCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Profile section ──────────────────────────────────────────────
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ProfileRow()
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Settings sections card ───────────────────────────────────────
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SettingsSectionHeader(title = "Appearance")
                SettingRowItem(
                    icon = Icons.Outlined.ColorLens,
                    iconBackground = Color(0xFFEDE9FE),
                    iconTint = LavenderPrimary,
                    title = "Theme",
                    subtitle = "Light theme"
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.DarkMode,
                    iconBackground = Color(0xFFE0E7FF),
                    iconTint = Color(0xFF4A68E0),
                    title = "Dark Mode",
                    subtitle = "System default"
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.FontDownload,
                    iconBackground = Color(0xFFF3F4F6),
                    iconTint = Color(0xFF6B7280),
                    title = "Font",
                    subtitle = "System default"
                )
                SettingsDivider()
                SettingsSectionHeader(title = "Account & Sync")
                SettingRowItem(
                    icon = Icons.Outlined.Sync,
                    iconBackground = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    title = "Sync",
                    subtitle = "Not connected"
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Notifications,
                    iconBackground = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    title = "Notifications",
                    subtitle = "Daily reminders off"
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Lock,
                    iconBackground = Color(0xFFFCE7F3),
                    iconTint = Color(0xFFDB2777),
                    title = "Privacy",
                    subtitle = "Manage permissions"
                )
                SettingsDivider()
                SettingsSectionHeader(title = "General")
                SettingRowItem(
                    icon = Icons.Outlined.Language,
                    iconBackground = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Language",
                    subtitle = "English (US)"
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.AccountCircle,
                    iconBackground = Color(0xFFF5F3FF),
                    iconTint = LavenderPrimary,
                    title = "About",
                    subtitle = "Version 1.0.0"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────
@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App logo / wordmark
        Text(
            text = "ANOTES",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = Color(0xFF333333),
                letterSpacing = (-0.5).sp
            ),
            modifier = Modifier.weight(1f)
        )
        // "Pro" badge
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ProBadgeGradientStart, ProBadgeGradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(200f, 0f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Star icon (simplified as text glyph)
                Text(
                    text = "✦",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pro",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── Hero subscription card ──────────────────────────────────────────────────
@Composable
private fun SubscriptionHeroCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(HeroGradientStart, HeroGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Upgrade to Pro",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
                Text(
                    text = "Unlock unlimited notes, themes, sync & more.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // CTA chip
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Get Pro now",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // User avatar circle
            AvatarCircle()
        }
        // Decorative sparkles
        SparkleDecoration(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 140.dp)
        )
    }
}

@Composable
private fun AvatarCircle() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(HeroAvatarOrange),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👤",
            fontSize = 36.sp
        )
    }
}

@Composable
private fun SparkleDecoration(modifier: Modifier = Modifier) {
    Text(
        text = "✦",
        color = SparkleColor,
        fontSize = 22.sp,
        modifier = modifier
    )
}

// ─── Profile row ─────────────────────────────────────────────────────────────
@Composable
private fun ProfileRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar initials
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LavenderPrimary, LavenderSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notes User",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "notes@email.com",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Go to profile",
            tint = TextSecondary
        )
    }
}

// ─── Shared card wrapper ──────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = SectionBorder, shape = RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 14.dp, bottom = 4.dp, end = 16.dp)
    )
}

// ─── Individual setting row ───────────────────────────────────────────────────
@Composable
private fun SettingRowItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBackground),
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFB5B5B5),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── Thin divider between rows ─────────────────────────────────────────────────
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
        color = BorderSubtle,
        thickness = 0.5.dp
    )
}
