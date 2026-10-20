package com.avetiso.core.ads

import kotlinx.coroutines.flow.Flow

interface AdsConsentRepository {
    val consent: Flow<AdsDataConsent>
    suspend fun saveConsent(consent: AdsDataConsent)
}
