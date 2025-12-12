package com.avetiso.feature_settings.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avetiso.common_ui.utils.DialogUtils
import com.avetiso.feature_settings.R
import com.avetiso.feature_settings.databinding.FragmentSettingsBinding
import com.avetiso.feature_settings.mvi.SettingsEvent
import com.avetiso.feature_settings.mvi.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels()

    // Флаг, чтобы не триггерить onItemSelected при программной установке
    private var isUserAction = false

    // Флаг инициализации
    private var isSpinnerInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Слушаем данные
                launch {
                    viewModel.currentDefaultCurrency.collect { currency ->
                        // Если данные пришли (не null)
                        if (currency != null) {
                            if (!isSpinnerInitialized) {
                                // ✅ ПЕРВЫЙ ЗАПУСК: Настраиваем спиннер сразу с правильным значением
                                initSpinnerWithValue(currency)
                            } else {
                                // ПОСЛЕДУЮЩИЕ ОБНОВЛЕНИЯ: Просто меняем выделение
                                updateSpinnerSelection(currency)
                            }
                        }
                    }
                }

                // 2. Следим за событиями (Диалог)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SettingsEvent.AskConfirmation -> {
                                DialogUtils.showYesNoDialog(
                                    context = requireContext(),
                                    title = "Валюта по умолчанию",
                                    message = "Установить ${event.currency}?",
                                    onPositiveClicked = {
                                        viewModel.confirmCurrencyChange(event.currency)
                                    },
                                    onNegativeClicked = {
                                        viewModel.onDeclineCurrencyChange()
                                    }
                                )
                            }

                            is SettingsEvent.RestoreSelection -> {
                                // Если отказались, возвращаем спиннер назад
                                setSpinnerSelection(event.currency)
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ Метод инициализации (вызывается 1 раз)
    private fun initSpinnerWithValue(currency: String) {
        val currencies = listOf("BYN", "USD", "EUR", "RUB", "KZT", "BTC", "ETH", "USDT")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.spinnerDefaultCurrency?.adapter = adapter

        // 1. Сразу находим и ставим нужную позицию
        val position = currencies.indexOf(currency)
        if (position >= 0) {
            binding?.spinnerDefaultCurrency?.setSelection(position, false) // false = без анимации
        }

        // 2. Только ПОСЛЕ установки значения вешаем слушатель
        // Используем post, чтобы гарантировать, что слушатель не сработает на саму инициализацию
        binding?.spinnerDefaultCurrency?.post {
            binding?.spinnerDefaultCurrency?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.onCurrencySelected(currencies[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        isSpinnerInitialized = true
    }

    // Метод обновления (если нажали "Нет" в диалоге или данные обновились извне)
    private fun updateSpinnerSelection(currency: String) {
        val adapter = binding?.spinnerDefaultCurrency?.adapter as? ArrayAdapter<String> ?: return
        val position = adapter.getPosition(currency)
        if (position >= 0 && binding?.spinnerDefaultCurrency?.selectedItemPosition != position) {
            binding?.spinnerDefaultCurrency?.setSelection(position, false)
        }
    }

    private fun setSpinnerSelection(currency: String?) {
        val adapter = binding?.spinnerDefaultCurrency?.adapter as? ArrayAdapter<String> ?: return
        val position = adapter.getPosition(currency)
        if (position >= 0) {
            // Временно отключаем флаг, чтобы не вызвать onItemSelected снова
            isUserAction = false
            binding?.spinnerDefaultCurrency?.setSelection(position)
            binding?.spinnerDefaultCurrency?.post { isUserAction = true }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}