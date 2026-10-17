package com.avetiso.core.ads

import android.content.Context
import android.telephony.TelephonyManager
import com.avetiso.core.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAdsRegionResolver @Inject constructor(
    @ApplicationContext context: Context
) : AdsRegionResolver {

    private val applicationContext = context.applicationContext

    override fun resolveCountryIso(): String? {
        val telephonyManager = applicationContext.getSystemService(
            TelephonyManager::class.java
        )

        return resolveNetworkCountryIso(telephonyManager)
            ?: resolveSimCountryIso(telephonyManager)
            ?: resolveLocaleCountryIso()
    }

    private fun resolveNetworkCountryIso(
        telephonyManager: TelephonyManager?
    ): String? {
        val countryIso = runCatching {
            telephonyManager?.networkCountryIso
        }.getOrNull()

        return normalizeCountryIso(countryIso)
    }

    private fun resolveSimCountryIso(
        telephonyManager: TelephonyManager?
    ): String? {
        val countryIso = runCatching {
            telephonyManager?.simCountryIso
        }.getOrNull()

        return normalizeCountryIso(countryIso)
    }

    private fun resolveLocaleCountryIso(): String? {
        val locales = applicationContext.resources.configuration.locales
        val configurationCountryIso = if (locales.isEmpty) {
            null
        } else {
            locales[0].country
        }

        return normalizeCountryIso(configurationCountryIso)
            ?: normalizeCountryIso(Locale.getDefault().country)
    }

    private fun normalizeCountryIso(countryIso: String?): String? {
        return countryIso
            ?.trim()
            ?.takeIf { value ->
                value.length == AppConstants.AdConstants.RegionRouting.ISO_ALPHA_2_LENGTH
            }
            ?.uppercase(Locale.ROOT)
    }
}
