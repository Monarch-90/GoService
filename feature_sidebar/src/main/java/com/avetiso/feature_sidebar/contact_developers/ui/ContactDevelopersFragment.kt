package com.avetiso.feature_sidebar.contact_developers.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.contact_developers.mvi.CloseScreen
import com.avetiso.feature_sidebar.contact_developers.mvi.ContactDevelopersEvent
import com.avetiso.feature_sidebar.contact_developers.mvi.ContactDevelopersIntent
import com.avetiso.feature_sidebar.contact_developers.mvi.ContactDevelopersState
import com.avetiso.feature_sidebar.contact_developers.mvi.ContactDevelopersViewModel
import com.avetiso.feature_sidebar.contact_developers.mvi.EmailChanged
import com.avetiso.feature_sidebar.contact_developers.mvi.MessageChanged
import com.avetiso.feature_sidebar.contact_developers.mvi.SendClicked
import com.avetiso.feature_sidebar.contact_developers.mvi.ShowToast
import com.avetiso.feature_sidebar.databinding.FragmentContactDevelopersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactDevelopersFragment : Fragment(R.layout.fragment_contact_developers) {

    private val viewModel: ContactDevelopersViewModel by viewModels()

    private var _binding: FragmentContactDevelopersBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactDevelopersBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() = with(binding) {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        ietEmail.doAfterTextChanged { text ->
            viewModel.processIntent(EmailChanged(text.toString()))
        }

        ietMessage.doAfterTextChanged { text ->
            viewModel.processIntent(MessageChanged(text.toString()))
        }

        btnSend.setOnClickListener {
            viewModel.processIntent(SendClicked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Наблюдаем за состоянием (collect)
                launch {
                    viewModel.state.collect { state ->
                        renderState(state)
                    }
                }

                // Наблюдаем за разовыми событиями СТРОГО через collect (а не collectLatest)
                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun renderState(state: ContactDevelopersState) = with(binding) {
        progressBar.isVisible = state.isLoading
        btnSend.isEnabled = !state.isLoading
        ietEmail.isEnabled = !state.isLoading
        ietMessage.isEnabled = !state.isLoading

        ilEmail.error = state.emailErrorRes?.let { getString(it) }
        ilMessage.error = state.messageErrorRes?.let { getString(it) }
    }

    private fun handleEvent(event: ContactDevelopersEvent) {
        when (event) {
            is ShowToast -> {
                Toast.makeText(requireContext(), event.messageResId, Toast.LENGTH_LONG).show()
            }
            is CloseScreen -> {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}