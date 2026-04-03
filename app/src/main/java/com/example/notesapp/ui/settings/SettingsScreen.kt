package com.example.notesapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.LavenderPrimary
import com.example.notesapp.ui.theme.SurfaceCard
import com.example.notesapp.ui.theme.TextSecondary

@Composable
fun SettingsScreen(parentPadding: PaddingValues) {
    Scaffold(modifier = Modifier.padding(parentPadding)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Manage your account and app preferences.", style = MaterialTheme.typography.bodyMedium)
            }

            Surface(shape = RoundedCornerShape(24.dp), color = SurfaceCard) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LavenderPrimary)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(text = "N", color = androidx.compose.ui.graphics.Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Notes User", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Offline-first workspace", color = TextSecondary)
                    }
                }
            }

            SettingRow("Appearance", "Light theme", Icons.Outlined.Palette)
            SettingRow("Sync", "Not connected", Icons.Outlined.Sync)
            SettingRow("Notifications", "Daily reminders off", Icons.Outlined.Notifications)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = RoundedCornerShape(20.dp), color = SurfaceCard) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
