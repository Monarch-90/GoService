package com.avetiso.feature_schedule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.common_ui.compose_picker.ComposeDatePickerDialogFragment
import com.avetiso.core.model.ServiceSnapshot
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.avetiso.feature_schedule.mvi.ScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private var binding: FragmentScheduleBinding? = null

    private val calendarViewModel: CalendarViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    // Менеджер календаря будет null, пока View не создано
    private var calendarManager: CalendarManager? = null

    private var appointmentAdapter = AppointmentAdapter { appointment ->
        showStatusSelectionDialog(appointment)
    }
    private var actions: RecyclerViewActions<Appointment>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentScheduleBinding.bind(view)
        binding = currentBinding

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
            onActionsShown = {
                // Когда действия показаны - плавно прячем кнопку "+"
                binding?.btnAddAppointment?.animate()
                    ?.scaleX(0f)
                    ?.scaleY(0f)
                    ?.setDuration(200)
                    ?.withEndAction {
                        binding?.btnAddAppointment?.visibility = View.INVISIBLE
                    }
                    ?.start()
            },
            onActionsDismissed = {
                // Когда действия закрыты - плавно показываем кнопку "+"
                binding?.btnAddAppointment?.visibility = View.VISIBLE
                binding?.btnAddAppointment?.animate()
                    ?.scaleX(1f)
                    ?.scaleY(1f)
                    ?.setDuration(200)
                    ?.start()
            },
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
            val selectedDate = calendarViewModel.state.value.selectedDate

            // Создаем action с передачей аргумента
            if (selectedDate != null) {
                // Если дата выбрана, переходим на экран добавления
                val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(
                    selectedDate = selectedDate.toString(),
                    appointmentId = -1L
                )
                findNavController().navigate(action)
            } else {
                // Если дата не выбрана, показываем подсказку
                Toast.makeText(requireContext(), "Пожалуйста, выберите день", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    var previousSelectedDate: LocalDate? = null
                    calendarViewModel.state.collect { state ->
                        if (previousSelectedDate != null && previousSelectedDate != state.selectedDate) {
                            // 1. СНАЧАЛА закрываем открытую запись.
                            //    В этот момент RecyclerView еще показывает старый список.
                            actions?.dismissActions()
                        }
                        previousSelectedDate = state.selectedDate
                        updateMonthTitle(state.visibleMonth)

                        // 2. И ТОЛЬКО ПОТОМ загружаем данные для новой даты.
                        scheduleViewModel.loadAppointmentsForDate(state.selectedDate?.toString() ?: "")
                        calendarManager?.observeState(state)
                    }
                }

                launch {
                    scheduleViewModel.appointmentsForDate.collect { appointmentsForAdapter ->
                        // Просто передаем готовый список в адаптер
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

    private fun showStatusSelectionDialog(appointment: Appointment) {
        val statuses = arrayOf("Активна", "Исполнена", "Отмена", "Перенос", "Неявка")

        val customTitleView = LayoutInflater.from(requireContext())
            .inflate(R.layout.status_dialog_title, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setCustomTitle(customTitleView)
            .setItems(statuses) { dialog, which ->
                val selectedStatus = statuses[which]
                if (selectedStatus == "Перенос") {
                    showRescheduleDatePicker(appointment)
                } else {
                    scheduleViewModel.updateAppointmentStatus(appointment.id, selectedStatus)
                }
            }
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
        }
        dialog.show()
    }

    private fun showRescheduleDatePicker(appointment: Appointment) {
        val dialog = ComposeDatePickerDialogFragment.newInstance(
            title = "Выберите дату"
        )

        // Устанавливаем слушатель, который сработает при нажатии "ОК"
        dialog.onDateSelected = { selectedMillis ->
            // Конвертируем timestamp в нужный нам формат YYYY-MM-DD
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val newDate = sdf.format(Date(selectedMillis))

            scheduleViewModel.updateAppointmentStatus(appointment.id, "Перенос", newDate)
        }

        dialog.show(childFragmentManager, "ComposeDatePickerDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        calendarManager = null // Очищаем ссылку на менеджер
    }
}