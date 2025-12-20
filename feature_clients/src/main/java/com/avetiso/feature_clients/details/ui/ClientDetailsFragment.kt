package com.avetiso.feature_clients.details.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.databinding.FragmentClientDetailsBinding
import com.avetiso.feature_clients.databinding.ItemClientDetailFieldBinding
import com.avetiso.feature_clients.details.mvi.ClientDetailsEvent
import com.avetiso.feature_clients.details.mvi.ClientDetailsViewModel
import com.avetiso.navigation.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClientDetailsFragment : Fragment(R.layout.fragment_client_details) {

    private var binding: FragmentClientDetailsBinding? = null
    private val viewModel: ClientDetailsViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    // Переменная для таймера бегущей строки
    private var marqueeJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientDetailsBinding.bind(view)

        setupListeners() // КЛИКИ: Исходящие действия (что я нажимаю)
        setupResultListeners() // ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        observeState() // ДАННЫЕ: Подписка на ViewModel
        observeEvents()
    }

    private fun setupListeners() {
        val currentBinding = binding ?: return

        currentBinding.toolbar.setNavigationOnClickListener {
            viewModel.onBackClicked()
        }

        currentBinding.btnEdit.setOnClickListener {
            viewModel.onEditClicked()
        }

        currentBinding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(DELETE_REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val isConfirmed = bundle.getBoolean(DeleteDialogFragment.RESULT_CONFIRMED)
            if (isConfirmed) {
                viewModel.onDeleteClicked()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ClientDetailsEvent.EditClient -> {
                            val clientId = viewModel.state.value.client?.id
                            if (clientId != null) {
                                clientsNavigator.navigateToAddEditClient(findNavController(), clientId)
                            }
                        }

                        is ClientDetailsEvent.NavigateBack -> {
                            findNavController().navigateUp()
                        }

                        is ClientDetailsEvent.DeleteClient -> {
                            // 1. Показываем сообщение пользователю
                            android.widget.Toast.makeText(requireContext(), "Клиент удален", android.widget.Toast.LENGTH_SHORT)
                                .show()
                            // 2. Закрываем экран
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun render(state: com.avetiso.feature_clients.details.mvi.ClientDetailsState) {
        val currentBinding = binding ?: return

        currentBinding.progressBar.isVisible = state.isLoading

        state.client?.let { client ->
            currentBinding.toolbar.title = ""
            currentBinding.tvToolbarTitle.text = client.name

            // ЛОГИКА ЗАДЕРЖКИ БЕГУЩЕЙ СТРОКИ

            // 1. Отменяем предыдущий таймер, если он был
            marqueeJob?.cancel()

            // 2. Сбрасываем выделение (останавливаем строку и возвращаем в начало)
            currentBinding.tvToolbarTitle.isSelected = false

            // 3. Запускаем новый таймер
            marqueeJob = viewLifecycleOwner.lifecycleScope.launch {
                // Ждем 2 секунды (2000 миллисекунд)
                delay(2000)

                // Если фрагмент еще жив и binding не null — запускаем
                if (isActive) {
                    currentBinding.tvToolbarTitle.isSelected = true
                }
            }

            currentBinding.tvPhone.text = client.phoneNumber

            val formattedInstagram = when {
                client.instagram.isBlank() -> ""
                !client.instagram.startsWith("@") -> "@${client.instagram}"
                else -> client.instagram
            }

            currentBinding.tvInstagram.text = formattedInstagram
            currentBinding.tvInstagram.isVisible = formattedInstagram.isNotBlank()
            currentBinding.tvInstagram.paintFlags = currentBinding.tvInstagram.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            // Очищаем контейнер перед добавлением полей, чтобы не дублировать при обновлениях
            currentBinding.infoContainer.removeAllViews()

            // ✅ 1. СКИДКА
            if (client.discount > 0) {
                addDetailField(
                    label = "Личная скидка", // Более понятное название
                    value = "${client.discount}%",
                    iconResId = com.avetiso.core.R.drawable.ic_percent
                )
            }

            // ✅ 2. ИСТОЧНИК
            if (client.source.isNotBlank()) {
                addDetailField(
                    label = "Источник привлечения",
                    value = client.source,
                    iconResId = com.avetiso.core.R.drawable.ic_label // Создай или используй существующую
                )
            }

            // ✅ 3. КАСТОМНЫЕ ПОЛЯ
            client.customFields.forEach { (label, value) ->
                if (value.isNotBlank()) {
                    addDetailField(
                        label = label,
                        value = value,
                        iconResId = com.avetiso.core.R.drawable.ic_info // Универсальная иконка для кастомных полей
                    )
                }
            }

            // ✅ 4. ПРИМЕЧАНИЕ
            if (client.note.isNotBlank()) {
                addDetailField(
                    label = "Примечание",
                    value = client.note,
                    iconResId = com.avetiso.core.R.drawable.ic_description // Создай или используй
                )
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val clientName = viewModel.state.value.client?.name ?: ""

        // Используем новый DeleteDialogFragment
        DeleteDialogFragment.newInstance(
            requestKey = DELETE_REQUEST_KEY,
            message = getString(com.avetiso.core.R.string.delete_dialog_message, clientName)
        ).show(childFragmentManager, DeleteDialogFragment.TAG)
    }

    private fun addDetailField(label: String, value: String, iconResId: Int) {
        val currentBinding = binding ?: return

        val fieldBinding = ItemClientDetailFieldBinding.inflate(
            LayoutInflater.from(requireContext()),
            currentBinding.infoContainer,
            false
        )

        fieldBinding.tvLabel.text = label
        fieldBinding.tvValue.text = value
        fieldBinding.ivIcon.setImageResource(iconResId) // Устанавливаем иконку

        currentBinding.infoContainer.addView(fieldBinding.root)
    }

    companion object {
        private const val DELETE_REQUEST_KEY = "delete_client_request" // ✅ Ключ
    }

    override fun onDestroyView() {
        marqueeJob?.cancel()
        marqueeJob = null
        super.onDestroyView()
        binding = null
    }
}