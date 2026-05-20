package com.lampung.baktimarsada.feature.roulette.data

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.model.RouletteMemberUi
import com.lampung.baktimarsada.feature.roulette.model.RouletteSessionUi
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.MemberRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class DefaultRouletteImportDataSource @Inject constructor(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    private val loginUseCase: LoginUseCase,
    private val stringProvider: StringProvider
) : RouletteImportDataSource {

    override fun observeSession(): Flow<RouletteSessionUi?> {
        return authRepository.observeSession().map { session ->
            session?.toRouletteSessionUi()
        }
    }

    override fun observeMembers(): Flow<List<RouletteMemberUi>> {
        return memberRepository.observeMembers().map { members ->
            members.map { member -> member.toRouletteMemberUi() }
        }
    }

    override suspend fun login(identifier: String, password: String): AppResult<Unit> {
        return when (val result = loginUseCase(identifier, password)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(result.message)
        }
    }

    override suspend fun refreshMembers(): AppResult<Unit> {
        val session = authRepository.observeSession().firstOrNull()
            ?: return AppResult.Error(stringProvider.get(R.string.roulette_import_session_not_found))
        return when (val result = memberRepository.refresh(session.sectorContext)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(result.message)
        }
    }

    private fun SessionState.toRouletteSessionUi(): RouletteSessionUi {
        return RouletteSessionUi(
            userId = userId,
            displayName = displayName,
            sectorName = sectorContext.sectorName
        )
    }

    private fun MemberDetail.toRouletteMemberUi(): RouletteMemberUi {
        return RouletteMemberUi(
            id = id,
            fullName = fullName,
            familyGroup = familyGroup,
            roleInSector = roleInSector
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
