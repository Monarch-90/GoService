package com.avetiso.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAdsConsentRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AdsConsentRepository {

    private val consentKey = stringPreferencesKey(AppConstants.Preferences.KEY_ADS_DATA_CONSENT)

    override val consent: Flow<AdsDataConsent> = dataStore.data.map { preferences ->
        when (preferences[consentKey]) {
            AdsDataConsent.GRANTED.name -> AdsDataConsent.GRANTED
            AdsDataConsent.DENIED.name -> AdsDataConsent.DENIED
            else -> AdsDataConsent.UNDECIDED
        }
    }

    override suspend fun saveConsent(consent: AdsDataConsent) {
        require(consent != AdsDataConsent.UNDECIDED)
        dataStore.edit { preferences ->
            preferences[consentKey] = consent.name
        }
    }
}
