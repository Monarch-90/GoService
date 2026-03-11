package com.avetiso.core.mapper

import android.content.Context
import com.avetiso.core.AppConstants
import com.avetiso.core.R
import com.avetiso.core.models.CurrencyTotal
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppointmentPriceMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun mapToString(totals: List<CurrencyTotal>): String {
        return totals.joinToString("\n") { total ->
            val prefix = if (total.isPriceFrom) {
                "${context.getString(R.string.от_)} "
            } else ""

            val formattedPrice = AppConstants.Format.PRICE_2_DECIMALS.format(total.amount)
            "$prefix$formattedPrice ${total.currency}"
        }
    }

    fun getDuplicateErrorString(): String {
        return context.getString(R.string.Такая_запись_уже_существует_на_эту_дату)
    }
}