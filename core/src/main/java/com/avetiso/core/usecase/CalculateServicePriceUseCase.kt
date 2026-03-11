package com.avetiso.core.usecase

import com.avetiso.core.models.CurrencyTotal
import com.avetiso.core.models.ServiceSnapshot
import javax.inject.Inject

class CalculateServicePriceUseCase @Inject constructor() {

    operator fun invoke(services: List<ServiceSnapshot>, discountPercent: Int): List<CurrencyTotal> {
        return services
            .groupBy { it.currency }
            .map { (currency, servicesInCurrency) ->
                val subTotal = servicesInCurrency.sumOf { it.price }
                val isPriceFrom = servicesInCurrency.any { it.isPriceFrom }

                val finalAmount = if (discountPercent > 0) {
                    subTotal * (1.0 - discountPercent / 100.0)
                } else {
                    subTotal
                }

                CurrencyTotal(
                    currency = currency ?: "",
                    amount = finalAmount,
                    isPriceFrom = isPriceFrom
                )
            }
    }
}