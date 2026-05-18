package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiItemListOrEmpty
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
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
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            AdminCreateUserFloatingActionButton(
                onClick = { showCreateDialog = true },
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
            errorMessage = state.errorMessage,
            successMessage = state.successMessage,
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

    if (showCreateDialog) {
        AdminCreateUserDialog(
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
            labelFullName = stringResource(id = R.string.form_full_name),
            onDismiss = { showCreateDialog = false },
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
                showCreateDialog = false
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
}

@Composable
private fun AdminUserCreateContent(
    title: String,
    onBack: () -> Unit,
    errorMessage: String?,
    successMessage: String?,
    selectedTab: UserRoleFilter,
    tabs: List<Pair<UserRoleFilter, String>>,
    onSelectedTabChange: (UserRoleFilter) -> Unit,
    items: List<CreateUserAccountResponseDto>,
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
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
        ) {
            errorMessage?.let {
                BaktiSectionMessage(message = it)
            }
            successMessage?.let {
                BaktiSectionMessage(message = it)
            }

            BaktiTabRow(
                selectedTab = selectedTab,
                tabs = tabs,
                onSelectedTabChange = onSelectedTabChange
            )

            BaktiItemListOrEmpty(
                items = items,
                emptyMessage = emptyMessage,
                itemKey = itemKey,
                itemContent = itemContent
            )
        }
    }
}

@Composable
private fun AdminCreateUserItem(
    fullName: String,
    username: String,
    email: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$fullName ($username)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall
        )
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
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun AdminCreateUserDialog(
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
    labelFullName: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(jemaatRoleValue) }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    val canCreate = !isLoading &&
        username.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        fullName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    visualTransformation = PasswordVisualTransformation()
                )
                BaktiTextInput(
                    value = fullName,
                    label = labelFullName,
                    onValueChange = { fullName = it }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = actionCancelLabel)
            }
        },
        confirmButton = {
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
                enabled = canCreate
            ) {
                Text(text = actionSaveLabel)
            }
        }
    )
}

@Preview(name = "Admin user create dialog")
@Composable
private fun AdminUserCreateDialogPreview() {
    BaktiMarsadaTheme {
        AdminCreateUserDialog(
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
            labelFullName = "Nama Lengkap",
            onDismiss = {},
            onCreate = { _, _, _, _, _ -> }
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
            errorMessage = null,
            successMessage = null,
            selectedTab = UserRoleFilter.ADMIN,
            tabs = listOf(
                UserRoleFilter.JEMAAT to "Jemaat",
                UserRoleFilter.ADMIN to "Admin"
            ),
            onSelectedTabChange = {},
            contentPadding = PaddingValues(0.dp),
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
