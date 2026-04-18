package com.example.notesapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.ThumbUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.ui.theme.BorderSubtle
import com.example.notesapp.ui.theme.LavenderPrimary
import com.example.notesapp.ui.theme.LavenderSecondary
import com.example.notesapp.ui.theme.TextSecondary

private val ProBadgeGradientStart = Color(0xFF9B8CFF)
private val ProBadgeGradientEnd = Color(0xFFE06FD8)
private val HeroGradientStart = Color(0xFF7C6CF2)
private val HeroGradientEnd = Color(0xFFC569E0)
private val SectionBorder = Color(0xFFEAEAEA)

@Composable
fun SettingsScreen(parentPadding: PaddingValues, onLogout: () -> Unit) {
    Scaffold(
        modifier = Modifier.padding(parentPadding).testTag("settings_screen"),
        containerColor = Color(0xFFF8F8FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top App Bar: "AI Notes" + Pro badge ──────────────────────────
            SettingsTopBar()

            Spacer(modifier = Modifier.height(16.dp))

            // ── Hero Banner ─────────────────────────────────────────────────
            HeroBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_hero_card")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Account section ─────────────────────────────────────────────
            SectionHeader(title = "Account")
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_account_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.SmartToy,
                    iconBackground = Color(0xFFF3F4F6),
                    iconTint = Color(0xFF6B7280),
                    title = "Test Device",
                    subtitle = "Unlimited Smart notes, no limits!",
                    showArrow = false
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Logout,
                    iconBackground = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFD32F2F),
                    title = "Logout",
                    subtitle = null,
                    showArrow = true,
                    modifier = Modifier.clickable { onLogout() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── General section ─────────────────────────────────────────────
            SectionHeader(title = "General")
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_general_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.Language,
                    iconBackground = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "App Languages",
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.ShoppingCart,
                    iconBackground = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    title = "Restore Purchase",
                    subtitle = null,
                    showArrow = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Other section ───────────────────────────────────────────────
            SectionHeader(title = "Other")
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_other_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.ThumbUp,
                    iconBackground = Color(0xFFFEF9C3),
                    iconTint = Color(0xFFEAB308),
                    title = "Rate Us",
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Feedback,
                    iconBackground = Color(0xFFF3F4F6),
                    iconTint = Color(0xFF6B7280),
                    title = "Feedback",
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.IosShare,
                    iconBackground = Color(0xFFE0E7FF),
                    iconTint = Color(0xFF4A68E0),
                    title = "Share",
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.PrivacyTip,
                    iconBackground = Color(0xFFFCE7F3),
                    iconTint = Color(0xFFDB2777),
                    title = "Privacy Policy",
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Description,
                    iconBackground = Color(0xFFF5F3FF),
                    iconTint = LavenderPrimary,
                    title = "Terms of Use",
                    subtitle = null,
                    showArrow = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────
@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "AI Notes",
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
                Text(text = "♛", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ─── Hero Banner ─────────────────────────────────────────────────────────────
@Composable
private fun HeroBanner(modifier: Modifier = Modifier) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: text + CTA button
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Get the most out of AI Notes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
                Text(
                    text = "Unlimited Smart notes, no limits!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // CTA "Upgrade to pro" button
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Upgrade to pro",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: user avatar circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(LavenderPrimary, LavenderSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 32.sp)
            }
        }

        // Sparkle decoration
        Text(
            text = "✦",
            color = Color.White.copy(alpha = 0.30f),
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 0.dp, end = 96.dp)
        )
    }
}

// ─── Section header (outside cards) ──────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
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
            .padding(start = 16.dp, bottom = 6.dp, end = 16.dp)
    )
}

// ─── Shared card wrapper ──────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = SectionBorder, shape = RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// ─── Individual setting row ───────────────────────────────────────────────────
@Composable
private fun SettingRowItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    showArrow: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFB5B5B5),
                modifier = Modifier.size(20.dp)
            )
        }
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
