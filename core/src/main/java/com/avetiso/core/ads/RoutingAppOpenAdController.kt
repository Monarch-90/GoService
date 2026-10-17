
package com.avetiso.core.ads

import android.app.Activity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingAppOpenAdController @Inject constructor(
    providers: Set<@JvmSuppressWildcards AppOpenAdProvider>,
    private val adsProviderPolicy: AdsProviderPolicy
) : AppOpenAdController {

    private val providersByType = providers.associateBy { provider ->
        provider.provider
    }

    init {
        check(providersByType.size == providers.size) {
            "Only one AppOpenAdProvider can be registered for each AdsProvider"
        }
    }

    override suspend fun loadAppOpenAd() {
        activeProvider().loadAppOpenAd()
    }

    override suspend fun showAppOpenAd(
        activity: Activity
    ): AppOpenAdShowResult {
        return activeProvider().showAppOpenAd(activity)
    }

    private fun activeProvider(): AppOpenAdProvider {
        return requireNotNull(
            providersByType[adsProviderPolicy.activeProvider]
        ) {
            "No AppOpenAdProvider registered for ${adsProviderPolicy.activeProvider}"
        }
    }
}
