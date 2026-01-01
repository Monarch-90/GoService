package com.avetiso.feature_schedule.add_appointment.steps.step2.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.compose_picker.ComposeTimePickerDialogFragment
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.AppointmentConstants
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step2.adapter.TimeSlotAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step2.mvi.Step2Event
import com.avetiso.feature_schedule.add_appointment.steps.step2.mvi.Step2SelectTimeViewModel
import com.avetiso.feature_schedule.databinding.FragmentStep2SelectTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Step2SelectTimeFragment : Fragment(R.layout.fragment_step2_select_time) {

    private var binding: FragmentStep2SelectTimeBinding? = null
    private var timeSlotAdapter: TimeSlotAdapter? = null

    private var actions: RecyclerViewActions<TimeSlotEntity>? = null

    private val viewModel: Step2SelectTimeViewModel by viewModels()
    private val parentViewModel: AddAppointmentViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStep2SelectTimeBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        setupResultListeners()
        observeViewModels()
    }

    private fun setupRecyclerView() {
        // 1. Безопасно получаем binding
        val currentBinding = binding ?: return

        // 2. Создаем адаптер и сохраняем ссылку
        val adapter = TimeSlotAdapter().also { timeSlotAdapter = it }

        currentBinding.rvTimeSlots.adapter = adapter

        // Отключение анимации
        currentBinding.rvTimeSlots.itemAnimator = null

        // RecyclerViewActions теперь работает с TimeSlotEntity
        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvTimeSlots,
            adapter = adapter,
            // Все лямбды теперь получают на вход `TimeSlotEntity`
            getItemId = { entity -> entity.id },
            onEdit = { entity -> showTimePicker(timeSlotToEdit = entity) },
            onDeleteClicked = { timeSlot ->
                viewModel.onDeleteIconClicked(timeSlot)

                DeleteDialogFragment.newInstance(
                    requestKey = AppointmentConstants.Request.DELETE_TIMESLOT,
                    // Используем ТУ ЖЕ логику, что и в адаптере
                    message = getString(com.avetiso.core.R.string.delete_dialog_message, timeSlot.formattedTime)
                ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
            },
            onItemClick = { clickedEntity ->
                parentViewModel.handleEvent(AddAppointmentEvent.TimeSlotClicked(clickedEntity))
            },
            onActionsShown = {
                parentViewModel.handleEvent(AddAppointmentEvent.ClearTimeSlotSelection)
            }
        )
        adapter.actions = actions
    }


    private fun setupClickListeners() {
        binding?.btnAddTimeSlot?.setOnClickListener {
            actions?.dismissActions()
            showTimePicker(timeSlotToEdit = null)
        }
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(AppointmentConstants.Request.TIME_PICKER, viewLifecycleOwner) { _, bundle ->
            val hour = bundle.getInt(AppConstants.Result.RESULT_HOUR)
            val minute = bundle.getInt(AppConstants.Result.RESULT_MINUTE)
            val slotId = bundle.getLong(AppConstants.Result.TIME_RESULT_EXTRA_ID)

            val idToSend = if (slotId == AppConstants.ID_NONE) 0L else slotId

            viewModel.saveTimeSlot(hour, minute, idToSend)
        }

        childFragmentManager.setFragmentResultListener(AppointmentConstants.Request.DELETE_TIMESLOT, viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.onDeleteConfirmed()
            }
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.timeSlots.collect { allSlots ->
                        timeSlotAdapter?.submitList(allSlots)
                        if (actions?.activeItemId != null && allSlots.none { it.id == actions?.activeItemId }) {
                            actions?.dismissActions()
                        }
                    }
                }

                launch {
                    parentViewModel.state.collectLatest { parentState ->
                        val selectedIds = parentState.selectedTimeSlots.map { it.id }.toSet()
                        timeSlotAdapter?.setSelectedItems(selectedIds)
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when(event) {
                            is Step2Event.ShowToast -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showTimePicker(timeSlotToEdit: TimeSlotEntity?) {
        val isEditing = timeSlotToEdit != null
        val title = if (isEditing) "Редактировать слот" else "Добавить слот"

        val initialHour = timeSlotToEdit?.let { it.startTimeMinutes / 60 } ?: 0
        val initialMinute = timeSlotToEdit?.let { it.startTimeMinutes % 60 } ?: 0

        val extraId = timeSlotToEdit?.id ?: AppConstants.ID_NONE

        ComposeTimePickerDialogFragment.newInstance(
            requestKey = AppointmentConstants.Request.TIME_PICKER,
            title = title,
            initialHour = initialHour,
            initialMinute = initialMinute,
            extraId = extraId
        ).show(childFragmentManager, AppointmentConstants.Tags.TIME_PICKER_DIALOG)
    }

    override fun onDestroyView() {
        binding?.rvTimeSlots?.adapter = null
        timeSlotAdapter = null
        actions = null
        binding = null
        super.onDestroyView()
    }
}