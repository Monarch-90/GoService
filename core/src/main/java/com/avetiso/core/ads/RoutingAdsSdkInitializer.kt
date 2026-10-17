
package com.avetiso.core.ads

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingAdsSdkInitializer @Inject constructor(
    initializers: Set<@JvmSuppressWildcards AdsNetworkSdkInitializer>,
    private val adsProviderPolicy: AdsProviderPolicy
) : AdsSdkInitializer {

    private val initializersByProvider = initializers.associateBy { initializer ->
        initializer.provider
    }

    init {
        check(initializersByProvider.size == initializers.size) {
            "Only one AdsNetworkSdkInitializer can be registered for each AdsProvider"
        }
    }

    override fun initialize() {
        activeInitializer().initialize()
    }

    override suspend fun awaitReady(): Boolean {
        return activeInitializer().awaitReady()
    }

    private fun activeInitializer(): AdsNetworkSdkInitializer {
        return requireNotNull(
            initializersByProvider[adsProviderPolicy.activeProvider]
        ) {
            "No AdsNetworkSdkInitializer registered for ${adsProviderPolicy.activeProvider}"
        }
    }
}
