package com.avetiso.feature_sidebar.about.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.about.domain.usecase.GetAboutTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val getAboutTextUseCase: GetAboutTextUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AboutState>(AboutState.Idle)
    val state: StateFlow<AboutState> = _state.asStateFlow()

    private val _event = Channel<AboutEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        loadText()
    }

    fun processIntent(intent: AboutIntent) {
        when (intent) {
            is AboutIntent.LoadAboutText -> loadText()
            is AboutIntent.OnBackClicked -> handleBackClick()
            is AboutIntent.OnPrivacyPolicyClicked -> handlePrivacyPolicyClick()
        }
    }

    private fun handlePrivacyPolicyClick() {
        viewModelScope.launch {
            _event.send(AboutEvent.OpenWebPage(SidebarConstants.About.PRIVACY_POLICY_URL))
        }
    }

    private fun loadText() {
        if (_state.value is AboutState.Loading || _state.value is AboutState.Success) {
            return
        }

        _state.value = AboutState.Loading

        viewModelScope.launch {
            getAboutTextUseCase().fold(
                onSuccess = { model ->
                    _state.value = AboutState.Success(text = model.content)
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: SidebarConstants.About.UNKNOWN_ERROR

                    _state.value = AboutState.Error(errorMessage)
                    _event.send(AboutEvent.ShowToast(errorMessage))
                }
            )
        }
    }

    private fun handleBackClick() {
        viewModelScope.launch {
            _event.send(AboutEvent.NavigateBack)
        }
    }
}