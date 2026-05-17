package com.lampung.baktimarsada.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: String,
    val location: String,
    @ColumnInfo(name = "sector_id")
    val sectorId: String,
    @ColumnInfo(name = "sector_name")
    val sectorName: String
)

@Entity(tableName = "event_program_items")
data class EventProgramItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "event_id")
    val eventId: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    val title: String,
    val content: String,
    val leader: String,
    val type: String,
    @ColumnInfo(name = "scripture_reference", defaultValue = "")
    val scriptureReference: String = "",
    @ColumnInfo(name = "scripture_text", defaultValue = "")
    val scriptureText: String = "",
    @ColumnInfo(defaultValue = "")
    val note: String = ""
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "family_group")
    val familyGroup: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    val address: String,
    @ColumnInfo(name = "role_in_sector")
    val roleInSector: String,
    @ColumnInfo(name = "sector_id")
    val sectorId: String,
    @ColumnInfo(name = "sector_name")
    val sectorName: String
)

@Entity(tableName = "finance_reports")
data class FinanceReportEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "period_label")
    val periodLabel: String,
    val amount: Long,
    @ColumnInfo(name = "is_visible_to_jemaat")
    val isVisibleToJemaat: Boolean,
    @ColumnInfo(name = "sector_id")
    val sectorId: String,
    @ColumnInfo(name = "sector_name")
    val sectorName: String
)

@Entity(tableName = "payment_obligations")
data class PaymentObligationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "member_id")
    val memberId: String,
    @ColumnInfo(name = "member_name")
    val memberName: String,
    val title: String,
    val description: String,
    val amount: Long,
    @ColumnInfo(name = "due_date")
    val dueDate: String,
    val status: String,
    @ColumnInfo(name = "sector_id")
    val sectorId: String,
    @ColumnInfo(name = "sector_name")
    val sectorName: String
)

@Entity(tableName = "worship_templates")
data class WorshipTemplateEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "tenant_id")
    val tenantId: String,
    @ColumnInfo(name = "sector_id")
    val sectorId: String?,
    val title: String,
    val description: String
)

@Entity(tableName = "worship_template_items")
data class WorshipTemplateItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "template_id")
    val templateId: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    val title: String,
    val content: String,
    val leader: String,
    val type: String,
    @ColumnInfo(name = "scripture_reference", defaultValue = "")
    val scriptureReference: String = "",
    @ColumnInfo(name = "scripture_text", defaultValue = "")
    val scriptureText: String = "",
    @ColumnInfo(defaultValue = "")
    val note: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
