package com.avetiso.core.ads.yandex

import android.content.Context
import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import com.avetiso.core.ads.AdsNetworkSdkInitializer
import com.avetiso.core.ads.AdsPrivacySdk
import com.avetiso.core.ads.AdsProvider
import com.yandex.mobile.ads.common.YandexAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexAdsSdkInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentRepository: AdsConsentRepository,
    private val privacySdk: AdsPrivacySdk
) : AdsNetworkSdkInitializer {

    override val provider = AdsProvider.YANDEX
    private val initializationStarted = AtomicBoolean(false)
    private val readiness = CompletableDeferred<Boolean>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun initialize() {
        if (!initializationStarted.compareAndSet(false, true)) return

        scope.launch {
            try {
                val consent = consentRepository.consent.first { it != AdsDataConsent.UNDECIDED }
                privacySdk.applyConsent(consent)
                withContext(Dispatchers.Main.immediate) {
                    YandexAds.initialize(context) {
                        readiness.complete(true)
                    }
                }
            } catch (exception: CancellationException) {
                readiness.complete(false)
                throw exception
            } catch (exception: Exception) {
                readiness.complete(false)
            }
        }
    }

    override suspend fun awaitReady(): Boolean {
        initialize()
        return readiness.await()
    }
}
