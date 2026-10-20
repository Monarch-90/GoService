package com.avetiso.core.usecase

import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import com.avetiso.core.ads.AdsPrivacySdk
import javax.inject.Inject

class SaveAdsConsentUseCase @Inject constructor(
    private val repository: AdsConsentRepository,
    private val privacySdk: AdsPrivacySdk
) {
    suspend operator fun invoke(consent: AdsDataConsent) {
        require(consent != AdsDataConsent.UNDECIDED)
        // Apply before publishing the changed choice to ad loaders.
        privacySdk.applyConsent(consent)
        repository.saveConsent(consent)
    }
}
