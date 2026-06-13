package com.avetiso.core.usecase

import android.content.Context
import com.avetiso.core.R
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.core.models.AppCurrency
import com.avetiso.core.models.CurrencyListItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrencyListUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {
    /**
     * Реактивно собирает список для Spinner:
     * [+ Добавить] -> [Базовые валюты] -> [Кастомные валюты] -> [- Удалить]
     */
    operator fun invoke(): Flow<List<CurrencyListItem>> {
        return settingsRepository.customCurrencies.map { customSet ->
            val list = mutableListOf<CurrencyListItem>()

            // 1. Кнопка добавления всегда сверху
            list.add(CurrencyListItem.ActionAdd(context.getString(R.string.add_custom_currency)))

            // 2. Базовые валюты
            AppCurrency.getCodesList().forEach { code ->
                list.add(CurrencyListItem.Currency(code))
            }

            // 3. Кастомные валюты пользователя (сортируем для красоты)
            if (customSet.isNotEmpty()) {
                customSet.sorted().forEach { code ->
                    list.add(CurrencyListItem.Currency(code))
                }

                // 4. Кнопка удаления появляется ТОЛЬКО если есть кастомные валюты
                list.add(CurrencyListItem.ActionDelete(context.getString(R.string.delete_custom_currency)))
            }

            list
        }
    }
}