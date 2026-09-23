package com.avetiso.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AppLaunchCounterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppLaunchCounterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppLaunchCounterRepository {

    private val appLaunchCountKey =
        intPreferencesKey(AppConstants.Preferences.KEY_APP_LAUNCH_COUNT)

    override suspend fun incrementAndGetLaunchCount(): Int {
        var launchCount = 0

        dataStore.edit { preferences ->
            launchCount = (preferences[appLaunchCountKey] ?: 0) + 1
            preferences[appLaunchCountKey] = launchCount
        }

        return launchCount
    }
}