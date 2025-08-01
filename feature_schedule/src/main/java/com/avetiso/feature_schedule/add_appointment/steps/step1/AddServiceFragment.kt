package com.avetiso.feature_schedule.add_appointment.steps.step1

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.AddServiceViewModel
import com.avetiso.feature_schedule.databinding.FragmentAddServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddServiceFragment : Fragment(R.layout.fragment_add_service) {

    private var binding: FragmentAddServiceBinding? = null
    private val viewModel: AddServiceViewModel by viewModels()

    // Получаем аргументы, переданные через Safe Args
    private val args: AddServiceFragmentArgs by navArgs()
    private var serviceToEdit: ServiceEntity? = null

    // ФЛАГ, чтобы отследить первую загрузку
    private var isInitialDataLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddServiceBinding.bind(view)

        // Проверяем, пришел ли объект для редактирования
        serviceToEdit = args.serviceToEdit

        // Заполняем поля только один раз, при первом создании View
        if (serviceToEdit != null && !isInitialDataLoaded) {
            populateFieldsForEdit(serviceToEdit!!)
            isInitialDataLoaded = true // Ставим флаг, что данные загружены
        }

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

        binding?.buttonSave?.setOnClickListener {
            saveService()
        }

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

                    // Мы управляем состоянием группы кнопок
                    if (state.isPriceFrom) {
                        binding?.toggleButtonPriceFrom?.check(R.id.button_price_from)
                    } else {
                        binding?.toggleButtonPriceFrom?.uncheck(R.id.button_price_from)
                    }
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

    private fun populateFieldsForEdit(service: ServiceEntity) {
        binding?.toolbar?.title = "Редактировать услугу"
        binding?.inputEditTextName?.setText(service.name)
        binding?.textCategory?.text = service.categoryName
        binding?.inputEditTextPrice?.setText(service.price.toString())

        // Обновляем состояние в ViewModel, чтобы все работало корректно
        val hours = service.durationMinutes / 60
        val minutes = service.durationMinutes % 60
        viewModel.setDuration(hours, minutes)
        viewModel.setPriceFrom(service.isPriceFrom)
        viewModel.setCurrency(service.currency)
    }

    private fun saveService() {
        // Сначала сбрасываем все предыдущие ошибки
        binding?.inputLayoutName?.error = null
        binding?.inputLayoutPrice?.error = null
        binding?.textCategory?.background = null // Убираем рамку
        binding?.textDuration?.background = null // Убираем рамку

        val name = binding?.inputEditTextName?.text?.toString()
        val category = binding?.textCategory?.text?.toString()
        val priceStr = binding?.inputEditTextPrice?.text?.toString()
        val duration = binding?.textDuration?.text?.toString()

        // Получаем актуальное состояние прямо из ViewModel
        val currentState = viewModel.uiState.value

        when {
            name.isNullOrBlank() -> {
                binding?.inputLayoutName?.error = "Название не может быть пустым"
            }

            category.isNullOrBlank() || category == "Выбрать категорию" -> {
                // Применяем красную рамку к TextView
                binding?.textCategory?.setBackgroundResource(R.drawable.error_border)
                // Можно также показать короткое сообщение
                Toast.makeText(
                    requireContext(),
                    "Выберите категорию",
                    Toast.LENGTH_SHORT
                ).show()
            }

            priceStr.isNullOrBlank() -> {
                binding?.inputLayoutPrice?.error = "Укажите цену"
            }

            duration.isNullOrBlank() || duration == "0 ч 00 мин" -> {
                // Применяем красную рамку к TextView
                binding?.textDuration?.setBackgroundResource(R.drawable.error_border)
                Toast.makeText(
                    requireContext(),
                    "Укажите продолжительность",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                // Все проверки пройдены, можно сохранять
                val durationInMinutes = currentState.selectedHour * 60 + currentState.selectedMinute
                val serviceToSave = ServiceEntity(
                    // Если мы редактируем, используем существующий id, иначе оставляем 0 (для новой)
                    id = serviceToEdit?.id ?: 0L,
                    name = name,
                    categoryName = category,
                    isPriceFrom = currentState.isPriceFrom,
                    price = priceStr.toDouble(),
                    currency = currentState.selectedCurrency,
                    durationMinutes = durationInMinutes
                )

                viewModel.saveService(serviceToSave)
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "service_updated",
                    true
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
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
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