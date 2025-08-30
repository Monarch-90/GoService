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
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.avetiso.feature_schedule.mvi.ScheduleViewModel
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

    private val calendarViewModel: CalendarViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    // Менеджер календаря будет null, пока View не создано
    private var calendarManager: CalendarManager? = null

    private val appointmentAdapter = AppointmentAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentScheduleBinding.bind(view)
        binding = currentBinding

        // Настройка RecyclerView остается здесь
        currentBinding.rvAppointments.adapter = appointmentAdapter

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
            val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(selectedDate)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на ViewModel календаря
                launch {
                    calendarViewModel.state.collect { state ->
                        updateMonthTitle(state.visibleMonth)
                        // Теперь мы просто сообщаем второй ViewModel, какая дата выбрана
                        scheduleViewModel.loadAppointmentsForDate(state.selectedDate.toString())
                        calendarManager?.observeState(state)
                    }
                }

                // Подписка на ViewModel записей (загруженных из БД)
                launch {
                    scheduleViewModel.appointmentsForDate.collect { realAppointments ->
                        // Конвертируем AppointmentEntity в модель для адаптера (Appointment)
                        val appointmentsForAdapter = realAppointments.map { entity ->
                            // Вам нужно будет добавить логику получения имен услуг и клиента по ID,
                            // но пока сделаем заглушки
                            Appointment(
                                time = LocalTime.ofSecondOfDay(entity.startTimeMinutes * 60L),
                                serviceName = "Услуги (IDs: ${entity.serviceIds.joinToString()})",
                                clientName = "Клиент (ID: ${entity.clientId})",
                                durationMinutes = entity.totalDurationMinutes.toLong()
                            )
                        }
                        appointmentAdapter.submitList(appointmentsForAdapter)
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        calendarManager = null // Очищаем ссылку на менеджер
    }
}