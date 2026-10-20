package com.avetiso.core.ads.yandex

import com.avetiso.core.ads.AdsDataConsent
import com.avetiso.core.ads.AdsPrivacySdk
import com.yandex.mobile.ads.common.YandexAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexAdsPrivacySdk @Inject constructor() : AdsPrivacySdk {
    override suspend fun applyConsent(consent: AdsDataConsent) {
        require(consent != AdsDataConsent.UNDECIDED)
        withContext(Dispatchers.Main.immediate) {
            YandexAds.setUserConsent(consent == AdsDataConsent.GRANTED)
        }
    }
}
