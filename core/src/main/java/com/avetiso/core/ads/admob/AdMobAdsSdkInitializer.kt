
package com.avetiso.core.ads.admob

import android.content.Context
import com.avetiso.core.ads.AdsNetworkSdkInitializer
import com.avetiso.core.ads.AdsProvider
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobAdsSdkInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) : AdsNetworkSdkInitializer {

    override val provider = AdsProvider.ADMOB

    private val initializationStarted = AtomicBoolean(false)
    private val readiness = CompletableDeferred<Boolean>()

    override fun initialize() {
        if (!initializationStarted.compareAndSet(false, true)) {
            return
        }

        try {
            MobileAds.initialize(context) {
                readiness.complete(true)
            }
        } catch (exception: Exception) {
            readiness.complete(false)
        }
    }

    override suspend fun awaitReady(): Boolean {
        initialize()
        return readiness.await()
    }
}
