package com.example.notesapp.ui.settings.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.settings.viewmodel.SettingsViewModel
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun SettingsScreen(
    parentPadding: PaddingValues,
    onLogoutSuccess: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        parentPadding = parentPadding,
        profileTitle = uiState.profileTitle,
        onLogout = {
            viewModel.logout(
                activityContext = context as Activity,
                onSuccess = onLogoutSuccess,
                onError = { /* Handle error */ }
            )
        }
    )
}

@Composable
fun SettingsScreenContent(
    parentPadding: PaddingValues,
    profileTitle: String = stringResource(R.string.settings_guest_profile),
    onLogout: () -> Unit
) {
    Scaffold(
        modifier = Modifier.padding(parentPadding).testTag("settings_screen"),
        containerColor = LocalAppColors.current.settingsBackground,
        contentWindowInsets = WindowInsets(0)
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
            SectionHeader(title = stringResource(R.string.settings_account_section_title))
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_account_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.SmartToy,
                    iconBackground = LocalAppColors.current.settingsNeutralIconBackground,
                    iconTint = LocalAppColors.current.settingsNeutralIconTint,
                    title = profileTitle,
                    subtitle = stringResource(R.string.settings_pro_desc),
                    showArrow = false
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    iconBackground = LocalAppColors.current.settingsDestructiveIconBackground,
                    iconTint = LocalAppColors.current.settingsDestructiveIconTint,
                    title = stringResource(R.string.settings_logout_action),
                    subtitle = null,
                    showArrow = true,
                    modifier = Modifier.clickable {
                        onLogout()
                    }
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            // ── General section ─────────────────────────────────────────────
            SectionHeader(title = stringResource(R.string.settings_general_section_title))
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_general_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.Language,
                    iconBackground = LocalAppColors.current.settingsLanguageIconBackground,
                    iconTint = LocalAppColors.current.settingsLanguageIconTint,
                    title = stringResource(R.string.settings_app_languages_action),
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.ShoppingCart,
                    iconBackground = LocalAppColors.current.settingsPurchaseIconBackground,
                    iconTint = LocalAppColors.current.settingsPurchaseIconTint,
                    title = stringResource(R.string.settings_restore_purchase_action),
                    subtitle = null,
                    showArrow = true
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            // ── Other section ───────────────────────────────────────────────
            SectionHeader(title = stringResource(R.string.settings_other_section_title))
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_other_section")
            ) {
                SettingRowItem(
                    icon = Icons.Outlined.ThumbUp,
                    iconBackground = LocalAppColors.current.settingsRatingIconBackground,
                    iconTint = LocalAppColors.current.settingsRatingIconTint,
                    title = stringResource(R.string.settings_rate_us_action),
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Feedback,
                    iconBackground = LocalAppColors.current.settingsNeutralIconBackground,
                    iconTint = LocalAppColors.current.settingsNeutralIconTint,
                    title = stringResource(R.string.settings_feedback_action),
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.IosShare,
                    iconBackground = LocalAppColors.current.settingsShareIconBackground,
                    iconTint = LocalAppColors.current.settingsShareIconTint,
                    title = stringResource(R.string.settings_share_action),
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.PrivacyTip,
                    iconBackground = LocalAppColors.current.settingsPrivacyIconBackground,
                    iconTint = LocalAppColors.current.settingsPrivacyIconTint,
                    title = stringResource(R.string.settings_privacy_policy_action),
                    subtitle = null,
                    showArrow = true
                )
                SettingsDivider()
                SettingRowItem(
                    icon = Icons.Outlined.Description,
                    iconBackground = LocalAppColors.current.settingsTermsIconBackground,
                    iconTint = LocalAppColors.current.primary,
                    title = stringResource(R.string.settings_terms_of_use_action),
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
            text = stringResource(R.string.settings_ai_notes),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = LocalAppColors.current.textPrimary,
                letterSpacing = (-0.5).sp
            ),
            modifier = Modifier.weight(1f)
        )
        // "Pro" badge
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LocalAppColors.current.proBadgeStart, LocalAppColors.current.proBadgeEnd),
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
                Text(
                    text = "♛",
                    color = LocalAppColors.current.onAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_pro),
                    color = LocalAppColors.current.onAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
                    colors = listOf(LocalAppColors.current.heroBannerStart, LocalAppColors.current.heroBannerEnd),
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
                    text = stringResource(R.string.settings_ai_notes_desc),
                    color = LocalAppColors.current.onAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
                Text(
                    text = stringResource(R.string.settings_pro_desc),
                    color = LocalAppColors.current.onAccent.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // CTA "Upgrade to pro" button
                Box(
                    modifier = Modifier
                        .background(
                            color = LocalAppColors.current.onAccent.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = LocalAppColors.current.onAccent.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_upgrade_pro),
                        color = LocalAppColors.current.onAccent,
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
                            colors = listOf(LocalAppColors.current.primary, LocalAppColors.current.secondary)
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
            color = LocalAppColors.current.onAccent.copy(alpha = 0.30f),
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
            color = LocalAppColors.current.textSecondary,
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
private fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = LocalAppColors.current.border, shape = RoundedCornerShape(12.dp))
            .background(LocalAppColors.current.surface)
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
                    color = LocalAppColors.current.textPrimary
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalAppColors.current.textSecondary
                )
            }
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = LocalAppColors.current.textTertiary,
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
        color = LocalAppColors.current.divider,
        thickness = 0.5.dp
    )
}
