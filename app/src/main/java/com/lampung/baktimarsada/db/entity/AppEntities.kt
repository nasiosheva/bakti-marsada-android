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
