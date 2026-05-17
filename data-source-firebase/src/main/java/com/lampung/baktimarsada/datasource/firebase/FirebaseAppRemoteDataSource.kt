package com.lampung.baktimarsada.datasource.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.core.constants.DataSourceProvider
import com.lampung.baktimarsada.core.di.DataSourceBindingKey
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
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
    private val firestore: FirebaseFirestore
) : AppRemoteDataSource {

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
                    AppConstants.FIRESTORE_FIELD_SECTOR_NAME to resolved.sectorName
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
            sectorName = sectorName
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

    private fun EventDto.withIdIfNeeded(prefix: String): EventDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
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
