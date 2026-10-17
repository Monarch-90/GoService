
package com.avetiso.core.ads

import android.view.ViewGroup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingBannerAdController @Inject constructor(
    providers: Set<@JvmSuppressWildcards BannerAdProvider>,
    private val adsProviderPolicy: AdsProviderPolicy
) : BannerAdController {

    private val providersByType = providers.associateBy { provider ->
        provider.provider
    }

    init {
        check(providersByType.size == providers.size) {
            "Only one BannerAdProvider can be registered for each AdsProvider"
        }
    }

    override suspend fun showBanner(container: ViewGroup) {
        activeProvider().showBanner(container)
    }

    override fun destroyBanner(container: ViewGroup) {
        activeProvider().destroyBanner(container)
    }

    private fun activeProvider(): BannerAdProvider {
        return requireNotNull(
            providersByType[adsProviderPolicy.activeProvider]
        ) {
            "No BannerAdProvider registered for ${adsProviderPolicy.activeProvider}"
        }
    }
}
