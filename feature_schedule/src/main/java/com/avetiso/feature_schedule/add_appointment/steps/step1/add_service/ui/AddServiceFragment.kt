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
import com.avetiso.common_ui.dialogs.ConfirmationDialogFragment
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi.AddServiceEvent
import com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi.AddServiceViewModel
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

    // ФЛАГ: Чтобы отличать программную установку от клика пользователя
    private var isUserAction = false

    // Флаг, чтобы настроить спиннер только один раз при получении данных
    private var isSpinnerSetup = false

    // Сюда мы положим "USD", пока пользователь думает над диалогом.
    private var pendingCurrency: String? = null

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

        binding?.btnSave?.setOnClickListener {
            saveService()
        }

        setupResultListeners() // Регистрируем. ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        setupFields()
        observeUi()
    }

    private fun setupResultListeners() {
        // Слушатель для диалога валюты
        childFragmentManager.setFragmentResultListener(SET_DEFAULT_CURRENCY_KEY, viewLifecycleOwner) { _, bundle ->
            val isConfirmed = bundle.getBoolean(ConfirmationDialogFragment.RESULT_CONFIRMED)

            if (isConfirmed) {
                // Если ДА - берем валюту из памяти и сохраняем
                pendingCurrency?.let { currency ->
                    viewModel.setNewDefaultCurrency(currency)
                }
            }
            // Если НЕТ - ничего делать не надо, просто забываем
            pendingCurrency = null
        }

        // Слушаем результат с экрана выбора категории
        setFragmentResultListener("category_selection") { _, bundle ->
            val selectedCategoryName = bundle.getString("selected_category_name")
            binding?.textCategory?.text = selectedCategoryName
        }

        // Слушатель для времени (продолжительность)
        childFragmentManager.setFragmentResultListener(DURATION_PICKER_KEY, viewLifecycleOwner) { _, bundle ->
            val hour = bundle.getInt(ComposeTimePickerDialogFragment.RESULT_HOUR)
            val minute = bundle.getInt(ComposeTimePickerDialogFragment.RESULT_MINUTE)

            // Передаем данные во ViewModel
            viewModel.setDuration(hour, minute)
        }
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на состояние (для обновления текста продолжительности и т.д.)
                launch {
                    viewModel.uiState.collect { state ->
                        updateDurationText(state.selectedHour, state.selectedMinute)
                        if (state.isPriceFrom) {
                            binding?.toggleBtnPriceFrom?.check(R.id.btn_price_from)
                        } else {
                            binding?.toggleBtnPriceFrom?.uncheck(R.id.btn_price_from)
                        }

                        // ✅ Инициализируем спиннер только когда получили валюту из БД (не null)
                        // и только если еще не инициализировали
                        if (state.selectedCurrency != null && !isSpinnerSetup) {
                            setupCurrencySpinner(state.selectedCurrency)
                            isSpinnerSetup = true
                        }
                    }
                }

                // Новая подписка на события (Toast, навигация)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddServiceEvent.ShowToast -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }

                            is AddServiceEvent.NavigateBackWithResult -> {
                                // Этот код переехал сюда из saveService()
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    "service_updated",
                                    true
                                )
                                findNavController().navigateUp()
                            }

                            is AddServiceEvent.AskToSetDefaultCurrency -> {
                                pendingCurrency = event.currency // Запомнили

                                ConfirmationDialogFragment.newInstance(
                                    requestKey = SET_DEFAULT_CURRENCY_KEY,
                                    title = "Валюта по умолчанию",
                                    message = "Установить ${event.currency}?",
                                    positiveText = "Да",
                                    negativeText = "Нет"
                                ).show(childFragmentManager, ConfirmationDialogFragment.TAG)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupInputFields() {
        // Для текстовых полей используем TextWatcher
        binding?.ietName?.addTextChangedListener {
            // Как только пользователь начинает печатать, убираем ошибку
            binding?.ilName?.error = null
        }
        binding?.ietPrice?.addTextChangedListener {
            binding?.ilPrice?.error = null
        }
    }

    private fun setupFields() {
        binding?.toolbar?.setNavigationOnClickListener { findNavController().navigateUp() }

        setupInputFields()
        setupDurationPicker()
        setupCategoryPicker()
        setupPriceToggle()
    }

    private fun populateFieldsForEdit(service: ServiceEntity) {
        binding?.toolbar?.title = "Редактировать услугу"
        binding?.ietName?.setText(service.name)
        binding?.textCategory?.text = service.categoryName
        binding?.ietPrice?.setText(service.price.toString())

        // Обновляем состояние в ViewModel, чтобы все работало корректно
        val hours = service.durationMinutes / 60
        val minutes = service.durationMinutes % 60
        viewModel.setDuration(hours, minutes)
        viewModel.setPriceFrom(service.isPriceFrom)
        viewModel.setCurrency(service.currency)
    }

    private fun saveService() {
        val name = binding?.ietName?.text?.toString()
        val category = binding?.textCategory?.text?.toString()
        val priceStr = binding?.ietPrice?.text?.toString()
        val duration = binding?.textDuration?.text?.toString()

        // Получаем актуальное состояние прямо из ViewModel
        val currentState = viewModel.uiState.value

        when {
            name.isNullOrBlank() -> {
                binding?.ilName?.error = "Название не может быть пустым"
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
                binding?.ilPrice?.error = "Укажите цену"
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
            }
        }
    }

    private fun setupDurationPicker() {
        binding?.textDuration?.setOnClickListener {

            val currentState = viewModel.uiState.value
            showDurationPickerDialog(currentState.selectedHour, currentState.selectedMinute)
        }
    }

    private fun showDurationPickerDialog(hour: Int, minute: Int) {
        ComposeTimePickerDialogFragment.newInstance(
            requestKey = DURATION_PICKER_KEY,
            title = "Выберите продолжительность",
            initialHour = hour,
            initialMinute = minute
            // extraId нам здесь не нужен, по умолчанию будет -1
        ).show(childFragmentManager, "HourMinutePickerDialogFragment")
    }

    private fun setupCategoryPicker() {
        binding?.textCategory?.setOnClickListener {
            // Сбрасываем фон ПЕРЕД переходом на другой экран
            binding?.textCategory?.background = null

            findNavController().navigate(R.id.action_addServiceFragment_to_selectCategoryFragment)
        }
    }

    private fun setupPriceToggle() {
        binding?.toggleBtnPriceFrom?.addOnButtonCheckedListener { _, _, isChecked ->
            // Сообщаем ViewModel об изменении
            viewModel.setPriceFrom(isChecked)
        }
    }

    private fun setupCurrencySpinner(selectedCurrency: String) {
        val currencies = listOf("GEL", "USD", "EUR")

        // Позже мы добавим сюда логику "избранных" валют

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.spinnerCurrency?.adapter = adapter

        // 1. Устанавливаем начальное значение (из ViewModel) БЕЗ вызова слушателя
        val initialCurrency = viewModel.uiState.value.selectedCurrency
        val initialIndex = currencies.indexOf(initialCurrency)
        if (initialIndex >= 0) {
            binding?.spinnerCurrency?.setSelection(initialIndex, false)
        }

        // 2. Настраиваем слушатель с проверкой флага
        binding?.spinnerCurrency?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Реагируем ТОЛЬКО если это действие пользователя
                if (isUserAction) {
                    viewModel.onCurrencySelectedInSpinner(currencies[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 3. Активируем флаг с задержкой (через post),
        // чтобы пропустить автоматические вызовы при инициализации layout
        binding?.spinnerCurrency?.post {
            isUserAction = true
        }
    }

    private fun updateDurationText(hour: Int, minute: Int) {
        binding?.textDuration?.text = String.format("%d ч %02d мин", hour, minute)
    }

    companion object {
        private const val SET_DEFAULT_CURRENCY_KEY = "set_default_currency_request"
        private const val DURATION_PICKER_KEY = "duration_picker_request"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        isSpinnerSetup = false
        isUserAction = false
    }
}