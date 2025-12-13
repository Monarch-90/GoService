package com.avetiso.feature_clients.add_edit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientEvent
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientViewModel
import com.avetiso.feature_clients.databinding.FragmentAddEditClientBinding
import com.avetiso.feature_clients.databinding.ItemCustomFieldBinding
import com.google.android.material.textfield.TextInputEditText
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
        setupResultListeners()
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
                            else -> { /* Игнорируем события, предназначенные для ViewModel */
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

        val cleanInstagram = client.instagram.removePrefix("@")
        currentBinding.inputEditTextInstagram.setText(cleanInstagram)

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

        currentBinding.inputEditTextInstagram.addTextChangedListener { editable ->
            val text = editable.toString()
            if (text.startsWith("@")) {
                // Если пользователь ввел @ в начале, удаляем её моментально
                val newText = text.substring(1)
                currentBinding.inputEditTextInstagram.setText(newText)
                // Возвращаем курсор в начало (или на позицию 0, так как мы удалили символ)
                currentBinding.inputEditTextInstagram.setSelection(0)
            }
        }

        currentBinding.btnSave.setOnClickListener { saveClient() }
        currentBinding.btnAddField.setOnClickListener { showAddFieldDialog() }
    }

    // Обработка ввода из InputDialogFragment
    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(INPUT_FIELD_KEY, viewLifecycleOwner) { _, bundle ->
            val text = bundle.getString(InputDialogFragment.RESULT_TEXT) ?: return@setFragmentResultListener
            addCustomFieldView(text)
        }
    }

    private fun showAddFieldDialog() {
        val usedNames = mutableListOf(
            getString(com.avetiso.core.R.string.Имя_клиента),
            getString(com.avetiso.core.R.string.Номер_телефона),
            getString(com.avetiso.core.R.string.Инстаграм),
            getString(com.avetiso.core.R.string.Источник_привлечения),
            getString(com.avetiso.core.R.string.Личная_скидка),
            getString(com.avetiso.core.R.string.Примечание),
        )

        // Добавляем уже созданные кастомные поля
        usedNames.addAll(customFieldViews.keys)

        InputDialogFragment.newInstance(
            requestKey = INPUT_FIELD_KEY,
            title = getString(com.avetiso.core.R.string.Новое_поле),
            hint = getString(com.avetiso.core.R.string.Название_поля),
            forbiddenValues = usedNames,
        ).show(childFragmentManager, InputDialogFragment.TAG)
    }

    private fun addCustomFieldView(fieldName: String, fieldValue: String = "") {
        val currentBinding = binding ?: return

        // 1. "Надуваем" наш кастомный layout
        val fieldBinding = ItemCustomFieldBinding.inflate(
            LayoutInflater.from(requireContext()), // Используем LayoutInflater
            currentBinding.customFieldsContainer, // Указываем родителя
            false // Не прикрепляем сразу, добавим ниже
        )

        // 2. Настраиваем надутый layout
        fieldBinding.ilCustomField.hint = fieldName
        fieldBinding.ietCustomField.setText(fieldValue)
        fieldBinding.ietCustomField.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        // 3. Настраиваем кнопку удаления
        fieldBinding.btnRemoveField.setOnClickListener {
            // Удаляем View из контейнера
            currentBinding.customFieldsContainer.removeView(fieldBinding.root)
            // Удаляем поле из нашей Map
            customFieldViews.remove(fieldName)
        }

        // 4. Добавляем готовое View в контейнер
        currentBinding.customFieldsContainer.addView(fieldBinding.root)

        // 5. Сохраняем ссылку на EditText (ключ - fieldName, значение - EditText)
        customFieldViews[fieldName] = fieldBinding.ietCustomField
    }

    private fun saveClient() {
        val currentBinding = binding ?: return
        val name = currentBinding.inputEditTextName.text.toString().trim()

        if (name.isBlank()) {
            currentBinding.inputLayoutName.error = "Имя не может быть пустым"
            return
        }

        val discountStr = currentBinding.inputEditTextDiscount.text.toString()

        val rawInstagram = currentBinding.inputEditTextInstagram.text.toString().trim()
        val finalInstagram = if (rawInstagram.isNotEmpty()) "@$rawInstagram" else ""

        val customFieldsMap = customFieldViews.mapValues { entry ->
            entry.value.text.toString().trim()
        }

        val clientToSave = ClientEntity(
            id = viewModel.state.value.client?.id ?: 0L,
            name = name,
            phoneNumber = currentBinding.inputEditTextPhone.text.toString().trim(),
            instagram = finalInstagram,
            source = currentBinding.inputEditTextSource.text.toString().trim(),
            discount = discountStr.toIntOrNull() ?: 0,
            note = currentBinding.inputEditTextNote.text.toString().trim(),
            customFields = customFieldsMap
        )

        viewModel.saveClient(clientToSave)
    }

    companion object {
        private const val INPUT_FIELD_KEY = "input_field_request"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}