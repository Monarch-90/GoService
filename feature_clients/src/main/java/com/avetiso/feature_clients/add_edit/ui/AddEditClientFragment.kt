package com.avetiso.feature_clients.add_edit.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientEvent
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientViewModel
import com.avetiso.feature_clients.databinding.FragmentAddEditClientBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditClientFragment : Fragment(R.layout.fragment_add_edit_client) {

    private var binding: FragmentAddEditClientBinding? = null
    private val viewModel: AddEditClientViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddEditClientBinding.bind(view)

        setupListeners()
        observeUi()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на состояние (для заполнения полей в режиме редактирования)
                launch {
                    viewModel.state.collect { state ->
                        state.client?.let { client ->
                            populateFields(client)
                        }
                        // Логика навигации отсюда удалена
                    }
                }

                // Подписка на события (Toast и навигация)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddEditClientEvent.ShowToast -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }
                            is AddEditClientEvent.NavigateBackWithResult -> {
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("client_updated", true)
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun populateFields(client: ClientEntity) {
        val currentBinding = binding ?: return
        currentBinding.toolbar.title = "Редактировать клиента"
        currentBinding.inputEditTextName.setText(client.name)
        currentBinding.inputEditTextPhone.setText(client.phoneNumber)
        currentBinding.inputEditTextInstagram.setText(client.instagram)
        currentBinding.inputEditTextSource.setText(client.source)
        currentBinding.inputEditTextDiscount.setText(if (client.discount > 0) client.discount.toString() else "")
        currentBinding.inputEditTextNote.setText(client.note)
    }


    private fun setupListeners() {
        val currentBinding = binding ?: return
        currentBinding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        currentBinding.inputEditTextName.addTextChangedListener {
            currentBinding.inputLayoutName.error = null
        }
        currentBinding.inputEditTextPhone.addTextChangedListener {
            currentBinding.inputLayoutPhone.error = null
        }

        currentBinding.btnSave.setOnClickListener {
            saveClient()
        }
    }

    private fun saveClient() {
        val currentBinding = binding ?: return
        val name = currentBinding.inputEditTextName.text.toString().trim()

        if (name.isBlank()) {
            currentBinding.inputLayoutName.error = "Имя не может быть пустым"
            return
        }

        val discountStr = currentBinding.inputEditTextDiscount.text.toString()

        val clientToSave = ClientEntity(
            id = viewModel.state.value.client?.id ?: 0L,
            name = name,
            phoneNumber = currentBinding.inputEditTextPhone.text.toString().trim(),
            instagram = currentBinding.inputEditTextInstagram.text.toString().trim(),
            source = currentBinding.inputEditTextSource.text.toString().trim(),
            discount = discountStr.toIntOrNull() ?: 0,
            note = currentBinding.inputEditTextNote.text.toString().trim()
        )

        viewModel.saveClient(clientToSave)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}