package com.avetiso.feature_sidebar.contact_developers.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.contact_developers.domain.SendFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDevelopersViewModel @Inject constructor(
    private val sendFeedbackUseCase: SendFeedbackUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ContactDevelopersState())
    val state = _state.asStateFlow()

    private val _events = Channel<ContactDevelopersEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentEmail = ""
    private var currentMessage = ""

    fun processIntent(intent: ContactDevelopersIntent) {
        when (intent) {
            is EmailChanged -> {
                currentEmail = intent.email
                _state.update { it.copy(emailErrorRes = null) }
            }
            is MessageChanged -> {
                currentMessage = intent.message
                _state.update { it.copy(messageErrorRes = null) }
            }
            is SendClicked -> validateAndSend()
        }
    }

    private fun validateAndSend() {
        var isValid = true
        var emailError: Int? = null
        var messageError: Int? = null

        if (currentEmail.isBlank()) {
            emailError = R.string.sidebar_contact_devs_error_empty_field
            isValid = false
        } else if (!currentEmail.matches(Regex(SidebarConstants.Validation.EMAIL_REGEX))) {
            emailError = R.string.sidebar_contact_devs_error_invalid_email
            isValid = false
        }

        if (currentMessage.isBlank()) {
            messageError = R.string.sidebar_contact_devs_error_empty_field
            isValid = false
        }

        _state.update {
            it.copy(emailErrorRes = emailError, messageErrorRes = messageError)
        }

        if (isValid) {
            sendFeedback()
        }
    }

    private fun sendFeedback() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = sendFeedbackUseCase(currentEmail, currentMessage)
            _state.update { it.copy(isLoading = false) }

            result.onSuccess {
                _events.send(ShowToast(R.string.sidebar_contact_devs_success_toast))
                _events.send(CloseScreen)
            }.onFailure {
                _events.send(ShowToast(R.string.sidebar_contact_devs_error_toast))
            }
        }
    }
}