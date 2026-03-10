package com.avetiso.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.avetiso.core.AppConstants
import com.avetiso.core.model.AppCurrency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.Data.DATA_STORE_NAME)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val defaultCurrencyKey = stringPreferencesKey(AppConstants.Preferences.KEY_DEFAULT_CURRENCY)
    private val customCurrenciesKey = stringSetPreferencesKey(AppConstants.Preferences.KEY_CUSTOM_CURRENCIES)

    // Поток данных: всегда возвращает актуальную валюту. По умолчанию "GEL".
    val defaultCurrency: Flow<String> = context.dataStore.data
        .catch { exception ->
            // Если ошибка чтения диска, не крашимся, а возвращаем пустые настройки
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Берем имя из Enum
            preferences[defaultCurrencyKey] ?: AppCurrency.GEL.name
        }

    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[defaultCurrencyKey] = currency
        }
    }

    // Подписываемся на список кастомных валют
    val customCurrencies: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[customCurrenciesKey] ?: emptySet()
        }

    // Безопасное добавление новой валюты
    suspend fun addCustomCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[customCurrenciesKey] ?: emptySet()
            preferences[customCurrenciesKey] = currentSet + currency.trim().uppercase()
        }
    }

    // Безопасное удаление (на будущее, для экрана настроек)
    suspend fun removeCustomCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[customCurrenciesKey] ?: emptySet()
            preferences[customCurrenciesKey] = currentSet - currency.trim().uppercase()
        }
    }
}