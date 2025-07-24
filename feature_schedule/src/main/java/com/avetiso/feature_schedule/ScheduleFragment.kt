package com.avetiso.feature_schedule

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

// Убираем лишние импорты и добавляем новые
class ScheduleFragment : Fragment(R.layout.fragment_schedule) { // Передаем макет в конструктор

    private var binding: FragmentScheduleBinding? = null

    // Переменная для хранения выбранной даты
    private var selectedDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScheduleBinding.bind(view)

        val currentBinding = binding ?: return
        val calendarView = currentBinding.calendarView

// Новый блок с передачей обработчика
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Передаем в конструктор наш обработчик
            override fun create(view: View) = DayViewContainer(view) { day ->
                // Логика, которая выполняется при клике на день
                if (selectedDate != day.date) {
                    val oldDate = selectedDate
                    selectedDate = day.date
                    // Обновляем старую и новую ячейки
                    oldDate?.let { calendarView.notifyDateChanged(it) }
                    calendarView.notifyDateChanged(day.date)

                    // TODO: Здесь будем загружать записи для `selectedDate`
                    Toast.makeText(
                        requireContext(),
                        "Выбрана дата: ${day.date}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                // Остальная часть bind-метода, как в шаге 3
                container.day = data
                val textView = container.textView
                textView.text = dateFormatter.format(data.date)

                if (data.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    if (data.date == selectedDate) {
                        textView.setBackgroundResource(R.drawable.calendar_day_selected_bg)
                        textView.setTextColor(requireContext().getColor(android.R.color.white))
                    } else {
                        textView.background = null
                        textView.setTextColor(requireContext().getColor(android.R.color.black))
                    }
                } else {
                    textView.visibility = View.INVISIBLE
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

        currentBinding.buttonAddAppointment.setOnClickListener {
            Toast.makeText(requireContext(), "Добавить новую запись", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}