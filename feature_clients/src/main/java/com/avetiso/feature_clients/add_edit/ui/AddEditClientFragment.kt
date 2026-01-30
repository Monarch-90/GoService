package com.avetiso.feature_clients.add_edit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientEvent
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientState
import com.avetiso.feature_clients.add_edit.mvi.AddEditClientViewModel
import com.avetiso.feature_clients.databinding.FragmentAddEditClientBinding
import com.avetiso.feature_clients.databinding.ItemCustomFieldBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditClientFragment : Fragment(R.layout.fragment_add_edit_client) {

    private var binding: FragmentAddEditClientBinding? = null
    private val viewModel: AddEditClientViewModel by viewModels()
    private val customFieldViews = mutableMapOf<String, ItemCustomFieldBinding>()

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
                // 1. Подписка на состояние (Рендер UI)
                launch {
                    viewModel.state.collect { state ->
                        render(state)
                    }
                }

                // 2. Подписка на события (Тосты, Навигация)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddEditClientEvent.ShowToast -> {
                                Toast.makeText(
                                    requireContext(),
                                    event.message.asString(
                                        requireContext()
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            is AddEditClientEvent.NavigateBackWithResult -> {
                                setFragmentResult(
                                    ClientsConstants.Requests.CLIENT_UPDATED,
                                    bundleOf(ClientsConstants.ResultKeys.IS_CLIENT_UPDATED to true)
                                )
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: AddEditClientState) {
        val currentBinding = binding ?: return

        currentBinding.toolbar.title = if (state.isEditing) {
            getString(R.string.Редактировать_клиента)
        } else {
            getString(R.string.Новый_клиент)
        }

        // Функция-помощник для обновления текста без "дёрганья" курсора
        fun updateTextIfChanged(editText: EditText, newText: String) {
            if (editText.text.toString() != newText) {
                editText.setText(newText)
                // Ставим курсор в конец
                editText.setSelection(newText.length)
            }
        }

        updateTextIfChanged(currentBinding.inputEditTextName, state.nameInput)
        updateTextIfChanged(currentBinding.inputEditTextPhone, state.phoneInput)
        updateTextIfChanged(currentBinding.inputEditTextInstagram, state.instagramInput)
        updateTextIfChanged(currentBinding.inputEditTextSource, state.sourceInput)
        updateTextIfChanged(currentBinding.inputEditTextDiscount, state.discountInput)
        updateTextIfChanged(currentBinding.inputEditTextNote, state.noteInput)

        renderCustomFields(state.customFieldsInput)
    }

    private fun renderCustomFields(customFields: Map<String, String>) {
        val currentBinding = binding ?: return
        val container = currentBinding.customFieldsContainer

        // 1. Удаляем Views, которых больше нет в State
        val fieldNamesToRemove = customFieldViews.keys.filter { !customFields.containsKey(it) }
        fieldNamesToRemove.forEach { name ->
            val viewBinding = customFieldViews[name]
            if (viewBinding != null) {
                container.removeView(viewBinding.root)
                customFieldViews.remove(name)
            }
        }

        // 2. Добавляем или обновляем Views
        customFields.forEach { (fieldName, fieldValue) ->
            if (customFieldViews.containsKey(fieldName)) {
                // Если поле уже есть, просто обновляем текст (если он отличается)
                val binding = customFieldViews[fieldName]!!
                if (binding.ietCustomField.text.toString() != fieldValue) {
                    binding.ietCustomField.setText(fieldValue)
                }
            } else {
                // Если поля нет, создаем новое
                val fieldBinding = ItemCustomFieldBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    container,
                    false
                )

                fieldBinding.ilCustomField.hint = fieldName
                fieldBinding.ietCustomField.setText(fieldValue)
                fieldBinding.ietCustomField.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

                // Слушатель удаления
                fieldBinding.btnRemoveField.setOnClickListener {
                    viewModel.removeCustomField(fieldName)
                }

                // Слушатель ввода текста
                fieldBinding.ietCustomField.doAfterTextChanged {
                    viewModel.onCustomFieldValueChanged(fieldName, it.toString())
                }

                container.addView(fieldBinding.root)
                customFieldViews[fieldName] = fieldBinding
            }
        }
    }

    private fun setupListeners() {
        val currentBinding = binding ?: return

        currentBinding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        currentBinding.inputEditTextName.doAfterTextChanged { viewModel.onNameChanged(it.toString()) }
        currentBinding.inputEditTextPhone.doAfterTextChanged { viewModel.onPhoneChanged(it.toString()) }
        currentBinding.inputEditTextSource.doAfterTextChanged { viewModel.onSourceChanged(it.toString()) }
        currentBinding.inputEditTextDiscount.doAfterTextChanged { viewModel.onDiscountChanged(it.toString()) }
        currentBinding.inputEditTextNote.doAfterTextChanged { viewModel.onNoteChanged(it.toString()) }

        currentBinding.inputEditTextInstagram.doAfterTextChanged { editable ->
            val text = editable.toString()
            if (text.startsWith(AppConstants.Format.INSTAGRAM_PREFIX)) {
                val newText = text.substring(1)
                currentBinding.inputEditTextInstagram.setText(newText)
                currentBinding.inputEditTextInstagram.setSelection(0)
            } else {
                viewModel.onInstagramChanged(text)
            }
        }

        currentBinding.btnSave.setOnClickListener {
            // Валидация UI перед отправкой в VM (для красивой ошибки на поле)
            if (currentBinding.inputEditTextName.text.isNullOrBlank()) {
                currentBinding.inputLayoutName.error = getString(R.string.Имя_не_может_быть_пустым)
            } else {
                currentBinding.inputLayoutName.error = null
                viewModel.onSaveClicked()
            }
        }

        currentBinding.btnAddField.setOnClickListener { showAddFieldDialog() }
    }

    // Обработка ввода из InputDialogFragment
    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            ClientsConstants.Requests.INPUT_FIELD,
            viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(AppConstants.Result.RESULT_TEXT) ?: return@setFragmentResultListener
            viewModel.addCustomField(text)
        }
    }

    private fun showAddFieldDialog() {
        val state = viewModel.state.value

        val usedNames = mutableListOf(
            getString(R.string.Имя_клиента),
            getString(R.string.Номер_телефона),
            getString(R.string.Инстаграм),
            getString(R.string.Источник_привлечения),
            getString(R.string.Личная_скидка_процент),
            getString(com.avetiso.core.R.string.Примечание),
        )

        // Добавляем уже созданные кастомные поля
        usedNames.addAll(state.customFieldsInput.keys)

        InputDialogFragment.newInstance(
            requestKey = ClientsConstants.Requests.INPUT_FIELD,
            title = getString(R.string.Новое_поле),
            hint = getString(R.string.Название_поля),
            forbiddenValues = usedNames,
        ).show(childFragmentManager, AppConstants.Result.INPUT_DIALOG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        customFieldViews.clear()
    }
}