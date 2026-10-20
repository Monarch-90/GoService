package com.avetiso.core.usecase

import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import com.avetiso.core.ads.AdsPrivacySdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveAdsConsentUseCaseTest {

    @Test
    fun decliningAppliesSdkPolicyBeforePersistingChoice() = runBlocking {
        val operations = mutableListOf<String>()
        val repository = TestAdsConsentRepository(operations)
        val sdk = TestAdsPrivacySdk(operations)

        SaveAdsConsentUseCase(repository, sdk)(AdsDataConsent.DENIED)

        assertEquals(listOf("sdk:DENIED", "store:DENIED"), operations)
        assertEquals(AdsDataConsent.DENIED, repository.state.value)
    }

    @Test
    fun grantingAppliesSdkPolicyBeforePersistingChoice() = runBlocking {
        val operations = mutableListOf<String>()
        val repository = TestAdsConsentRepository(operations)
        val sdk = TestAdsPrivacySdk(operations)

        SaveAdsConsentUseCase(repository, sdk)(AdsDataConsent.GRANTED)

        assertEquals(listOf("sdk:GRANTED", "store:GRANTED"), operations)
        assertEquals(AdsDataConsent.GRANTED, repository.state.value)
    }
}

private class TestAdsConsentRepository(
    private val operations: MutableList<String>
) : AdsConsentRepository {
    val state = MutableStateFlow(AdsDataConsent.UNDECIDED)
    override val consent: Flow<AdsDataConsent> = state

    override suspend fun saveConsent(consent: AdsDataConsent) {
        operations.add("store:${consent.name}")
        state.value = consent
    }
}

private class TestAdsPrivacySdk(
    private val operations: MutableList<String>
) : AdsPrivacySdk {
    override suspend fun applyConsent(consent: AdsDataConsent) {
        operations.add("sdk:${consent.name}")
    }
}
