package com.avetiso.feature_sidebar.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.dialogs.models.showChangeCurrencyDialog
import com.avetiso.core.AppConstants
import com.avetiso.core.model.AppCurrency
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.databinding.FragmentSettingsBinding
import com.avetiso.feature_sidebar.mvi.SettingsEvent
import com.avetiso.feature_sidebar.mvi.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // Флаг инициализации
    private var isSpinnerInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupResultListeners() // Регистрируем. ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        observeState()
        setupToolbar()
    }

    private fun setupToolbar() {
        // Обработка нажатия на стрелку "Назад"
        binding.toolbar.setNavigationOnClickListener {
            // Возвращается на предыдущий экран
            findNavController().navigateUp()

        }
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            AppConstants.Requests.CHANGE_CURRENCY,
            viewLifecycleOwner
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(AppConstants.Result.RESULT_CONFIRMED)

            if (isConfirmed) {
                viewModel.onConfirmationSuccess()
            } else {
                viewModel.onDeclineCurrencyChange()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Слушаем данные
                launch {
                    viewModel.currentDefaultCurrency.collect { currencyCode ->
                        // Если данные пришли (не null)
                        if (currencyCode != null) {
                            if (!isSpinnerInitialized) {
                                // ✅ ПЕРВЫЙ ЗАПУСК: Настраиваем спиннер сразу с правильным значением
                                initSpinnerWithValue(currencyCode)
                            } else {
                                // ПОСЛЕДУЮЩИЕ ОБНОВЛЕНИЯ: Просто меняем выделение
                                setSpinnerSelection(currencyCode)
                            }
                        }
                    }
                }

                // 2. Следим за событиями (Диалог)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SettingsEvent.AskConfirmation -> {
                                showChangeCurrencyDialog(
                                    currencyName = event.currency.name,
                                    requestKey = AppConstants.Requests.CHANGE_CURRENCY,
                                )
                            }

                            is SettingsEvent.RestoreSelection -> {
                                // Если отказались, возвращаем спиннер назад
                                setSpinnerSelection(event.currencyCode)
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ Метод инициализации (вызывается 1 раз)
    private fun initSpinnerWithValue(currency: String) {
        val currencies = AppCurrency.getCodesList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerDefaultCurrency.adapter = adapter

        // 1. Сразу находим и ставим нужную позицию
        val position = currencies.indexOf(currency)
        if (position >= 0) {
            binding.spinnerDefaultCurrency.setSelection(position, false) // false = без анимации
        }

        // 2. Только ПОСЛЕ установки значения вешаем слушатель
        // Используем post, чтобы гарантировать, что слушатель не сработает на саму инициализацию
        binding.spinnerDefaultCurrency.post {
            _binding?.spinnerDefaultCurrency?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    // Получаем строку кода из списка
                    val selectedCode = currencies[position]

                    // Конвертируем строку в строгий тип Enum
                    val selectedCurrency = AppCurrency.fromCode(selectedCode)

                    viewModel.onCurrencySelected(selectedCurrency)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        isSpinnerInitialized = true
    }

    private fun setSpinnerSelection(currencyCode: String?) {
        if (currencyCode == null) return

        @Suppress("UNCHECKED_CAST")
        val adapter = binding.spinnerDefaultCurrency.adapter as? ArrayAdapter<String> ?: return
        val position = adapter.getPosition(currencyCode)

        // Проверяем current position, чтобы не делать лишних движений
        if (position >= 0 && binding.spinnerDefaultCurrency.selectedItemPosition != position) {
            binding.spinnerDefaultCurrency.setSelection(position, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}