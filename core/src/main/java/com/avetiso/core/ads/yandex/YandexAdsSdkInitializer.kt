package com.avetiso.core.ads.yandex

import android.content.Context
import com.avetiso.core.ads.AdsNetworkSdkInitializer
import com.avetiso.core.ads.AdsProvider
import com.yandex.mobile.ads.common.YandexAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexAdsSdkInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) : AdsNetworkSdkInitializer {

    override val provider = AdsProvider.YANDEX

    private val initializationStarted = AtomicBoolean(false)
    private val readiness = CompletableDeferred<Boolean>()

    override fun initialize() {
        if (!initializationStarted.compareAndSet(false, true)) {
            return
        }

        try {
            YandexAds.initialize(context) {
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
