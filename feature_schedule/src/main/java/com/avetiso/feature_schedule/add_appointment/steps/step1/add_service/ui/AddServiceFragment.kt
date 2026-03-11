package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.avetiso.common_ui.compose_picker.ComposeTimePickerDialogFragment
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.common_ui.dialogs.models.showChangeCurrencyDialog
import com.avetiso.common_ui.dialogs.models.showDeleteCustomCurrencyDialog
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.models.CurrencyListItem
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.AppointmentConstants
import com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi.AddServiceEvent
import com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi.AddServiceViewModel
import com.avetiso.feature_schedule.databinding.FragmentAddServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddServiceFragment : Fragment(R.layout.fragment_add_service) {

    private var _binding: FragmentAddServiceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddServiceViewModel by viewModels()
    private var currentCurrencyItems: List<CurrencyListItem> = emptyList()

    // Получаем аргументы, переданные через Safe Args
    private val args: AddServiceFragmentArgs by navArgs()
    private var serviceToEdit: ServiceEntity? = null

    // ФЛАГ, чтобы отследить первую загрузку
    private var isInitialDataLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddServiceBinding.bind(view)

        // Проверяем, пришел ли объект для редактирования
        serviceToEdit = args.serviceToEdit

        // Заполняем поля только один раз, при первом создании View
        if (serviceToEdit != null && !isInitialDataLoaded) {
            populateFieldsForEdit(serviceToEdit!!)
            isInitialDataLoaded = true // Ставим флаг, что данные загружены
        }

        binding.btnSave.setOnClickListener {
            saveService()
        }

        setupResultListeners() // Регистрируем. ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        setupFields()
        observeState()
    }

    private fun setupResultListeners() {
        // 1. Слушатель для добавления НОВОЙ валюты
        childFragmentManager.setFragmentResultListener(
            AppConstants.Requests.ADD_CUSTOM_CURRENCY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newCurrency = bundle.getString(AppConstants.Result.RESULT_TEXT)
            if (!newCurrency.isNullOrBlank()) {
                viewModel.saveCustomCurrency(newCurrency)
            }
        }

        // 2. Слушатель для сохранения валюты ПО УМОЛЧАНИЮ
        childFragmentManager.setFragmentResultListener(
            AppointmentConstants.Request.SET_DEFAULT_CURRENCY,
            viewLifecycleOwner
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(AppConstants.Result.RESULT_CONFIRMED)
            if (isConfirmed) {
                viewModel.onDefaultCurrencyConfirmed()
            } else {
                viewModel.onDefaultCurrencyDeclined()
            }
        }

        // 3. Слушатель для выбора КАТЕГОРИИ
        setFragmentResultListener(AppointmentConstants.Request.SELECTION_CATEGORY) { _, bundle ->
            val selectedCategoryName = bundle.getString(AppointmentConstants.Result.SELECTED_CATEGORY_NAME)
            if (selectedCategoryName != null) {
                viewModel.setSelectedCategory(selectedCategoryName)
            }
        }

        // 4. Слушатель для ПРОДОЛЖИТЕЛЬНОСТИ
        childFragmentManager.setFragmentResultListener(
            AppointmentConstants.Request.DURATION_PICKER,
            viewLifecycleOwner
        ) { _, bundle ->
            val hour = bundle.getInt(AppConstants.Result.RESULT_HOUR)
            val minute = bundle.getInt(AppConstants.Result.RESULT_MINUTE)

            // Передаем данные во ViewModel
            viewModel.setDuration(hour, minute)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Подписка на основное состояние (UI State)
                launch {
                    viewModel.uiState.collect { state ->
                        updateDurationText(state.selectedHour, state.selectedMinute)
                        if (state.isPriceFrom) {
                            binding.toggleBtnPriceFrom.check(R.id.btn_price_from)
                        } else {
                            binding.toggleBtnPriceFrom.uncheck(R.id.btn_price_from)
                        }

                        if (state.selectedCategoryName != null) {
                            binding.textCategory.text = state.selectedCategoryName
                        } else {
                            binding.textCategory.text = context?.getString(R.string.Выбрать_категорию)
                        }
                    }
                }

                // 1. Слушаем список валют
                launch {
                    viewModel.currencyList.collect { list ->
                        if (list.isNotEmpty()) {
                            currentCurrencyItems = list
                            updateCurrencySpinnerAdapter(list)
                        }
                    }
                }

                // 2. Слушаем дефолтную валюту (для первичной установки)
                launch {
                    viewModel.uiState.collect { state ->
                        // Автоматически выбираем валюту, если она загружена
                        if (state.selectedCurrency != null) {
                            setCurrencySpinnerSelection(state.selectedCurrency)
                        }
                    }
                }

                // Новая подписка на события (Toast, навигация)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddServiceEvent.ShowToast -> {
                                Toast.makeText(
                                    requireContext(),
                                    event.message.asString(
                                        requireContext()
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            is AddServiceEvent.NavigateBackWithResult -> {
                                // Этот код переехал сюда из saveService()
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    AppointmentConstants.Result.SERVICE_UPDATED,
                                    true
                                )
                                findNavController().navigateUp()
                            }

                            is AddServiceEvent.AskToSetDefaultCurrency -> {
                                showChangeCurrencyDialog(
                                    currencyName = event.currencyCode,
                                    requestKey = AppointmentConstants.Request.SET_DEFAULT_CURRENCY,
                                )
                            }

                            is AddServiceEvent.ShowAddCurrencyDialog -> {
                                showAddCurrencyDialog()
                            }

                            is AddServiceEvent.RestoreCurrencySelection -> {
                                setCurrencySpinnerSelection(event.previousCurrencyCode)
                            }

                            is AddServiceEvent.ShowDeleteCurrencyDialog -> {
                                showDeleteCurrencyDialog(event.currencies)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCurrencySpinnerAdapter(list: List<CurrencyListItem>) {
        val labels = list.map { item ->
            when (item) {
                is CurrencyListItem.ActionAdd -> item.label
                is CurrencyListItem.ActionDelete -> item.label
                is CurrencyListItem.Currency -> item.code
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 1. Отключаем слушатель перед манипуляциями
        binding.spinnerCurrency.onItemSelectedListener = null

        // 2. Устанавливаем адаптер
        binding.spinnerCurrency.adapter = adapter

        // 3. Ищем позицию текущей выбранной валюты
        val currentCurrency = viewModel.uiState.value.selectedCurrency
        var position = list.indexOfFirst { it is CurrencyListItem.Currency && it.code == currentCurrency }

        // ЗАЩИТА ОТ СБРОСА: Если валюты больше нет в списке (она была удалена),
        // мы принудительно ищем первую НАСТОЯЩУЮ валюту (обычно это GEL на 1 позиции)
        if (position == -1) {
            position = list.indexOfFirst { it is CurrencyListItem.Currency }
        }

        // Ставим правильный выбор
        if (position >= 0) {
            binding.spinnerCurrency.setSelection(position, false)
        }

        // 4. Возвращаем слушатель
        binding.spinnerCurrency.post {
            binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (currentCurrencyItems.isNotEmpty()) {
                        viewModel.onCurrencyItemSelected(currentCurrencyItems[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setCurrencySpinnerSelection(currencyCode: String?) {
        if (currencyCode == null || currentCurrencyItems.isEmpty()) return

        val position = currentCurrencyItems.indexOfFirst { it is CurrencyListItem.Currency && it.code == currencyCode }

        if (position >= 0 && binding.spinnerCurrency.selectedItemPosition != position) {
            // При программном переключении валюты мы тоже временно снимаем слушатель,
            // чтобы ViewModel не получала ложных эвентов
            binding.spinnerCurrency.post {
                val listener = binding.spinnerCurrency.onItemSelectedListener
                binding.spinnerCurrency.onItemSelectedListener = null

                binding.spinnerCurrency.setSelection(position, false)

                binding.spinnerCurrency.post {
                    binding.spinnerCurrency.onItemSelectedListener = listener
                }
            }
        }
    }

    private fun showAddCurrencyDialog() {
        val forbiddenValues = currentCurrencyItems.mapNotNull { if (it is CurrencyListItem.Currency) it.code else null }

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

    private fun setupInputFields() {
        // Для текстовых полей используем TextWatcher
        binding.ietName.addTextChangedListener {
            // Как только пользователь начинает печатать, убираем ошибку
            binding.ilName.error = null
        }
        binding.ietPrice.addTextChangedListener {
            binding.ilPrice.error = null
        }
    }

    private fun setupFields() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupInputFields()
        setupDurationPicker()
        setupCategoryPicker()
        setupPriceToggle()
    }

    private fun populateFieldsForEdit(service: ServiceEntity) {
        binding.toolbar.title = context?.getString(R.string.Редактировать_услугу)
        binding.ietName.setText(service.name)
        binding.ietPrice.setText(service.price.toString())


        // Обновляем состояние в ViewModel, чтобы все работало корректно
        val hours = service.durationMinutes / 60
        val minutes = service.durationMinutes % 60

        viewModel.setDuration(hours, minutes)
        viewModel.setPriceFrom(service.isPriceFrom)
        viewModel.setCurrency(service.currency)

        // Загружаем категорию во ViewModel
        viewModel.setSelectedCategory(service.categoryName)
    }

    private fun saveService() {
        val name = binding.ietName.text?.toString()
        val priceStr = binding.ietPrice.text?.toString()

        // Получаем актуальное состояние прямо из ViewModel
        val currentState = viewModel.uiState.value
        val category = currentState.selectedCategoryName

        // Считаем общее время в минутах для валидации
        val totalMinutes = currentState.selectedHour * 60 + currentState.selectedMinute

        // Получаем дефолтный текст категории для сравнения
        val defaultCategoryText = context?.getString(R.string.Выбрать_категорию)

        when {
            name.isNullOrBlank() -> {
                binding.ilName.error = context?.getString(R.string.Название_не_может_быть_пустым)
            }

            category.isNullOrBlank() || category == defaultCategoryText -> {
                // Применяем красную рамку к TextView
                binding.textCategory.setBackgroundResource(R.drawable.error_border)
                // Можно также показать короткое сообщение
                Toast.makeText(
                    requireContext(),
                    context?.getString(R.string.Выберите_категорию),
                    Toast.LENGTH_SHORT
                ).show()
            }

            priceStr.isNullOrBlank() -> {
                binding.ilPrice.error = context?.getString(R.string.Укажите_цену)
            }

            totalMinutes == 0 -> {
                binding.textDuration.setBackgroundResource(R.drawable.error_border)
                Toast.makeText(
                    requireContext(),
                    context?.getString(
                        R.string.Укажите_продолжительность
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                // Все проверки пройдены, можно сохранять
                val serviceToSave = ServiceEntity(
                    // Если мы редактируем, используем существующий id, иначе оставляем 0 (для новой)
                    id = serviceToEdit?.id ?: 0L,
                    name = name,
                    categoryName = category,
                    isPriceFrom = currentState.isPriceFrom,
                    price = priceStr.toDouble(),
                    currency = currentState.selectedCurrency,
                    durationMinutes = totalMinutes
                )

                viewModel.saveService(serviceToSave)
            }
        }
    }

    private fun setupDurationPicker() {
        binding.textDuration.setOnClickListener {

            val currentState = viewModel.uiState.value
            showDurationPickerDialog(currentState.selectedHour, currentState.selectedMinute)
        }
    }

    private fun showDurationPickerDialog(hour: Int, minute: Int) {
        ComposeTimePickerDialogFragment.newInstance(
            requestKey = AppointmentConstants.Request.DURATION_PICKER,
            title = context?.getString(R.string.Выберите_продолжительность).toString(),
            initialHour = hour,
            initialMinute = minute
            // extraId нам здесь не нужен, по умолчанию будет -1
        ).show(childFragmentManager, AppointmentConstants.Tags.DURATION_PICKER_DIALOG)
    }

    private fun setupCategoryPicker() {
        binding.textCategory.setOnClickListener {
            // Сбрасываем фон ПЕРЕД переходом на другой экран
            binding.textCategory.background = null

            findNavController().navigate(R.id.action_addServiceFragment_to_selectCategoryFragment)
        }
    }

    private fun setupPriceToggle() {
        binding.toggleBtnPriceFrom.addOnButtonCheckedListener { _, _, isChecked ->
            // Сообщаем ViewModel об изменении
            viewModel.setPriceFrom(isChecked)
        }
    }

    private fun updateDurationText(hour: Int, minute: Int) {
        binding.textDuration.text = String.format(AppConstants.Format.DURATION, hour, minute)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}