package com.avetiso.feature_schedule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_schedule.add_appointment.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private var binding: FragmentScheduleBinding? = null

    // Используем новую ViewModel
    private val calendarViewModel: CalendarViewModel by viewModels()

    // Менеджер календаря будет null, пока View не создано
    private var calendarManager: CalendarManager? = null

    private val appointmentAdapter = AppointmentAdapter()

    // Данные для примера
    private val dummyAppointments = mapOf(
        LocalDate.now() to listOf(
            Appointment(LocalTime.of(10, 0), "Маникюр", "Анна", 90),
            Appointment(LocalTime.of(12, 30), "Педикюр", "Мария", 120)
        ),
        LocalDate.now().plusDays(1) to listOf(
            Appointment(LocalTime.of(11, 0), "Стрижка", "Ольга", 60)
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentScheduleBinding.bind(view)
        binding = currentBinding

        // Настройка RecyclerView остается здесь
        currentBinding.recyclerViewAppointments.adapter = appointmentAdapter

        // Инициализируем и настраиваем календарь
        calendarManager = CalendarManager(
            calendarView = currentBinding.calendarView,
            viewModel = calendarViewModel,
            context = requireContext()
        ).also { it.setupCalendar() }

        setupClickListeners()
        observeViewModel()

        // Первоначальное обновление списка записей
        updateAppointments(calendarViewModel.state.value.selectedDate)
    }

    private fun setupClickListeners() {
        val currentBinding = binding ?: return
        currentBinding.buttonNextMonth.setOnClickListener {
            currentBinding.calendarView.findFirstVisibleMonth()?.let {
                currentBinding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }
        currentBinding.buttonPreviousMonth.setOnClickListener {
            currentBinding.calendarView.findFirstVisibleMonth()?.let {
                currentBinding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
        currentBinding.buttonAddAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleFragment_to_addAppointmentFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.state.collect { state ->
                    // Обновляем UI на основе нового состояния
                    updateMonthTitle(state.visibleMonth)
                    updateAppointments(state.selectedDate)

                    // Сообщаем менеджеру, что нужно обновить View календаря
                    calendarManager?.observeState(state)
                }
            }
        }
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        val monthTitle = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        val yearTitle = yearMonth.year.toString()
        binding?.textMonthTitle?.text = "$monthTitle $yearTitle"
    }

    private fun updateAppointments(date: LocalDate) {
        val appointments = dummyAppointments[date].orEmpty()
        appointmentAdapter.submitList(appointments)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        calendarManager = null // Очищаем ссылку на менеджер
    }
}