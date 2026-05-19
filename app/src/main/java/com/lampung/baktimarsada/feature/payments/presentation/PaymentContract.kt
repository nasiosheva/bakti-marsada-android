package com.lampung.baktimarsada.feature.payments.presentation

import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail

data class PaymentUiState(
    val items: List<PaymentObligationDetail> = emptyList(),
    val members: List<MemberDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface PaymentEvent {
    data object Refresh : PaymentEvent
    data class Save(val item: PaymentObligationDetail) : PaymentEvent
    data class Delete(val id: String) : PaymentEvent
}

// created by Mories Deo Hutapea, S.E.,S.Kom
