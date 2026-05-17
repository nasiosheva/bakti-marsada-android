package com.lampung.baktimarsada.ui.util

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Long): String {
    val locale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()
    return NumberFormat.getCurrencyInstance(locale).format(amount)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
