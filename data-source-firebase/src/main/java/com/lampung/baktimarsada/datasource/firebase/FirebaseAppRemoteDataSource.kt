package com.lampung.baktimarsada.datasource.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.core.constants.DataSourceProvider
import com.lampung.baktimarsada.core.di.DataSourceBindingKey
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.EventProgramItemDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateItemDto
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAppRemoteDataSource @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore?
) : AppRemoteDataSource {

    private val firestore: FirebaseFirestore
        get() = firebaseFirestore ?: error("Firebase Firestore is unavailable")

    override suspend fun login(request: LoginRequestDto): SessionResponseDto {
        val normalizedIdentifier = request.identifier.trim().lowercase()
        val userDoc = firestore.collection(AppConstants.FIRESTORE_COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_IDENTIFIER_NORMALIZED, normalizedIdentifier)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_PASSWORD, request.password)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val fallbackAccount = fallbackAccount(request)
        if (userDoc == null && fallbackAccount == null) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val token = "firebase-${UUID.randomUUID()}"
        val userId = userDoc?.id ?: fallbackAccount!!.userId
        val role = (userDoc?.stringField(AppConstants.FIRESTORE_FIELD_ROLE) ?: fallbackAccount?.role)
            .orEmpty()
            .uppercase()
            .ifBlank { "JEMAAT" }
        val displayName = userDoc?.stringField(AppConstants.FIRESTORE_FIELD_DISPLAY_NAME)
            ?: fallbackAccount?.displayName
            ?: userId
        val sectorId = userDoc?.stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID)
            ?: fallbackAccount?.sectorId
            ?: TenantRuntime.current.defaultSectorId
        val sectorName = userDoc?.stringField(AppConstants.FIRESTORE_FIELD_SECTOR_NAME)
            ?: fallbackAccount?.sectorName
            ?: TenantRuntime.current.defaultSectorName

        return SessionResponseDto(
            authToken = token,
            userId = userId,
            displayName = displayName,
            role = role,
            tenantId = TenantRuntime.current.tenantId,
            tenantName = TenantRuntime.current.tenantName,
            subTenantId = TenantRuntime.current.subTenantId,
            subTenantName = TenantRuntime.current.subTenantName,
            sectorId = sectorId,
            sectorName = sectorName
        )
    }

    override suspend fun loginWithGoogle(request: GoogleLoginRequestDto): SessionResponseDto {
        if (request.idToken.isBlank()) {
            throw IllegalArgumentException("Google login token is required")
        }
        return SessionResponseDto(
            authToken = "firebase-google-${UUID.randomUUID()}",
            userId = "firebase-google-user",
            displayName = "Google Jemaat",
            role = "JEMAAT",
            tenantId = TenantRuntime.current.tenantId,
            tenantName = TenantRuntime.current.tenantName,
            subTenantId = TenantRuntime.current.subTenantId,
            subTenantName = TenantRuntime.current.subTenantName,
            sectorId = TenantRuntime.current.defaultSectorId,
            sectorName = TenantRuntime.current.defaultSectorName
        )
    }

    override suspend fun logout(token: String): Unit = Unit

    override suspend fun syncFcmToken(token: String): Unit = Unit

    override suspend fun fetchEvents(sectorId: String): List<EventDto> {
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_EVENTS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_SECTOR_ID, sectorId)
            .orderBy(AppConstants.FIRESTORE_FIELD_SCHEDULED_AT, Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toEventDto() }
    }

    override suspend fun saveEvent(event: EventDto): EventDto {
        val resolved = event.withIdIfNeeded(prefix = "event")
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_EVENTS)
            .document(resolved.id)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_TITLE to resolved.title,
                    AppConstants.FIRESTORE_FIELD_DESCRIPTION to resolved.description,
                    AppConstants.FIRESTORE_FIELD_SCHEDULED_AT to resolved.scheduledAt,
                    AppConstants.FIRESTORE_FIELD_LOCATION to resolved.location,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to resolved.sectorId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to resolved.sectorName,
                    AppConstants.FIRESTORE_FIELD_PROGRAM_ITEMS to resolved.programItems.map { it.toMap() }
                )
            )
            .await()
        return resolved
    }

    override suspend fun deleteEvent(eventId: String) {
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_EVENTS)
            .document(eventId)
            .delete()
            .await()
    }

    override suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto> {
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_WORSHIP_TEMPLATES)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_SECTOR_ID, sectorId)
            .orderBy(AppConstants.FIRESTORE_FIELD_TITLE, Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toWorshipTemplateDto() }
    }

    override suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto {
        val resolved = template.withIdIfNeeded(prefix = "template")
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_WORSHIP_TEMPLATES)
            .document(resolved.id)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_TENANT_ID to resolved.tenantId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to resolved.sectorId,
                    AppConstants.FIRESTORE_FIELD_TITLE to resolved.title,
                    AppConstants.FIRESTORE_FIELD_DESCRIPTION to resolved.description,
                    AppConstants.FIRESTORE_FIELD_TEMPLATE_ITEMS to resolved.items.map { it.toMap() }
                )
            )
            .await()
        return resolved
    }

    override suspend fun deleteWorshipTemplate(templateId: String) {
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_WORSHIP_TEMPLATES)
            .document(templateId)
            .delete()
            .await()
    }

    override suspend fun fetchMembers(sectorId: String): List<MemberDto> {
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_MEMBERS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_SECTOR_ID, sectorId)
            .orderBy(AppConstants.FIRESTORE_FIELD_FULL_NAME, Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toMemberDto() }
    }

    override suspend fun saveMember(member: MemberDto): MemberDto {
        val resolved = member.withIdIfNeeded(prefix = "member")
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_MEMBERS)
            .document(resolved.id)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_FULL_NAME to resolved.fullName,
                    AppConstants.FIRESTORE_FIELD_FAMILY_GROUP to resolved.familyGroup,
                    AppConstants.FIRESTORE_FIELD_PHONE_NUMBER to resolved.phoneNumber,
                    AppConstants.FIRESTORE_FIELD_ADDRESS to resolved.address,
                    AppConstants.FIRESTORE_FIELD_ROLE_IN_SECTOR to resolved.roleInSector,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to resolved.sectorId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to resolved.sectorName
                )
            )
            .await()
        return resolved
    }

    override suspend fun deleteMember(memberId: String) {
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_MEMBERS)
            .document(memberId)
            .delete()
            .await()
    }

    override suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto> {
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_FINANCE_REPORTS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_SECTOR_ID, sectorId)
            .orderBy(AppConstants.FIRESTORE_FIELD_PERIOD_LABEL, Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toFinanceReportDto() }
    }

    override suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto {
        val resolved = report.withIdIfNeeded(prefix = "finance")
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_FINANCE_REPORTS)
            .document(resolved.id)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_TITLE to resolved.title,
                    AppConstants.FIRESTORE_FIELD_DESCRIPTION to resolved.description,
                    AppConstants.FIRESTORE_FIELD_PERIOD_LABEL to resolved.periodLabel,
                    AppConstants.FIRESTORE_FIELD_AMOUNT to resolved.amount,
                    AppConstants.FIRESTORE_FIELD_IS_VISIBLE_TO_JEMAAT to resolved.isVisibleToJemaat,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to resolved.sectorId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to resolved.sectorName
                )
            )
            .await()
        return resolved
    }

    override suspend fun deleteFinanceReport(reportId: String) {
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_FINANCE_REPORTS)
            .document(reportId)
            .delete()
            .await()
    }

    override suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto> {
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_PAYMENT_OBLIGATIONS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_SECTOR_ID, sectorId)
            .orderBy(AppConstants.FIRESTORE_FIELD_DUE_DATE, Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toPaymentObligationDto() }
    }

    override suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto {
        val resolved = obligation.withIdIfNeeded(prefix = "payment")
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_PAYMENT_OBLIGATIONS)
            .document(resolved.id)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_MEMBER_ID to resolved.memberId,
                    AppConstants.FIRESTORE_FIELD_MEMBER_NAME to resolved.memberName,
                    AppConstants.FIRESTORE_FIELD_TITLE to resolved.title,
                    AppConstants.FIRESTORE_FIELD_DESCRIPTION to resolved.description,
                    AppConstants.FIRESTORE_FIELD_AMOUNT to resolved.amount,
                    AppConstants.FIRESTORE_FIELD_DUE_DATE to resolved.dueDate,
                    AppConstants.FIRESTORE_FIELD_STATUS to resolved.status,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to resolved.sectorId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to resolved.sectorName
                )
            )
            .await()
        return resolved
    }

    override suspend fun deletePaymentObligation(obligationId: String) {
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_PAYMENT_OBLIGATIONS)
            .document(obligationId)
            .delete()
            .await()
    }

    override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto {
        val username = request.username.trim().lowercase()
        val email = request.email.trim().lowercase()
        val role = request.role.trim().uppercase().ifBlank { "JEMAAT" }
        val fullName = request.fullName.trim()

        if (username.length < 3) {
            throw IllegalArgumentException("Username must be at least 3 characters")
        }
        if (!email.contains("@")) {
            throw IllegalArgumentException("Invalid email")
        }
        if (request.password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters")
        }
        if (fullName.isBlank()) {
            throw IllegalArgumentException("Full name is required")
        }

        val existingByIdentifier = firestore.collection(AppConstants.FIRESTORE_COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_IDENTIFIER_NORMALIZED, username)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        if (existingByIdentifier != null) {
            throw IllegalArgumentException("Username already exists")
        }

        val existingByEmail = firestore.collection(AppConstants.FIRESTORE_COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_IDENTIFIER_NORMALIZED, email)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        if (existingByEmail != null) {
            throw IllegalArgumentException("Email already exists")
        }

        val userId = "firebase-${UUID.randomUUID()}"
        firestore.collection(AppConstants.FIRESTORE_COLLECTION_USERS)
            .document(userId)
            .set(
                mapOf(
                    AppConstants.FIRESTORE_FIELD_IDENTIFIER to email,
                    AppConstants.FIRESTORE_FIELD_IDENTIFIER_NORMALIZED to email,
                    AppConstants.FIRESTORE_FIELD_PASSWORD to request.password,
                    AppConstants.FIRESTORE_FIELD_DISPLAY_NAME to fullName,
                    AppConstants.FIRESTORE_FIELD_ROLE to role,
                    AppConstants.FIRESTORE_FIELD_SECTOR_ID to TenantRuntime.current.defaultSectorId,
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to TenantRuntime.current.defaultSectorName
                )
            )
            .await()

        return CreateUserAccountResponseDto(
            id = userId,
            username = username,
            email = email,
            role = role,
            fullName = fullName
        )
    }

    override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> {
        val normalizedRole = role.trim().uppercase()
        return firestore.collection(AppConstants.FIRESTORE_COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIRESTORE_FIELD_ROLE, normalizedRole)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val email = doc.stringField(AppConstants.FIRESTORE_FIELD_IDENTIFIER_NORMALIZED) ?: return@mapNotNull null
                val fullName = doc.stringField(AppConstants.FIRESTORE_FIELD_DISPLAY_NAME) ?: ""
                CreateUserAccountResponseDto(
                    id = doc.id,
                    username = email.substringBefore("@"),
                    email = email,
                    role = normalizedRole,
                    fullName = fullName
                )
            }
    }

    override suspend fun resetSimulationData(): Unit = Unit

    private fun fallbackAccount(request: LoginRequestDto): FallbackAccount? {
        val identifier = request.identifier.trim()
        return when {
            identifier.equals(TenantRuntime.current.sampleAdminIdentifier, ignoreCase = true) &&
                request.password == TenantRuntime.current.sampleAdminPassword -> {
                FallbackAccount(
                    userId = "firebase-admin-1",
                    displayName = "Admin Firebase",
                    role = "ADMIN",
                    sectorId = TenantRuntime.current.defaultSectorId,
                    sectorName = TenantRuntime.current.defaultSectorName
                )
            }

            identifier.equals(TenantRuntime.current.sampleJemaatIdentifier, ignoreCase = true) &&
                request.password == TenantRuntime.current.sampleJemaatPassword -> {
                FallbackAccount(
                    userId = "firebase-jemaat-1",
                    displayName = "Jemaat Firebase",
                    role = "JEMAAT",
                    sectorId = TenantRuntime.current.defaultSectorId,
                    sectorName = TenantRuntime.current.defaultSectorName
                )
            }

            else -> null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEventDto(): EventDto? {
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: return null
        val description = stringField(AppConstants.FIRESTORE_FIELD_DESCRIPTION) ?: ""
        val scheduledAt = stringField(AppConstants.FIRESTORE_FIELD_SCHEDULED_AT) ?: ""
        val location = stringField(AppConstants.FIRESTORE_FIELD_LOCATION) ?: ""
        val sectorId = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID) ?: TenantRuntime.current.defaultSectorId
        val sectorName = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_NAME) ?: TenantRuntime.current.defaultSectorName
        return EventDto(
            id = id,
            title = title,
            description = description,
            scheduledAt = scheduledAt,
            location = location,
            sectorId = sectorId,
            sectorName = sectorName,
            programItems = listField(AppConstants.FIRESTORE_FIELD_PROGRAM_ITEMS).mapIndexedNotNull { index, item ->
                item.toEventProgramItemDto(id, index)
            }
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toWorshipTemplateDto(): WorshipTemplateDto? {
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: return null
        val description = stringField(AppConstants.FIRESTORE_FIELD_DESCRIPTION) ?: ""
        val tenantId = stringField(AppConstants.FIRESTORE_FIELD_TENANT_ID) ?: TenantRuntime.current.tenantId
        val sectorId = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID)
        return WorshipTemplateDto(
            id = id,
            tenantId = tenantId,
            sectorId = sectorId,
            title = title,
            description = description,
            items = listField(AppConstants.FIRESTORE_FIELD_TEMPLATE_ITEMS).mapIndexedNotNull { index, item ->
                item.toWorshipTemplateItemDto(id, index)
            }
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMemberDto(): MemberDto? {
        val fullName = stringField(AppConstants.FIRESTORE_FIELD_FULL_NAME) ?: return null
        val familyGroup = stringField(AppConstants.FIRESTORE_FIELD_FAMILY_GROUP) ?: ""
        val phoneNumber = stringField(AppConstants.FIRESTORE_FIELD_PHONE_NUMBER) ?: ""
        val address = stringField(AppConstants.FIRESTORE_FIELD_ADDRESS) ?: ""
        val roleInSector = stringField(AppConstants.FIRESTORE_FIELD_ROLE_IN_SECTOR) ?: ""
        val sectorId = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID) ?: TenantRuntime.current.defaultSectorId
        val sectorName = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_NAME) ?: TenantRuntime.current.defaultSectorName
        return MemberDto(
            id = id,
            fullName = fullName,
            familyGroup = familyGroup,
            phoneNumber = phoneNumber,
            address = address,
            roleInSector = roleInSector,
            sectorId = sectorId,
            sectorName = sectorName
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFinanceReportDto(): FinanceReportDto? {
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: return null
        val description = stringField(AppConstants.FIRESTORE_FIELD_DESCRIPTION) ?: ""
        val periodLabel = stringField(AppConstants.FIRESTORE_FIELD_PERIOD_LABEL) ?: ""
        val amount = longField(AppConstants.FIRESTORE_FIELD_AMOUNT)
        val isVisibleToJemaat = booleanField(AppConstants.FIRESTORE_FIELD_IS_VISIBLE_TO_JEMAAT)
        val sectorId = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID) ?: TenantRuntime.current.defaultSectorId
        val sectorName = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_NAME) ?: TenantRuntime.current.defaultSectorName
        return FinanceReportDto(
            id = id,
            title = title,
            description = description,
            periodLabel = periodLabel,
            amount = amount,
            isVisibleToJemaat = isVisibleToJemaat,
            sectorId = sectorId,
            sectorName = sectorName
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPaymentObligationDto(): PaymentObligationDto? {
        val memberId = stringField(AppConstants.FIRESTORE_FIELD_MEMBER_ID) ?: return null
        val memberName = stringField(AppConstants.FIRESTORE_FIELD_MEMBER_NAME) ?: ""
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: ""
        val description = stringField(AppConstants.FIRESTORE_FIELD_DESCRIPTION) ?: ""
        val amount = longField(AppConstants.FIRESTORE_FIELD_AMOUNT)
        val dueDate = stringField(AppConstants.FIRESTORE_FIELD_DUE_DATE) ?: ""
        val status = stringField(AppConstants.FIRESTORE_FIELD_STATUS) ?: "UNPAID"
        val sectorId = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_ID) ?: TenantRuntime.current.defaultSectorId
        val sectorName = stringField(AppConstants.FIRESTORE_FIELD_SECTOR_NAME) ?: TenantRuntime.current.defaultSectorName
        return PaymentObligationDto(
            id = id,
            memberId = memberId,
            memberName = memberName,
            title = title,
            description = description,
            amount = amount,
            dueDate = dueDate,
            status = status,
            sectorId = sectorId,
            sectorName = sectorName
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.stringField(field: String): String? {
        return getString(field)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.longField(field: String): Long {
        return getLong(field) ?: 0L
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.booleanField(field: String): Boolean {
        return getBoolean(field) ?: false
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.listField(field: String): List<Map<String, Any?>> {
        return (get(field) as? List<*>)?.mapNotNull { value ->
            (value as? Map<*, *>)?.entries?.associate { (key, itemValue) -> key.toString() to itemValue }
        }.orEmpty()
    }

    private fun Map<String, Any?>.stringField(field: String): String? {
        return (this[field] as? String)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun Map<String, Any?>.longField(field: String): Long {
        val value = this[field]
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            else -> 0L
        }
    }

    private fun Map<String, Any?>.toEventProgramItemDto(eventId: String, fallbackIndex: Int): EventProgramItemDto? {
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: return null
        return EventProgramItemDto(
            id = stringField(AppConstants.FIRESTORE_FIELD_ID) ?: "$eventId-program-$fallbackIndex",
            eventId = stringField(AppConstants.FIRESTORE_FIELD_EVENT_ID) ?: eventId,
            orderIndex = longField(AppConstants.FIRESTORE_FIELD_ORDER_INDEX).toInt(),
            title = title,
            content = stringField(AppConstants.FIRESTORE_FIELD_CONTENT) ?: "",
            leader = stringField(AppConstants.FIRESTORE_FIELD_LEADER) ?: "",
            type = stringField(AppConstants.FIRESTORE_FIELD_TYPE) ?: "CUSTOM"
        )
    }

    private fun Map<String, Any?>.toWorshipTemplateItemDto(templateId: String, fallbackIndex: Int): WorshipTemplateItemDto? {
        val title = stringField(AppConstants.FIRESTORE_FIELD_TITLE) ?: return null
        return WorshipTemplateItemDto(
            id = stringField(AppConstants.FIRESTORE_FIELD_ID) ?: "$templateId-item-$fallbackIndex",
            templateId = stringField(AppConstants.FIRESTORE_FIELD_TEMPLATE_ID) ?: templateId,
            orderIndex = longField(AppConstants.FIRESTORE_FIELD_ORDER_INDEX).toInt(),
            title = title,
            content = stringField(AppConstants.FIRESTORE_FIELD_CONTENT) ?: "",
            leader = stringField(AppConstants.FIRESTORE_FIELD_LEADER) ?: "",
            type = stringField(AppConstants.FIRESTORE_FIELD_TYPE) ?: "CUSTOM"
        )
    }

    private fun EventProgramItemDto.toMap(): Map<String, Any?> {
        return mapOf(
            AppConstants.FIRESTORE_FIELD_ID to id,
            AppConstants.FIRESTORE_FIELD_EVENT_ID to eventId,
            AppConstants.FIRESTORE_FIELD_ORDER_INDEX to orderIndex,
            AppConstants.FIRESTORE_FIELD_TITLE to title,
            AppConstants.FIRESTORE_FIELD_CONTENT to content,
            AppConstants.FIRESTORE_FIELD_LEADER to leader,
            AppConstants.FIRESTORE_FIELD_TYPE to type
        )
    }

    private fun WorshipTemplateItemDto.toMap(): Map<String, Any?> {
        return mapOf(
            AppConstants.FIRESTORE_FIELD_ID to id,
            AppConstants.FIRESTORE_FIELD_TEMPLATE_ID to templateId,
            AppConstants.FIRESTORE_FIELD_ORDER_INDEX to orderIndex,
            AppConstants.FIRESTORE_FIELD_TITLE to title,
            AppConstants.FIRESTORE_FIELD_CONTENT to content,
            AppConstants.FIRESTORE_FIELD_LEADER to leader,
            AppConstants.FIRESTORE_FIELD_TYPE to type
        )
    }

    private fun EventDto.withIdIfNeeded(prefix: String): EventDto {
        val resolvedId = id.ifBlank { "$prefix-${UUID.randomUUID()}" }
        return copy(
            id = resolvedId,
            programItems = programItems.mapIndexed { index, item ->
                item.copy(
                    id = item.id.ifBlank { "$resolvedId-program-$index" },
                    eventId = resolvedId,
                    orderIndex = index
                )
            }
        )
    }

    private fun MemberDto.withIdIfNeeded(prefix: String): MemberDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun FinanceReportDto.withIdIfNeeded(prefix: String): FinanceReportDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun PaymentObligationDto.withIdIfNeeded(prefix: String): PaymentObligationDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun WorshipTemplateDto.withIdIfNeeded(prefix: String): WorshipTemplateDto {
        if (id.isNotBlank()) return this
        val resolvedId = "$prefix-${UUID.randomUUID()}"
        return copy(
            id = resolvedId,
            items = items.mapIndexed { index, item ->
                item.copy(
                    id = item.id.ifBlank { "$resolvedId-item-$index" },
                    templateId = resolvedId,
                    orderIndex = index
                )
            }
        )
    }

    private data class FallbackAccount(
        val userId: String,
        val displayName: String,
        val role: String,
        val sectorId: String,
        val sectorName: String
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseDataSourceModule {

    @Binds
    @IntoMap
    @DataSourceBindingKey(DataSourceProvider.FIREBASE)
    abstract fun bindFirebaseDataSource(impl: FirebaseAppRemoteDataSource): AppRemoteDataSource
}
