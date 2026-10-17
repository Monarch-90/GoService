package com.avetiso.core.ads

import com.avetiso.core.AppConstants
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAdsProviderPolicy @Inject constructor(
    adsRegionResolver: AdsRegionResolver
) : AdsProviderPolicy {

    override val activeProvider: AdsProvider = resolveProvider(
        countryIso = adsRegionResolver.resolveCountryIso()
    )

    private fun resolveProvider(countryIso: String?): AdsProvider {
        if (!AppConstants.AdConstants.AdMob.IS_ENABLED) {
            return AdsProvider.YANDEX
        }

        val normalizedCountryIso = countryIso
            ?.trim()
            ?.uppercase(Locale.ROOT)

        return if (
            normalizedCountryIso in AppConstants.AdConstants.RegionRouting.YANDEX_COUNTRY_ISO_CODES
        ) {
            AdsProvider.YANDEX
        } else {
            AdsProvider.ADMOB
        }
    }
}
