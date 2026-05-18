package com.lampung.baktimarsada.feature.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import com.lampung.baktimarsada.ui.component.BaktiBottomSheet
import com.lampung.baktimarsada.ui.component.JemaatPill

@Composable
fun ProfileRoute(
    session: SessionState,
    onLogout: () -> Unit,
    onTestSendNotification: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    ProfileScreen(
        session = session,
        onLogout = onLogout,
        onTestSendNotification = onTestSendNotification,
        onBack = onBack
    )
}

@Composable
fun ProfileScreen(
    session: SessionState,
    onLogout: () -> Unit,
    onTestSendNotification: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    var showLogoutSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            onBack?.let {
                BaktiToolbar(
                    title = stringResource(id = R.string.profile_title),
                    onBack = it
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileHeader(session = session)
            ProfileInfoSection(session = session)
            ProfileActionCard(
                icon = Icons.Filled.Notifications,
                title = stringResource(id = R.string.action_test_send_notification),
                onClick = onTestSendNotification,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "profile_test_send_notification_button" },
                isPrimary = true
            )
            ProfileActionCard(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(id = R.string.action_logout),
                onClick = { showLogoutSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "profile_logout_button" },
                isDestructive = true
            )
        }
    }

    if (showLogoutSheet) {
        BaktiBottomSheet(
            onDismissRequest = { showLogoutSheet = false },
            title = stringResource(id = R.string.logout_confirm_title)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(id = R.string.logout_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showLogoutSheet = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(id = R.string.action_cancel))
                    }
                    Button(
                        onClick = {
                            showLogoutSheet = false
                            onLogout()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.action_logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(session: SessionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.displayName.toProfileInitials(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.profile_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JemaatPill(
                    text = when (session.role) {
                        UserRole.ADMIN -> stringResource(id = R.string.profile_role_admin)
                        UserRole.JEMAAT -> stringResource(id = R.string.profile_role_jemaat)
                    }
                )
                if (AppBuildConfig.simulationEnabled) {
                    JemaatPill(text = stringResource(id = R.string.environment_simulate))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(
    session: SessionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_info_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ProfileInfoRow(
                icon = Icons.Filled.Business,
                label = stringResource(id = R.string.profile_tenant_label),
                value = session.tenantContext.tenantName
            )
            ProfileInfoRow(
                icon = Icons.Filled.AccountCircle,
                label = stringResource(id = R.string.profile_sub_tenant_label),
                value = session.tenantContext.subTenantName
            )
            ProfileInfoRow(
                icon = Icons.Filled.Groups,
                label = stringResource(id = R.string.profile_sector_label),
                value = session.sectorContext.sectorName
            )
            ProfileInfoRow(
                icon = Icons.Filled.Person,
                label = stringResource(id = R.string.profile_user_id_label),
                value = session.userId
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileCompactInfoCard(
                    icon = Icons.Filled.CloudQueue,
                    label = stringResource(id = R.string.profile_environment_label),
                    value = AppBuildConfig.appEnvironment,
                    modifier = Modifier.weight(1f)
                )
                ProfileCompactInfoCard(
                    icon = Icons.Filled.Storage,
                    label = stringResource(id = R.string.profile_data_source_label),
                    value = AppBuildConfig.dataSourceLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.height(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileCompactInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false
) {
    val containerColor = when {
        isDestructive -> MaterialTheme.colorScheme.errorContainer
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isDestructive -> MaterialTheme.colorScheme.onErrorContainer
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String.toProfileInitials(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "B" }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
