package com.avetiso.feature_schedule.ui

import android.os.Bundle
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
import com.avetiso.common_ui.appointments.showAppointmentDeleteDialog
import com.avetiso.common_ui.appointments.showAppointmentNoteDialog
import com.avetiso.common_ui.appointments.showAppointmentStatusDialog
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.ScheduleConstants
import com.avetiso.feature_schedule.add_appointment.adapter.AppointmentAdapter
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.avetiso.feature_schedule.calendar.ui.CalendarManager
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding
import com.avetiso.feature_schedule.mvi.ScheduleState
import com.avetiso.feature_schedule.mvi.ScheduleViewModel
import com.avetiso.navigation.controllers.SidebarController
import com.avetiso.navigation.routers.ScheduleNavigator
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    @Inject
    lateinit var scheduleNavigator: ScheduleNavigator
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    // Менеджер календаря будет null, пока View не создано
    private var calendarManager: CalendarManager? = null
    private var appointmentAdapter: AppointmentAdapter? = null
    private var actions: RecyclerViewActions<Appointment>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScheduleBinding.bind(view)

        setupAdapter()
        setupCalendar()
        setupClickListeners()
        setupResultListeners()
        observeViewModel()
    }

    private fun setupAdapter() {
        val adapter = AppointmentAdapter(
            onStatusClicked = { appointment ->
                showAppointmentStatusDialog(
                    appointment = appointment,
                    rescheduleRequestKey = ScheduleConstants.Requests.RESCHEDULE_DATE_KEY,
                    onStatusSelected = { status ->
                        scheduleViewModel.updateAppointmentStatus(appointment.id, status)
                    }
                )
            },
            onNoteClicked = { appointment ->
                scheduleViewModel.onEditNoteClicked(appointment.id)
                showAppointmentNoteDialog(
                    currentNote = appointment.note,
                    requestKey = ScheduleConstants.Requests.INPUT_NOTE_KEY
                )
            }
        ).also { appointmentAdapter = it }

        binding.rvAppointments.adapter = adapter

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding.rvAppointments,
            adapter = adapter,
            getItemId = { it.id },
            onEdit = { appointment ->
                scheduleNavigator.navigateToAddEditAppointment(
                    navController = findNavController(),
                    appointmentId = appointment.id,
                    selectedDate = calendarViewModel.state.value.selectedDate?.toString()
                )
            },
            onDeleteClicked = { appointment ->
                scheduleViewModel.onDeleteIconClicked(appointment.id)
                // Вызываем общий диалог
                showAppointmentDeleteDialog(ScheduleConstants.Requests.APPOINTMENT_DELETE)
            },
            onItemClick = { /* TODO: Логика клика */ },
            onActionsShown = {
                android.util.Log.d("ScheduleDebug", "onActionsShown - скрываем btnAddAppointment")

                binding.btnAddAppointment.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction {
                        // Проверяем binding перед обращением в callback анимации
                        _binding?.btnAddAppointment?.visibility = View.INVISIBLE
                    }
                    .start()
            },
            onActionsDismissed = {
                android.util.Log.d("ScheduleDebug", "onActionsDismissed - показываем btnAddAppointment")

                binding.btnAddAppointment.visibility = View.VISIBLE
                binding.btnAddAppointment.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .withEndAction(null)
                    .start()
            },
            triggerMode = TriggerMode.SWIPE_REVEAL
        )
        adapter.actions = actions
    }

    private fun setupCalendar() {
        calendarManager = CalendarManager(
            calendarView = binding.calendarView,
            viewModel = calendarViewModel,
            context = requireContext()
        ).also { it.setupCalendar(calendarViewModel.state.value.visibleMonth) }
    }

    private fun setupClickListeners() {
        binding.btnNextMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }
        binding.btnPreviousMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
        binding.btnAddAppointment.setOnClickListener {
            android.util.Log.d("ScheduleDebug", "btnAddAppointment clicked! Visibility = ${binding.btnAddAppointment.visibility}")

            // Получаем выбранную дату из ViewModel календаря
            val selectedDate = calendarViewModel.state.value.selectedDate

            // Создаем action с передачей аргумента
            if (selectedDate != null) {
                // Если дата выбрана, переходим на экран добавления
                val action = ScheduleFragmentDirections.actionScheduleFragmentToAddAppointmentFragment(
                    selectedDate = selectedDate.toString(),
                    appointmentId = AppConstants.ID_NONE
                )
                findNavController().navigate(action)
            } else {
                // Если дата не выбрана, показываем подсказку
                Toast.makeText(requireContext(), R.string.Пожалуйста_выберите_день, Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            // Мы проверяем: "Является ли родительская Activity контроллером шторки?"
            // Если да — вызываем метод. Если нет — ничего не делаем (безопасно).
            (requireActivity() as? SidebarController)?.openSideDrawer()
        }
    }

    private fun setupResultListeners() {
        // Ловим введенный текст
        childFragmentManager.setFragmentResultListener(
            ScheduleConstants.Requests.INPUT_NOTE_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(AppConstants.Result.RESULT_TEXT) ?: ""
            scheduleViewModel.onNoteDialogResult(text)
        }

        // Слушаем результат выбора даты
        childFragmentManager.setFragmentResultListener(
            ScheduleConstants.Requests.RESCHEDULE_DATE_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedMillis = bundle.getLong(AppConstants.Result.RESULT_DATE)
            val appointmentId = bundle.getLong(AppConstants.Result.DATE_RESULT_EXTRA_ID)

            if (appointmentId != AppConstants.ID_NONE) {
                val sdf = SimpleDateFormat(AppConstants.Format.FULL_DATE_FORMAT, Locale.getDefault())
                val newDate = sdf.format(Date(selectedMillis))
                // ID пришел из диалога, всё надежно
                scheduleViewModel.updateAppointmentStatus(
                    appointmentId,
                    AppointmentStatus.RESCHEDULED,
                    newDate
                )
            }
        }

        // Слушаем результат диалога удаления записи
        childFragmentManager.setFragmentResultListener(
            ScheduleConstants.Requests.APPOINTMENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
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

                        android.util.Log.d(
                            "ScheduleDebug",
                            "calendar state collected: new_date=${state.selectedDate}, prev_date=$previousSelectedDate"
                        )

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
                                Toast.makeText(requireContext(), getString(state.messageResId), Toast.LENGTH_LONG).show()
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
        binding.textMonthTitle.text = "$monthTitle $yearTitle"
    }

    override fun onDestroyView() {
        binding.rvAppointments.adapter = null
        _binding = null
        calendarManager = null // Очищаем ссылку на менеджер
        appointmentAdapter = null
        actions = null
        super.onDestroyView()
    }
}