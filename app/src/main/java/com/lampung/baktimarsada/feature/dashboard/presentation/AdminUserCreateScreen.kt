package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.ui.component.BaktiBottomSheet
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiResponseSnackbarEffect
import com.lampung.baktimarsada.ui.component.BaktiSnackbarHost
import com.lampung.baktimarsada.ui.component.BaktiTabRow
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun AdminUserCreateRoute(
    onBack: () -> Unit,
    viewModel: AdminUserCreateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title = stringResource(id = R.string.admin_create_user_title)
    val adminRoleLabel = stringResource(id = R.string.profile_role_admin)
    val jemaatRoleLabel = stringResource(id = R.string.profile_role_jemaat)
    var selectedTab by rememberSaveable { mutableStateOf(UserRoleFilter.JEMAAT) }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            BaktiSnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AdminCreateUserFloatingActionButton(
                onClick = { showCreateSheet = true },
                contentDescription = stringResource(id = R.string.action_add_user)
            )
        }
    ) { innerPadding ->
        val adjustedPadding = PaddingValues(
            start = 0.dp,
            top = 0.dp,
            end = 0.dp,
            bottom = innerPadding.calculateBottomPadding()
        )
        AdminUserCreateContent(
            title = title,
            onBack = onBack,
            selectedTab = selectedTab,
            tabs = listOf(
                UserRoleFilter.JEMAAT to jemaatRoleLabel,
                UserRoleFilter.ADMIN to adminRoleLabel
            ),
            onSelectedTabChange = { newTab ->
                selectedTab = newTab
                viewModel.refreshUsers(newTab.apiValue)
            },
            contentPadding = adjustedPadding,
            items = selectedTab.resolveUsers(state),
            adminCount = state.adminUsers.size,
            jemaatCount = state.jemaatUsers.size,
            emptyMessage = stringResource(id = R.string.user_list_empty),
            itemKey = { it.id },
            itemContent = { user ->
                AdminCreateUserItem(
                    fullName = user.fullName,
                    username = user.username ?: user.email,
                    email = user.email
                )
            }
        )
    }

    BaktiResponseSnackbarEffect(
        message = state.errorMessage,
        hostState = snackbarHostState,
        onMessageConsumed = viewModel::clearErrorMessage
    )
    BaktiResponseSnackbarEffect(
        message = state.successMessage,
        hostState = snackbarHostState,
        onMessageConsumed = viewModel::clearSuccessMessage
    )

    if (showCreateSheet) {
        AdminCreateUserSheet(
            isLoading = state.isLoading,
            adminRoleLabel = adminRoleLabel,
            jemaatRoleLabel = jemaatRoleLabel,
            adminRoleValue = UserRoleFilter.ADMIN.apiValue,
            jemaatRoleValue = UserRoleFilter.JEMAAT.apiValue,
            title = title,
            actionCancelLabel = stringResource(id = R.string.action_cancel),
            actionSaveLabel = stringResource(id = R.string.action_save),
            labelUsername = stringResource(id = R.string.form_username),
            labelEmail = stringResource(id = R.string.form_email),
            labelRole = stringResource(id = R.string.form_role),
            labelPassword = stringResource(id = R.string.login_password_label),
            passwordMinLengthMessage = stringResource(id = R.string.admin_create_user_password_min_length),
            labelFullName = stringResource(id = R.string.form_full_name),
            onDismiss = { showCreateSheet = false },
            onCreate = { username, email, role, password, fullName ->
                viewModel.createUser(
                    CreateUserAccountRequestDto(
                        username = username,
                        email = email,
                        role = role,
                        password = password,
                        fullName = fullName
                    )
                )
                showCreateSheet = false
            }
        )
    }
}

private enum class UserRoleFilter(val apiValue: String) {
    JEMAAT("JEMAAT"),
    ADMIN("ADMIN");

    fun resolveUsers(state: AdminUserCreateUiState): List<CreateUserAccountResponseDto> {
        return when (this) {
            JEMAAT -> state.jemaatUsers
            ADMIN -> state.adminUsers
        }
    }
}

data class AdminUserCreateUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val adminUsers: List<CreateUserAccountResponseDto> = emptyList(),
    val jemaatUsers: List<CreateUserAccountResponseDto> = emptyList()
)

@HiltViewModel
class AdminUserCreateViewModel @Inject constructor(
    private val remoteDataSource: AppRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val stringProvider: StringProvider
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUserCreateUiState())
    val state: StateFlow<AdminUserCreateUiState> = _state.asStateFlow()

    init {
        refreshUsers("JEMAAT")
        refreshUsers("ADMIN")
    }

    fun createUser(request: CreateUserAccountRequestDto) {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            runCatching {
                remoteDataSource.createUserAccount(request)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = stringProvider.get(R.string.admin_create_user_success)
                    )
                }
                refreshUsers(request.role)
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: stringProvider.get(R.string.admin_create_user_error_default)
                    )
                }
            }
        }
    }

    fun refreshUsers(role: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            runCatching { remoteDataSource.fetchUsersByRole(role) }
                .onSuccess { users ->
                    _state.update {
                        if (role.trim().uppercase() == "ADMIN") {
                            it.copy(adminUsers = users, errorMessage = null)
                        } else {
                            it.copy(jemaatUsers = users, errorMessage = null)
                        }
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(errorMessage = throwable.message ?: stringProvider.get(R.string.admin_create_user_error_default))
                    }
                }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}

@Composable
private fun AdminUserCreateContent(
    title: String,
    onBack: () -> Unit,
    selectedTab: UserRoleFilter,
    tabs: List<Pair<UserRoleFilter, String>>,
    onSelectedTabChange: (UserRoleFilter) -> Unit,
    items: List<CreateUserAccountResponseDto>,
    adminCount: Int,
    jemaatCount: Int,
    emptyMessage: String,
    itemKey: (CreateUserAccountResponseDto) -> Any,
    itemContent: @Composable (CreateUserAccountResponseDto) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        BaktiToolbar(
            title = title,
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    AdminUserHeroCard(
                        adminCount = adminCount,
                        jemaatCount = jemaatCount
                    )
                }

                item {
                    BaktiTabRow(
                        selectedTab = selectedTab,
                        tabs = tabs,
                        onSelectedTabChange = onSelectedTabChange
                    )
                }

                if (items.isEmpty()) {
                    item {
                        AdminUserEmptyCard(message = emptyMessage)
                    }
                } else {
                    items(
                        items = items,
                        key = itemKey
                    ) { user ->
                        itemContent(user)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserHeroCard(
    adminCount: Int,
    jemaatCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(id = R.string.admin_user_access_badge),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.admin_user_manage_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.admin_user_manage_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminUserMetricCard(
                    label = stringResource(id = R.string.profile_role_jemaat),
                    value = jemaatCount.toString(),
                    icon = Icons.Filled.Groups,
                    modifier = Modifier.weight(1f)
                )
                AdminUserMetricCard(
                    label = stringResource(id = R.string.profile_role_admin),
                    value = adminCount.toString(),
                    icon = Icons.Filled.AdminPanelSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AdminUserMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.height(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdminUserEmptyCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminCreateUserItem(
    fullName: String,
    username: String,
    email: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.height(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.toUserInitials(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminCreateUserFloatingActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun AdminCreateUserSheet(
    isLoading: Boolean,
    adminRoleLabel: String,
    jemaatRoleLabel: String,
    adminRoleValue: String,
    jemaatRoleValue: String,
    title: String,
    actionCancelLabel: String,
    actionSaveLabel: String,
    labelUsername: String,
    labelEmail: String,
    labelRole: String,
    labelPassword: String,
    passwordMinLengthMessage: String,
    labelFullName: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit,
    initialPassword: String = ""
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(jemaatRoleValue) }
    var password by rememberSaveable { mutableStateOf(initialPassword) }
    var fullName by rememberSaveable { mutableStateOf("") }
    val isPasswordLengthValid = password.length >= 8
    val showPasswordLengthError = password.isNotEmpty() && !isPasswordLengthValid
    val canCreate = !isLoading &&
        username.isNotBlank() &&
        email.isNotBlank() &&
        isPasswordLengthValid &&
        fullName.isNotBlank()

    BaktiBottomSheet(
        onDismissRequest = onDismiss,
        title = title
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.admin_user_create_sheet_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            BaktiTextInput(
                value = username,
                label = labelUsername,
                onValueChange = { username = it }
            )
            BaktiTextInput(
                value = email,
                label = labelEmail,
                onValueChange = { email = it }
            )
            BaktiDropdown(
                selectedValue = if (role == adminRoleValue) adminRoleLabel else jemaatRoleLabel,
                label = labelRole,
                options = listOf(jemaatRoleLabel, adminRoleLabel),
                onValueSelected = { selected ->
                    role = if (selected == adminRoleLabel) adminRoleValue else jemaatRoleValue
                }
            )
            BaktiTextInput(
                value = password,
                label = labelPassword,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                isError = showPasswordLengthError,
                errorMessage = if (showPasswordLengthError) passwordMinLengthMessage else ""
            )
            BaktiTextInput(
                value = fullName,
                label = labelFullName,
                onValueChange = { fullName = it }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = actionCancelLabel)
                }
                Button(
                    onClick = {
                        onCreate(
                            username.trim(),
                            email.trim(),
                            role,
                            password,
                            fullName.trim()
                        )
                    },
                    enabled = canCreate,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = actionSaveLabel)
                }
            }
        }
    }
}

private fun String.toUserInitials(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
}

@Preview(name = "Admin user create sheet")
@Composable
private fun AdminUserCreateSheetPreview() {
    BaktiMarsadaTheme {
        AdminCreateUserSheet(
            isLoading = false,
            adminRoleLabel = "Admin",
            jemaatRoleLabel = "Jemaat",
            adminRoleValue = UserRoleFilter.ADMIN.apiValue,
            jemaatRoleValue = UserRoleFilter.JEMAAT.apiValue,
            title = "Tambah User",
            actionCancelLabel = "Batal",
            actionSaveLabel = "Simpan",
            labelUsername = "Username",
            labelEmail = "Email",
            labelRole = "Role",
            labelPassword = "Password",
            passwordMinLengthMessage = "Password minimal 8 karakter",
            labelFullName = "Nama Lengkap",
            onDismiss = {},
            onCreate = { _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Admin user create sheet - invalid password")
@Composable
private fun AdminUserCreateSheetInvalidPasswordPreview() {
    BaktiMarsadaTheme {
        AdminCreateUserSheet(
            isLoading = false,
            adminRoleLabel = "Admin",
            jemaatRoleLabel = "Jemaat",
            adminRoleValue = UserRoleFilter.ADMIN.apiValue,
            jemaatRoleValue = UserRoleFilter.JEMAAT.apiValue,
            title = "Tambah User",
            actionCancelLabel = "Batal",
            actionSaveLabel = "Simpan",
            labelUsername = "Username",
            labelEmail = "Email",
            labelRole = "Role",
            labelPassword = "Password",
            passwordMinLengthMessage = "Password minimal 8 karakter",
            labelFullName = "Nama Lengkap",
            onDismiss = {},
            onCreate = { _, _, _, _, _ -> },
            initialPassword = "short"
        )
    }
}

@Preview(name = "Admin user create tabs")
@Composable
private fun AdminUserCreateContentPreview() {
    BaktiMarsadaTheme {
        AdminUserCreateContent(
            title = "Buat User",
            onBack = {},
            selectedTab = UserRoleFilter.ADMIN,
            tabs = listOf(
                UserRoleFilter.JEMAAT to "Jemaat",
                UserRoleFilter.ADMIN to "Admin"
            ),
            onSelectedTabChange = {},
            contentPadding = PaddingValues(0.dp),
            adminCount = 1,
            jemaatCount = 1,
            items = listOf(
                CreateUserAccountResponseDto(
                    id = "u-1",
                    fullName = "Admin Satu",
                    username = "admin1",
                    email = "admin1@demo.com",
                    role = "ADMIN"
                ),
                CreateUserAccountResponseDto(
                    id = "u-3",
                    fullName = "Jemaat Satu",
                    username = "jemaat1",
                    email = "jemaat1@demo.com",
                    role = "JEMAAT"
                )
            ),
            emptyMessage = "Belum ada data",
            itemKey = { it.id },
            itemContent = { user ->
                AdminCreateUserItem(
                    fullName = user.fullName,
                    username = user.username ?: user.email,
                    email = user.email
                )
            }
        )
    }
}
