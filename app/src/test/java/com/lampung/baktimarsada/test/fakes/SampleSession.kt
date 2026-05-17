package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole

fun sampleSession(role: UserRole = UserRole.ADMIN): SessionState {
    return SessionState(
        authToken = "token-1",
        userId = "user-1",
        displayName = "Tester",
        role = role,
        sectorContext = SectorContext(
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP"
        )
    )
}
