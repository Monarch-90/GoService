package com.avetiso.feature_sidebar.settings.ui

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
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.common_ui.dialogs.models.showChangeCurrencyDialog
import com.avetiso.common_ui.dialogs.models.showDeleteCustomCurrencyDialog
import com.avetiso.core.AppConstants
import com.avetiso.core.model.CurrencyListItem
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.databinding.FragmentSettingsBinding
import com.avetiso.feature_sidebar.settings.mvi.SettingsEvent
import com.avetiso.feature_sidebar.settings.mvi.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private var currentListItems: List<CurrencyListItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupToolbar()
        setupResultListeners() // Регистрируем. ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        setupSpinner()
        observeState()
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

        // Результат из диалога ввода новой валюты
        childFragmentManager.setFragmentResultListener(
            AppConstants.Requests.ADD_CUSTOM_CURRENCY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newCurrency = bundle.getString(AppConstants.Result.RESULT_TEXT)
            if (!newCurrency.isNullOrBlank()) {
                viewModel.addCustomCurrency(newCurrency)
            }
        }
    }

    private fun setupSpinner() {
        binding.spinnerDefaultCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (currentListItems.isNotEmpty()) {
                    val selectedItem = currentListItems[position]
                    viewModel.onItemSelected(selectedItem)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Слушаем список валют из UseCase
                launch {
                    viewModel.currencyList.collect { list ->
                        if (list.isNotEmpty()) {
                            currentListItems = list
                            updateSpinnerAdapter(list)
                            // Обязательно восстанавливаем текущее выделение после обновления адаптера
                            setSpinnerSelection(viewModel.currentDefaultCurrency.value)
                        }
                    }
                }

                // 2. Слушаем текущую дефолтную валюту
                launch {
                    viewModel.currentDefaultCurrency.collect { currencyCode ->
                        setSpinnerSelection(currencyCode)
                    }
                }

                // 3. Слушаем события
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SettingsEvent.AskConfirmation -> {
                                showChangeCurrencyDialog(
                                    currencyName = event.currencyCode,
                                    requestKey = AppConstants.Requests.CHANGE_CURRENCY,
                                )
                            }

                            is SettingsEvent.RestoreSelection -> {
                                setSpinnerSelection(event.currencyCode)
                            }

                            is SettingsEvent.ShowAddCurrencyDialog -> {
                                showAddCurrencyDialog()
                            }

                            is SettingsEvent.ShowDeleteCurrencyDialog -> {
                                showDeleteCurrencyDialog(event.currencies)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateSpinnerAdapter(list: List<CurrencyListItem>) {
        val labels = list.map { item ->
            when (item) {
                is CurrencyListItem.ActionAdd -> item.label
                is CurrencyListItem.ActionDelete -> item.label
                is CurrencyListItem.Currency -> item.code
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 1. Снимаем слушатель, чтобы изолировать UI от системных ложных кликов
        binding.spinnerDefaultCurrency.onItemSelectedListener = null

        // 2. Привязываем новый адаптер
        binding.spinnerDefaultCurrency.adapter = adapter

        // 3. Ищем, где находится наша дефолтная валюта в новом списке
        val currentCurrency = viewModel.currentDefaultCurrency.value
        var position = list.indexOfFirst { it is CurrencyListItem.Currency && it.code == currentCurrency }

        // ЗАЩИТА ПРИ УДАЛЕНИИ (Пункт 3):
        // Если валюту удалили (ее больше нет в списке), переключаемся на первую доступную
        if (position == -1) {
            position = list.indexOfFirst { it is CurrencyListItem.Currency }
        }

        // Выбираем правильную позицию тихо, без вызова событий
        if (position >= 0) {
            binding.spinnerDefaultCurrency.setSelection(position, false)
        }

        // 4. Вешаем слушатель обратно через очередь (Пункт 1)
        binding.spinnerDefaultCurrency.post {
            binding.spinnerDefaultCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (currentListItems.isNotEmpty()) {
                        viewModel.onItemSelected(currentListItems[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setSpinnerSelection(currencyCode: String?) {
        if (currencyCode == null || currentListItems.isEmpty()) return

        val position = currentListItems.indexOfFirst { it is CurrencyListItem.Currency && it.code == currencyCode }

        if (position >= 0 && binding.spinnerDefaultCurrency.selectedItemPosition != position) {
            // При программном откате выбора (например, когда нажали "+ Добавить")
            // тоже отключаем слушатель, чтобы не зациклить логику
            binding.spinnerDefaultCurrency.post {
                val listener = binding.spinnerDefaultCurrency.onItemSelectedListener
                binding.spinnerDefaultCurrency.onItemSelectedListener = null

                binding.spinnerDefaultCurrency.setSelection(position, false)

                binding.spinnerDefaultCurrency.post {
                    binding.spinnerDefaultCurrency.onItemSelectedListener = listener
                }
            }
        }
    }

    private fun showAddCurrencyDialog() {
        // Передаем существующие валюты в forbiddenValues, чтобы исключить создание дубликатов
        val forbiddenValues = currentListItems.mapNotNull { if (it is CurrencyListItem.Currency) it.code else null }

        InputDialogFragment.newInstance(
            requestKey = AppConstants.Requests.ADD_CUSTOM_CURRENCY,
            title = getString(com.avetiso.core.R.string.add_currency_title),
            hint = getString(com.avetiso.core.R.string.add_currency_hint),
            forbiddenValues = forbiddenValues,
            allowEmpty = false
        ).show(childFragmentManager, AppConstants.Result.INPUT_DIALOG)
    }

    private fun showDeleteCurrencyDialog(currencies: Array<String>) {
        showDeleteCustomCurrencyDialog(currencies) { currencyToDelete ->
            viewModel.deleteCustomCurrency(currencyToDelete)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}