package com.avetiso.feature_schedule.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.core.model.ServiceSnapshot
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.avetiso.feature_schedule.mvi.ScheduleViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private var binding: FragmentScheduleBinding? = null

    private val calendarViewModel: CalendarViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    // Менеджер календаря будет null, пока View не создано
    private var calendarManager: CalendarManager? = null

    private var appointmentAdapter = AppointmentAdapter()
    private var actions: RecyclerViewActions<Appointment>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentScheduleBinding.bind(view)
        binding = currentBinding

        appointmentAdapter = AppointmentAdapter()
        currentBinding.rvAppointments.adapter = appointmentAdapter

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding!!.rvAppointments,
            adapter = appointmentAdapter,
            getItemId = { it.id },
            getItemName = { "Удалить запись?" },
            onEdit = { appointment ->
                val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(
                    selectedDate = calendarViewModel.state.value.selectedDate.toString(),
                    appointmentId = appointment.id // Передаем ID для режима редактирования
                )
                findNavController().navigate(action)
            },
            onDelete = { appointment ->
                // Просто вызываем метод из ViewModel
                scheduleViewModel.deleteAppointment(appointment.id)
            },
            onItemClick = { /* TODO: Логика клика, если нужна */ },
            onActionsShown = {},
            triggerMode = TriggerMode.SWIPE_REVEAL
        )
        appointmentAdapter.actions = actions

        // Инициализируем и настраиваем календарь
        calendarManager = CalendarManager(
            calendarView = currentBinding.calendarView,
            viewModel = calendarViewModel,
            context = requireContext()
        ).also { it.setupCalendar() }

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        val currentBinding = binding ?: return
        currentBinding.btnNextMonth.setOnClickListener {
            currentBinding.calendarView.findFirstVisibleMonth()?.let {
                currentBinding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }
        currentBinding.btnPreviousMonth.setOnClickListener {
            currentBinding.calendarView.findFirstVisibleMonth()?.let {
                currentBinding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
        currentBinding.btnAddAppointment.setOnClickListener {
            // Получаем выбранную дату из ViewModel календаря
            val selectedDate = calendarViewModel.state.value.selectedDate.toString()

            // Создаем action с передачей аргумента
            val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(
                selectedDate = selectedDate, // Передаем дату для новой записи
                appointmentId = -1L // Передаем ID по умолчанию, означающий "создать новую"
            )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    calendarViewModel.state.collect { state ->
                        updateMonthTitle(state.visibleMonth)
                        scheduleViewModel.loadAppointmentsForDate(state.selectedDate.toString())
                        calendarManager?.observeState(state)
                    }
                }

                launch {
                    scheduleViewModel.appointmentsForDate.collect { appointmentsList ->
                        val gson = Gson()
                        val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type

                        val appointmentsForAdapter = appointmentsList.map { appointmentEntity ->
                            // ПАРСИМ ДАННЫЕ ИЗ СНИМКА (JSON)
                            val services: List<ServiceSnapshot> =
                                gson.fromJson(appointmentEntity.servicesJson, listType) ?: emptyList()

                            val hours = appointmentEntity.startTimeMinutes / 60
                            val minutes = appointmentEntity.startTimeMinutes % 60
                            val timeString = String.format("%02d:%02d", hours, minutes)

                            val serviceNamesString = services.joinToString(", ") { it.name }

                            val priceString = services
                                .groupBy { it.currency }
                                .map { (currency, servicesInCurrency) ->
                                    val total = servicesInCurrency.sumOf { it.price }
                                    val isPriceFrom = servicesInCurrency.any { it.isPriceFrom }
                                    val prefix = if (isPriceFrom) "от " else ""
                                    "$prefix${"%.2f".format(total)} $currency"
                                }
                                .joinToString("\n")

                            Appointment(
                                id = appointmentEntity.id,
                                time = timeString,
                                serviceNames = serviceNamesString,
                                clientName = appointmentEntity.clientName,
                                price = priceString
                            )
                        }
                        appointmentAdapter.submitList(appointmentsForAdapter)
                    }
                }
            }
        }
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val monthTitle = yearMonth.month.getDisplayName(
            TextStyle.FULL_STANDALONE,
            Locale.forLanguageTag("ru")
        ).replaceFirstChar { it.uppercase() }
        val yearTitle = yearMonth.year.toString()
        binding?.textMonthTitle?.text = "$monthTitle $yearTitle"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        calendarManager = null // Очищаем ссылку на менеджер
    }
}