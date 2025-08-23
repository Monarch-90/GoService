package com.avetiso.feature_clients.add_edit.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.R
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
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Если режим редактирования, заполняем поля
                    state.client?.let { client ->
                        populateFields(client)
                        // Сбрасываем client в state, чтобы поля не перезаполнялись при повороте экрана
                        // viewModel.handleEvent(AddEditClientEvent.ClearClientData)
                    }

                    // Если пришел флаг navigateBack, возвращаемся назад
                    if (state.navigateBack) {
                        // Устанавливаем результат для предыдущего экрана
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("client_updated", true)
                        findNavController().navigateUp()
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

        currentBinding.fabSaveClient.setOnClickListener {
            saveClient()
        }
    }

    private fun saveClient() {
        val currentBinding = binding ?: return
        val name = currentBinding.inputEditTextName.text.toString().trim()
        val phone = currentBinding.inputEditTextPhone.text.toString().trim()

        if (name.isBlank()) {
            currentBinding.inputLayoutName.error = "Имя не может быть пустым"
            return
        }
        if (phone.isBlank()) {
            currentBinding.inputLayoutPhone.error = "Номер телефона не может быть пустым"
            return
        }

        val discountStr = currentBinding.inputEditTextDiscount.text.toString()

        val clientToSave = ClientEntity(
            id = viewModel.state.value.client?.id ?: 0L,
            name = name,
            phoneNumber = phone,
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