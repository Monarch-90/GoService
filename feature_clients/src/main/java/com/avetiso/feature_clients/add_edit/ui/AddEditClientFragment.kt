package com.avetiso.feature_clients.add_edit.ui

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
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
import com.avetiso.feature_clients.databinding.DialogAddFieldBinding
import com.avetiso.feature_clients.databinding.FragmentAddEditClientBinding
import com.avetiso.feature_clients.databinding.ItemCustomFieldBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private fun showAddFieldDialog() {
        // 1. "Надуваем" кастомный макет с помощью ViewBinding
        val dialogBinding = DialogAddFieldBinding.inflate(LayoutInflater.from(requireContext()))

        // 2. Создаем диалог, передавая ему ViewBinding.root
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 3. Устанавливаем слушатели на НАШИ кнопки из макета
        dialogBinding.btnNegative.setOnClickListener {
            dialog.dismiss() // Просто закрываем
        }

        dialogBinding.btnPositive.setOnClickListener {
            val fieldName = dialogBinding.ietFieldName.text.toString().trim()

            // Проверка на пустоту и дублирование
            if (fieldName.isEmpty()) {
                dialogBinding.ilFieldName.error = "Название не может быть пустым"
                return@setOnClickListener // Остаемся в диалоге
            }
            if (customFieldViews.containsKey(fieldName)) {
                dialogBinding.ilFieldName.error = "Такое поле уже существует"
                return@setOnClickListener // Остаемся в диалоге
            }

            // Если все ок:
            addCustomFieldView(fieldName) // Добавляем View
            dialog.dismiss() // Закрываем диалог
        }

        // Убираем ошибку при начале ввода
        dialogBinding.ietFieldName.addTextChangedListener {
            dialogBinding.ilFieldName.error = null
        }

        // 4. Показываем диалог
        dialog.show()

        // 5. Применяем кастомный фон (как в твоем RecyclerViewActions)
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}