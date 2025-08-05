package com.avetiso.feature_schedule

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_schedule.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.data.Appointment
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@AndroidEntryPoint
class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private var binding: FragmentScheduleBinding? = null
    private var selectedDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    // Создаем адаптер
    private val appointmentAdapter = AppointmentAdapter()

    // Создаем "искусственные" данные для теста
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
        binding = FragmentScheduleBinding.bind(view)

        val currentBinding = binding ?: return

        // Настраиваем RecyclerView
        currentBinding.recyclerViewAppointments.adapter = appointmentAdapter

        // ... код календаря ...
        val calendarView = currentBinding.calendarView
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer.create(view)

            // Вся логика теперь находится здесь, в методе bind
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                // Прямой доступ к View через биндинг
                val textView = container.binding.dayText
                val verticalDivider = container.binding.verticalDivider

                textView.text = dateFormatter.format(data.date)

                // Установка клика
                container.binding.root.setOnClickListener {
                    if (data.position == DayPosition.MonthDate && selectedDate != data.date) {
                        val oldDate = selectedDate
                        selectedDate = data.date
                        oldDate?.let { calendarView.notifyDateChanged(it) }
                        calendarView.notifyDateChanged(data.date)
                        updateAppointments(data.date)
                    }
                }

                // Логика отображения
                if (data.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    if (data.date == selectedDate) {
                        textView.alpha = 1.0f // Убираем тусклость
                        textView.setTextColor(requireContext().getColor(android.R.color.white))
                        // Можете вернуть фон, если он нужен для выделения
                        // textView.setBackgroundResource(R.drawable.calendar_day_selected_bg)
                    } else {
                        textView.alpha = 1.0f // Убираем тусклость для всего текста
                        textView.background = null
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.custom_black_white
                            )
                        )
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                }

                // ИСПРАВЛЕННАЯ ЛОГИКА СКРЫТИЯ ЛИНИИ (БЕЗ lateinit И БЕЗ ОШИБОК)
                val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                val lastDayOfWeek = firstDayOfWeek.plus(6)
                if (data.date.dayOfWeek == lastDayOfWeek) {
                    verticalDivider.visibility = View.INVISIBLE
                } else {
                    verticalDivider.visibility = View.VISIBLE
                }
            }

        }

        // ... код настройки месяцев и скролла ...
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        // ...

        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: CalendarMonth) {
                val monthTitle =
                    month.yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                        .replaceFirstChar { it.uppercase() }
                val yearTitle = month.yearMonth.year.toString()
                currentBinding.textMonthTitle.text = "$monthTitle $yearTitle"
            }
        }

        // ЛИСЕНЕРЫ ДЛЯ КНОПОК-СТРЕЛОК
        currentBinding.buttonNextMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        currentBinding.buttonPreviousMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }

        currentBinding.buttonAddAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleFragment_to_addAppointmentFragment)
        }
    }

    private fun updateAppointments(date: LocalDate) {
        // Получаем записи на выбранную дату из наших данных
        // или пустой список, если записей нет
        val appointments = dummyAppointments[date].orEmpty()
        appointmentAdapter.submitList(appointments)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}