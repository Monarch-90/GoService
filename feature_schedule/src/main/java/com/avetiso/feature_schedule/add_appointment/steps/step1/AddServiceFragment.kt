package com.avetiso.feature_schedule.add_appointment.steps.step1

import android.os.Bundle
import android.view.View
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.AddServiceViewModel
import com.avetiso.feature_schedule.databinding.FragmentAddServiceBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddServiceFragment : Fragment(R.layout.fragment_add_service) {

    private var binding: FragmentAddServiceBinding? = null
    private val viewModel: AddServiceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddServiceBinding.bind(view)

        // Слушаем результат с экрана выбора категории
        setFragmentResultListener("category_selection") { _, bundle ->
            val selectedCategoryName = bundle.getString("selected_category_name")
            binding?.textCategory?.text = selectedCategoryName
        }

        childFragmentManager.setFragmentResultListener(
            "duration_selection",
            this // `this` - это ссылка на сам AddServiceFragment
        ) { _, bundle ->
            val hour = bundle.getInt("hour")
            val minute = bundle.getInt("minute")
            viewModel.setDuration(hour, minute)
        }

        setupMenu()
        setupFields()
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Обновляем UI каждый раз, когда меняется состояние
                    updateDurationText(state.selectedHour, state.selectedMinute)
                    // Можно также обновлять и другие элементы, если они будут в ViewModel
                    // binding?.toggleButtonPriceFrom?.isChecked = state.isPriceFrom
                }
            }
        }
    }

    private fun setupFields() {
        binding?.toolbar?.setNavigationOnClickListener { findNavController().navigateUp() }

        setupDurationPicker()
        setupCategoryPicker()
        setupPriceToggle()
        setupCurrencySpinner()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_service_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_save) {
                    saveService() // <-- ВЫЗОВ МЕТОДА СОХРАНЕНИЯ
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveService() {
        // Сначала сбрасываем все предыдущие ошибки
        binding?.inputLayoutName?.error = null
        binding?.inputLayoutPrice?.error = null
        binding?.textCategory?.background = null // Убираем рамку

        val name = binding?.inputEditTextName?.text?.toString()
        val category = binding?.textCategory?.text?.toString()
        val priceStr = binding?.inputEditTextPrice?.text?.toString()

        // Получаем актуальное состояние прямо из ViewModel
        val currentState = viewModel.uiState.value

        when {
            name.isNullOrBlank() -> {
                binding?.inputLayoutName?.error = "Название не может быть пустым"
            }

            category.isNullOrBlank() || category == "Выбрать категорию" -> {
                // Применяем нашу красную рамку к TextView
                binding?.textCategory?.setBackgroundResource(R.drawable.error_border)
                // Можно также показать короткое сообщение
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, выберите категорию",
                    Toast.LENGTH_SHORT
                ).show()
            }

            priceStr.isNullOrBlank() -> {
                binding?.inputLayoutPrice?.error = "Укажите цену"
            }

            else -> {
                // Все проверки пройдены, можно сохранять
                val durationInMinutes = currentState.selectedHour * 60 + currentState.selectedMinute
                val newService = ServiceEntity(
                    name = name,
                    categoryName = category,
                    isPriceFrom = currentState.isPriceFrom,
                    price = priceStr.toDouble(),
                    currency = currentState.selectedCurrency,
                    durationMinutes = durationInMinutes
                )

                viewModel.saveService(newService)

                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "new_service",
                    newService
                )
                findNavController().navigateUp()
            }
        }
    }

    private fun setupDurationPicker() {
        binding?.textDuration?.setOnClickListener {
            // Берём актуальные значения из ViewModel перед открытием диалога
            val currentState = viewModel.uiState.value
            showDurationPickerDialog(currentState.selectedHour, currentState.selectedMinute)
        }
    }

    private fun showDurationPickerDialog(hour: Int, minute: Int) {
        val dialog = DurationPickerDialogFragment().apply {
            arguments = Bundle().apply {
                putInt("hour", hour)
                putInt("minute", minute)
            }
        }
        dialog.show(childFragmentManager, "DurationPickerDialogFragment")
    }

    private fun setupCategoryPicker() {
        binding?.textCategory?.setOnClickListener {
            findNavController().navigate(R.id.action_addServiceFragment_to_selectCategoryFragment)
        }
    }

    private fun setupPriceToggle() {
        binding?.toggleButtonPriceFrom?.addOnButtonCheckedListener { _, _, isChecked ->
            // Сообщаем ViewModel об изменении
            viewModel.setPriceFrom(isChecked)
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("BYN", "USD", "EUR", "RUB", "KZT", "BTC", "ETH", "USDT")

        // Позже мы добавим сюда логику "избранных" валют

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.spinnerCurrency?.adapter = adapter

        val initialCurrencyIndex = currencies.indexOf(viewModel.uiState.value.selectedCurrency)
        if (initialCurrencyIndex != -1) {
            binding?.spinnerCurrency?.setSelection(initialCurrencyIndex)
        }

        binding?.spinnerCurrency?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Сообщаем ViewModel об изменении
                    viewModel.setCurrency(currencies[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }



    private fun updateDurationText(hour: Int, minute: Int) {
        binding?.textDuration?.text = String.format("%d ч %02d мин", hour, minute)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}