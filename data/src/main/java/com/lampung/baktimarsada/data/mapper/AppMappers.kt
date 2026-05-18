package com.lampung.baktimarsada.data.mapper

import com.lampung.baktimarsada.db.entity.EventEntity
import com.lampung.baktimarsada.db.entity.EventProgramItemEntity
import com.lampung.baktimarsada.db.entity.FinanceReportEntity
import com.lampung.baktimarsada.db.entity.MemberEntity
import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import com.lampung.baktimarsada.db.entity.WorshipTemplateEntity
import com.lampung.baktimarsada.db.entity.WorshipTemplateItemEntity
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.EventProgramItemDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateItemDto

fun SessionResponseDto.toDomain(): SessionState {
    return SessionState(
        authToken = authToken,
        userId = userId,
        displayName = displayName,
        role = UserRole.valueOf(role),
        tenantContext = TenantContext(
            tenantId = tenantId,
            tenantName = tenantName,
            subTenantId = subTenantId,
            subTenantName = subTenantName
        ),
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

fun EventProgramItemDto.toEntity(eventIdOverride: String = eventId): EventProgramItemEntity {
    return EventProgramItemEntity(
        id = id,
        eventId = eventIdOverride,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type,
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

fun EventEntity.toDomain(programItems: List<EventProgramItem> = emptyList()): EventDetail {
    return EventDetail(
        id = id,
        title = title,
        description = description,
        scheduledAt = scheduledAt,
        location = location,
        sectorId = sectorId,
        sectorName = sectorName,
        programItems = programItems.sortedBy { it.orderIndex }
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
        sectorName = sectorName,
        programItems = programItems.sortedBy { it.orderIndex }.map { it.toDto(id) }
    )
}

fun EventProgramItemEntity.toDomain(): EventProgramItem {
    return EventProgramItem(
        id = id,
        eventId = eventId,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type.toProgramItemType(),
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

fun EventProgramItem.toDto(eventIdOverride: String = eventId): EventProgramItemDto {
    return EventProgramItemDto(
        id = id,
        eventId = eventIdOverride,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type.name,
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
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

fun WorshipTemplateDto.toEntity(): WorshipTemplateEntity {
    return WorshipTemplateEntity(
        id = id,
        tenantId = tenantId,
        sectorId = sectorId,
        title = title,
        description = description
    )
}

fun WorshipTemplateItemDto.toEntity(templateIdOverride: String = templateId): WorshipTemplateItemEntity {
    return WorshipTemplateItemEntity(
        id = id,
        templateId = templateIdOverride,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type,
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

fun WorshipTemplateEntity.toDomain(items: List<WorshipTemplateItem> = emptyList()): WorshipTemplate {
    return WorshipTemplate(
        id = id,
        tenantId = tenantId,
        sectorId = sectorId,
        title = title,
        description = description,
        items = items.sortedBy { it.orderIndex }
    )
}

fun WorshipTemplateItemEntity.toDomain(): WorshipTemplateItem {
    return WorshipTemplateItem(
        id = id,
        templateId = templateId,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type.toProgramItemType(),
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

fun WorshipTemplate.toDto(): WorshipTemplateDto {
    return WorshipTemplateDto(
        id = id,
        tenantId = tenantId,
        sectorId = sectorId,
        title = title,
        description = description,
        items = items.sortedBy { it.orderIndex }.map { it.toDto(id) }
    )
}

fun WorshipTemplateItem.toDto(templateIdOverride: String = templateId): WorshipTemplateItemDto {
    return WorshipTemplateItemDto(
        id = id,
        templateId = templateIdOverride,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type.name,
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

private fun String.toProgramItemType(): ProgramItemType {
    return runCatching { ProgramItemType.valueOf(uppercase()) }.getOrDefault(ProgramItemType.CUSTOM)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
