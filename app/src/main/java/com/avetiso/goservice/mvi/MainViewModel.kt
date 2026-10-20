package com.avetiso.goservice.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import com.avetiso.core.usecase.SaveAdsConsentUseCase
import com.avetiso.core.usecase.ShouldShowAppOpenAdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val shouldShowAppOpenAdUseCase: ShouldShowAppOpenAdUseCase,
    private val adsConsentRepository: AdsConsentRepository,
    private val saveAdsConsentUseCase: SaveAdsConsentUseCase
) : ViewModel() {

    private val _effects = Channel<MainSideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private var pendingDestinationId: Int?
        get() = savedStateHandle[AppConstants.Time.KEY_PENDING_DESTINATION]
        set(value) {
            savedStateHandle[AppConstants.Time.KEY_PENDING_DESTINATION] = value
        }

    private val actionMutex = Mutex()
    private var nextAdEffectRequestId = 0L
    private var currentAdsConsent: AdsDataConsent? = null
    private var appLaunchRequested = false
    private var adsScheduledForThisLaunch = false
    private var consentDialogRequestSent = false

    init {
        viewModelScope.launch {
            adsConsentRepository.consent.collect { choice ->
                actionMutex.withLock {
                    currentAdsConsent = choice
                    if (choice == AdsDataConsent.UNDECIDED && !consentDialogRequestSent) {
                        consentDialogRequestSent = true
                        _effects.send(MainSideEffect.ShowAdsConsentDialog)
                    } else if (choice != AdsDataConsent.UNDECIDED) {
                        consentDialogRequestSent = false
                    }
                    enqueueLaunchAdsIfReady()
                }
            }
        }
    }

    fun processIntent(intent: MainIntent) {
        viewModelScope.launch {
            actionMutex.withLock {
                when (intent) {
                    is MainIntent.OnSidebarItemClicked -> handleItemClick(intent.itemId)
                    is MainIntent.OnDrawerClosed -> handleDrawerClosed()
                    MainAppLaunchedIntent -> handleAppLaunched()
                    is MainAdEffectHandledIntent -> handleAdEffectHandled(intent.requestId)
                    is MainAdsConsentChosenIntent -> saveAdsConsentUseCase(
                        if (intent.consentGranted) AdsDataConsent.GRANTED else AdsDataConsent.DENIED
                    )
                }
            }
        }
    }

    private suspend fun handleAppLaunched() {
        appLaunchRequested = true
        enqueueLaunchAdsIfReady()
    }

    private suspend fun enqueueLaunchAdsIfReady() {
        if (!appLaunchRequested || adsScheduledForThisLaunch) return
        val choice = currentAdsConsent ?: return
        if (choice == AdsDataConsent.UNDECIDED) return

        adsScheduledForThisLaunch = true
        requestAdEffect(MainAdEffectType.LOAD_APP_OPEN)
        if (shouldShowAppOpenAdUseCase()) {
            requestAdEffect(MainAdEffectType.SHOW_APP_OPEN)
        }
    }

    private fun handleAdEffectHandled(requestId: Long) {
        _state.update { state ->
            state.copy(
                pendingAdEffects = state.pendingAdEffects.filterNot { effect ->
                    effect.requestId == requestId
                }
            )
        }
    }

    private fun requestAdEffect(type: MainAdEffectType) {
        val effect = createAdEffect(type)

        _state.update { state ->
            state.copy(
                pendingAdEffects = state.pendingAdEffects + effect
            )
        }
    }

    private fun createAdEffect(type: MainAdEffectType): MainAdEffect {
        nextAdEffectRequestId += 1

        return MainAdEffect(
            requestId = nextAdEffectRequestId,
            type = type
        )
    }

    private suspend fun handleItemClick(itemId: Int) {
        if (pendingDestinationId != null) {
            return
        }

        pendingDestinationId = itemId
        _effects.send(MainSideEffect.CloseSidebar)
    }

    private suspend fun handleDrawerClosed() {
        val destinationId = pendingDestinationId ?: return

        pendingDestinationId = null
        _effects.send(MainSideEffect.Navigate(destinationId))
    }

    override fun onCleared() {
        _effects.close()
        super.onCleared()
    }
}
