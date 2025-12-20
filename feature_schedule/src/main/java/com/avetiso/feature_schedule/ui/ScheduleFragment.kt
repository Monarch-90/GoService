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
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.avetiso.feature_schedule.mvi.ScheduleState
import com.avetiso.feature_schedule.mvi.ScheduleViewModel
import com.avetiso.navigation.DrawerController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private var appointmentAdapter: AppointmentAdapter? = null

    private var actions: RecyclerViewActions<Appointment>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScheduleBinding.bind(view)

        val currentBinding = binding ?: return

        val adapter = AppointmentAdapter(
            onStatusClicked = { appointment -> showStatusSelectionDialog(appointment) },
            onNoteClicked = { appointment -> showNoteDialog(appointment) }
        ).also { appointmentAdapter = it }

        currentBinding.rvAppointments.adapter = adapter

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvAppointments,
            adapter = adapter,
            getItemId = { it.id },
            onEdit = { appointment ->
                val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(
                    selectedDate = calendarViewModel.state.value.selectedDate.toString(),
                    appointmentId = appointment.id // Передаем ID для режима редактирования
                )
                findNavController().navigate(action)
            },
            onDeleteClicked = { appointment ->
                // ✅ 1. Передаем только ID (так как appointment - это UI модель)
                scheduleViewModel.onDeleteIconClicked(appointment.id)

                // ✅ 2. Формируем текст.
                // Внимание: в твоем маппере поле называется serviceNames (во множественном числе)
                val messageText = "Удалить запись?"

                // 3. Показываем диалог
                DeleteDialogFragment.newInstance(
                    requestKey = "delete_appointment_request",
                    message = getString(com.avetiso.core.R.string.delete_dialog_message, messageText)
                ).show(childFragmentManager, DeleteDialogFragment.TAG)
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
        adapter.actions = actions

        // Инициализируем и настраиваем календарь
        calendarManager = CalendarManager(
            calendarView = currentBinding.calendarView,
            viewModel = calendarViewModel,
            context = requireContext()
        ).also { it.setupCalendar(calendarViewModel.state.value.visibleMonth) }

        setupClickListeners()
        setupResultListeners()
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

        currentBinding.toolbar.setNavigationOnClickListener {
            // Мы проверяем: "Является ли родительская Activity контроллером шторки?"
            // Если да — вызываем метод. Если нет — ничего не делаем (безопасно).
            (requireActivity() as? DrawerController)?.openSideDrawer()
        }
    }


    private fun setupResultListeners() {
        // Ловим введенный текст
        childFragmentManager.setFragmentResultListener(INPUT_NOTE_KEY, viewLifecycleOwner) { _, bundle ->
            val text = bundle.getString(InputDialogFragment.RESULT_TEXT) ?: ""
            scheduleViewModel.onNoteDialogResult(text)
        }

        // Слушаем результат выбора даты
        childFragmentManager.setFragmentResultListener(RESCHEDULE_DATE_KEY, viewLifecycleOwner) { _, bundle ->
            val selectedMillis = bundle.getLong(ComposeDatePickerDialogFragment.RESULT_DATE_KEY)
            val appointmentId = bundle.getLong(ComposeDatePickerDialogFragment.RESULT_EXTRA_ID)

            if (appointmentId != -1L) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val newDate = sdf.format(Date(selectedMillis))
                // ID пришел из диалога, всё надежно
                scheduleViewModel.updateAppointmentStatus(appointmentId, "Перенос", newDate)
            }
        }

        // Слушаем результат диалога удаления записи
        childFragmentManager.setFragmentResultListener("delete_appointment_request", viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean(DeleteDialogFragment.RESULT_CONFIRMED)) {
                scheduleViewModel.onDeleteConfirmed()
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
                        appointmentAdapter?.submitList(appointmentsForAdapter)
                    }
                }

                launch {
                    scheduleViewModel.scheduleState.collect { state ->
                        when (state) {
                            is ScheduleState.Success -> {
                                // Если успешно - закрываем диалог и сбрасываем состояние

                                scheduleViewModel.resetScheduleState()
                            }

                            is ScheduleState.Error -> {
                                // Если ошибка - показываем Toast и сбрасываем состояние
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                scheduleViewModel.resetScheduleState()
                            }
                            // В остальных случаях ничего не делаем
                            else -> {}
                        }
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
        ComposeDatePickerDialogFragment.newInstance(
            requestKey = RESCHEDULE_DATE_KEY,
            title = "Выберите дату",
            extraId = appointment.id // Передаем ID записи на хранение в диалог
        ).show(childFragmentManager, "DATE_PICKER")
    }

    private fun showNoteDialog(appointment: Appointment) {
        // Запоминаем ID записи, чтобы обновить её при получении результата
        scheduleViewModel.onEditNoteClicked(appointment.id)

        InputDialogFragment.newInstance(
            requestKey = INPUT_NOTE_KEY,
            title = getString(com.avetiso.core.R.string.Примечание),
            hint = getString(com.avetiso.core.R.string.Введите_текст),
            initialValue = appointment.note,
            isMultiline = true, // Включаем многострочный режим
            allowEmpty = true,
        ).show(childFragmentManager, InputDialogFragment.TAG)
    }

    companion object {
        private const val INPUT_NOTE_KEY = "input_note_request"
        private const val RESCHEDULE_DATE_KEY = "reschedule_date_request"
    }

    override fun onDestroyView() {
        binding?.rvAppointments?.adapter = null
        binding = null
        calendarManager = null // Очищаем ссылку на менеджер
        appointmentAdapter = null
        actions = null
        super.onDestroyView()
    }
}