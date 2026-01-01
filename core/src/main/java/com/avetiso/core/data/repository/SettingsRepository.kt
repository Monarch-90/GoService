package com.avetiso.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.avetiso.core.model.AppCurrency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val defaultCurrencyKey = stringPreferencesKey("default_currency")

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
}