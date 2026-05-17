package com.lampung.baktimarsada.data.mapper

import com.lampung.baktimarsada.db.entity.EventEntity
import com.lampung.baktimarsada.db.entity.FinanceReportEntity
import com.lampung.baktimarsada.db.entity.MemberEntity
import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto

fun SessionResponseDto.toDomain(): SessionState {
    return SessionState(
        authToken = authToken,
        userId = userId,
        displayName = displayName,
        role = UserRole.valueOf(role),
        sectorContext = SectorContext(
            sectorId = sectorId,
            sectorName = sectorName
        )
    )
}

fun EventDto.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        title = title,
        description = description,
        scheduledAt = scheduledAt,
        location = location,
        sectorId = sectorId,
        sectorName = sectorName
    )
}

fun EventEntity.toDomain(): EventDetail {
    return EventDetail(
        id = id,
        title = title,
        description = description,
        scheduledAt = scheduledAt,
        location = location,
        sectorId = sectorId,
        sectorName = sectorName
    )
}

fun EventDetail.toDto(): EventDto {
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

fun MemberDto.toEntity(): MemberEntity {
    return MemberEntity(
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

fun MemberEntity.toDomain(): MemberDetail {
    return MemberDetail(
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

fun MemberDetail.toDto(): MemberDto {
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

fun FinanceReportDto.toEntity(): FinanceReportEntity {
    return FinanceReportEntity(
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

fun FinanceReportEntity.toDomain(): FinanceReportDetail {
    return FinanceReportDetail(
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

fun FinanceReportDetail.toDto(): FinanceReportDto {
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

fun PaymentObligationDto.toEntity(): PaymentObligationEntity {
    return PaymentObligationEntity(
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

fun PaymentObligationEntity.toDomain(): PaymentObligationDetail {
    return PaymentObligationDetail(
        id = id,
        memberId = memberId,
        memberName = memberName,
        title = title,
        description = description,
        amount = amount,
        dueDate = dueDate,
        status = PaymentStatus.valueOf(status),
        sectorId = sectorId,
        sectorName = sectorName
    )
}

fun PaymentObligationDetail.toDto(): PaymentObligationDto {
    return PaymentObligationDto(
        id = id,
        memberId = memberId,
        memberName = memberName,
        title = title,
        description = description,
        amount = amount,
        dueDate = dueDate,
        status = status.name,
        sectorId = sectorId,
        sectorName = sectorName
    )
}
