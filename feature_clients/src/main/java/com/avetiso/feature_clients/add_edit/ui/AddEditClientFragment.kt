package com.avetiso.feature_clients.add_edit.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditClientFragment : Fragment(R.layout.fragment_add_edit_client) {

    private var binding: FragmentAddEditClientBinding? = null
    private val viewModel: AddEditClientViewModel by viewModels()
    private val customFieldViews = mutableMapOf<String, TextInputEditText>()

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
                        if (state.isEditing && !state.isInitialDataSet) {
                            state.client?.let { client ->
                                populateFields(client)
                                viewModel.handleViewEvent(AddEditClientEvent.InitialDataSet)
                            }
                        }
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
                                setFragmentResult("client_updated_request", bundleOf("updated" to true))
                                findNavController().navigateUp()
                            }
                            // Она обработает InitialDataSet и любые другие события, которые нас здесь не интересуют.
                            else -> { /* Игнорируем события, предназначенные для ViewModel */ }
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

        // Сначала очищаем контейнер, чтобы не было дублей
        currentBinding.customFieldsContainer.removeAllViews()
        customFieldViews.clear()

        // Создаем View для каждого кастомного поля
        client.customFields?.forEach { (fieldName, fieldValue) ->
            addCustomFieldView(fieldName, fieldValue)
        }
    }


    private fun setupListeners() {
        val currentBinding = binding ?: return
        currentBinding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        currentBinding.inputEditTextName.addTextChangedListener { currentBinding.inputLayoutName.error = null }
        currentBinding.inputEditTextPhone.addTextChangedListener { currentBinding.inputLayoutPhone.error = null }
        currentBinding.btnSave.setOnClickListener { saveClient() }
        currentBinding.btnAddField.setOnClickListener { showAddFieldDialog() }
    }

    private fun showAddFieldDialog() {
        val editText = AppCompatEditText(requireContext()).apply {
            hint = "Название поля"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить новое поле")
            .setView(editText)
            .setPositiveButton("Добавить") { _, _ ->
                val fieldName = editText.text.toString().trim()
                if (fieldName.isNotEmpty() && !customFieldViews.containsKey(fieldName)) {
                    addCustomFieldView(fieldName)
                } else {
                    Toast.makeText(context, "Имя поля не может быть пустым или дублироваться", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addCustomFieldView(fieldName: String, fieldValue: String = "") {
        val currentBinding = binding ?: return

        val textInputLayout = TextInputLayout(
            requireContext(),
            null,
            com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
        ).apply {
            hint = fieldName
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
        }

        val textInputEditText = TextInputEditText(textInputLayout.context).apply {
            setText(fieldValue)
        }

        textInputLayout.addView(textInputEditText)
        currentBinding.customFieldsContainer.addView(textInputLayout)

        customFieldViews[fieldName] = textInputEditText
    }

    private fun saveClient() {
        val currentBinding = binding ?: return
        val name = currentBinding.inputEditTextName.text.toString().trim()

        if (name.isBlank()) {
            currentBinding.inputLayoutName.error = "Имя не может быть пустым"
            return
        }

        val discountStr = currentBinding.inputEditTextDiscount.text.toString()

        val customFieldsMap = customFieldViews.mapValues { entry ->
            entry.value.text.toString().trim()
        }

        val clientToSave = ClientEntity(
            id = viewModel.state.value.client?.id ?: 0L,
            name = name,
            phoneNumber = currentBinding.inputEditTextPhone.text.toString().trim(),
            instagram = currentBinding.inputEditTextInstagram.text.toString().trim(),
            source = currentBinding.inputEditTextSource.text.toString().trim(),
            discount = discountStr.toIntOrNull() ?: 0,
            note = currentBinding.inputEditTextNote.text.toString().trim(),
            customFields = customFieldsMap
        )

        viewModel.saveClient(clientToSave)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}