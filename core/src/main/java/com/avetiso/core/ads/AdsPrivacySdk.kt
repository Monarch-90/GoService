package com.avetiso.core.ads

interface AdsPrivacySdk {
    suspend fun applyConsent(consent: AdsDataConsent)
}
