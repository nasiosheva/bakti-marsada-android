package com.lampung.baktimarsada.feature.profile.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.lampung.baktimarsada.ui.component.BaktiValueRow
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileHeader(session = session)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_tenant_label),
                        value = session.tenantContext.tenantName
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_sub_tenant_label),
                        value = session.tenantContext.subTenantName
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_sector_label),
                        value = session.sectorContext.sectorName
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_user_id_label),
                        value = session.userId
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_environment_label),
                        value = AppBuildConfig.appEnvironment
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.profile_data_source_label),
                        value = AppBuildConfig.dataSourceLabel
                    )
                }
            }
            Button(
                onClick = onTestSendNotification,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "profile_test_send_notification_button" }
            ) {
                Text(text = stringResource(id = R.string.action_test_send_notification))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { showLogoutSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "profile_logout_button" }
            ) {
                Text(text = stringResource(id = R.string.action_logout))
            }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.displayName.take(1).ifBlank { "B" }.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.profile_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
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

// created by Mories Deo Hutapea, S.E.,S.Kom
