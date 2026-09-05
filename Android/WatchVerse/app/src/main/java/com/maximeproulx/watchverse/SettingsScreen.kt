package com.maximeproulx.watchverse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SettingsGold = Color(
    red = 0.87f,
    green = 0.74f,
    blue = 0.28f
)

@Composable
fun SettingsScreen(
    universe: Universe,
    showReleaseYears: Boolean,
    notifyNewUniverses: Boolean,
    displayName: String = "",
    accountSubtitle: String = "",
    appVersion: String = "",
    onShowReleaseYearsChanged: (Boolean) -> Unit = {},
    onNotifyNewUniversesChanged: (Boolean) -> Unit = {},
    onResetUniverseProgress: () -> Unit = {},
    onResetAllProgress: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var showResetUniverseDialog by remember { mutableStateOf(false) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.appbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 140.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color(0xFF2C2C2C).copy(alpha = 0.85f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_close),
                        contentDescription = "Close Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "WATCHING") {
                SettingsToggleRow(
                    iconRes = R.drawable.ic_settings_calendar,
                    title = "Show Release Years",
                    checked = showReleaseYears,
                    onCheckedChange = onShowReleaseYearsChanged
                )

                SettingsDivider()

                SettingsComingSoonRow(
                    iconRes = R.drawable.ic_settings_bell,
                    title = "Spoiler Protection"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "NOTIFICATIONS") {
                SettingsToggleRow(
                    iconRes = R.drawable.ic_settings_bell,
                    title = "Notify About New Universes",
                    checked = notifyNewUniverses,
                    onCheckedChange = onNotifyNewUniversesChanged
                )

                SettingsDivider()

                SettingsComingSoonRow(
                    iconRes = R.drawable.ic_settings_tv,
                    title = "New Episode Alerts"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "DATA") {
                SettingsActionRow(
                    iconRes = R.drawable.ic_settings_reset,
                    title = "Reset ${universe.title} Progress",
                    textColor = Color.White,
                    onClick = {
                        showResetUniverseDialog = true
                    }
                )

                SettingsDivider()

                SettingsActionRow(
                    iconRes = R.drawable.ic_settings_trash,
                    title = "Reset All Universe Progress",
                    textColor = Color.Red,
                    onClick = {
                        showResetAllDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "ACCOUNT") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_person),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Column {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (accountSubtitle.isNotBlank()) {
                            Text(
                                text = accountSubtitle,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                SettingsDivider()

                SettingsActionRow(
                    iconRes = R.drawable.ic_settings_logout,
                    title = "Log Out",
                    textColor = Color.Red,
                    onClick = {
                        showLogoutDialog = true
                    }
                )

                SettingsDivider()

                SettingsActionRow(
                    iconRes = R.drawable.ic_settings_delete_account,
                    title = "Delete Account",
                    textColor = Color.Red,
                    onClick = {
                        showDeleteAccountDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = "ABOUT") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_info),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Text(
                        text = "Version",
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = appVersion,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }

                SettingsDivider()

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Legal Disclaimer",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = """
                            WatchVerse is an independent app and is not affiliated with or endorsed by any rights holder.

                            This product uses the TMDB API but is not endorsed or certified by TMDB.

                            All third-party trademarks, titles, logos, characters, artwork, and related intellectual property belong to their respective owners.
                        """.trimIndent(),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    if (showResetUniverseDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetUniverseDialog = false
            },
            title = {
                Text("Reset ${universe.title} Progress?")
            },
            text = {
                Text(
                    "This will mark every movie in ${universe.title} as unwatched."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetUniverseDialog = false
                        onResetUniverseProgress()
                    }
                ) {
                    Text(
                        text = "Reset",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetUniverseDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetAllDialog = false
            },
            title = {
                Text("Reset All Universe Progress?")
            },
            text = {
                Text(
                    "This will mark every movie in every universe as unwatched."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetAllDialog = false
                        onResetAllProgress()
                    }
                ) {
                    Text(
                        text = "Reset",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetAllDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text("Log Out?")
            },
            text = {
                Text(
                    "You will need to sign in again to access your WatchVerse account."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Log Out",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAccountDialog = false
            },
            title = {
                Text("Delete Account?")
            },
            text = {
                Text(
                    "This will permanently delete your WatchVerse account, progress, badges, and settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    iconRes: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsGold
            )
        )
    }
}

@Composable
private fun SettingsComingSoonRow(
    iconRes: Int,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Coming Soon",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsActionRow(
    iconRes: Int,
    title: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            color = textColor,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.08f)
    )
}
