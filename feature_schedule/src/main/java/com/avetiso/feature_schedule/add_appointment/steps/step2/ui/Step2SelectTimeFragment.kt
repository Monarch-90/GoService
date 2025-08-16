package com.avetiso.feature_schedule.add_appointment.steps.step2.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.compose_picker.ComposePickerDialogFragment
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step2.adapter.TimeSlotAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step2.mvi.Step2SelectTimeViewModel
import com.avetiso.feature_schedule.databinding.FragmentStep2SelectTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val REQUEST_KEY_ADD = "time_slot_add"
private const val REQUEST_KEY_EDIT = "time_slot_edit"

@AndroidEntryPoint
class Step2SelectTimeFragment : Fragment(R.layout.fragment_step2_select_time) {

    private var binding: FragmentStep2SelectTimeBinding? = null
    private var timeSlotAdapter: TimeSlotAdapter? = null

    private var actions: RecyclerViewActions<TimeSlotEntity>? = null
    private var editingTimeSlotId: Long? = null

    private val viewModel: Step2SelectTimeViewModel by viewModels()
    private val parentViewModel: AddAppointmentViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStep2SelectTimeBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        observeViewModels()
        setupFragmentResultListeners()
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter()
        val currentBinding = binding ?: return
        currentBinding.rvTimeSlots.adapter = timeSlotAdapter
        currentBinding.rvTimeSlots.itemAnimator = null

        // RecyclerViewActions теперь работает с TimeSlotEntity
        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvTimeSlots,
            adapter = timeSlotAdapter!!,
            // Все лямбды теперь получают на вход `TimeSlotEntity`
            getItemId = { entity -> entity.id },
            getItemName = { entity ->
                val hours = entity.startTimeMinutes / 60
                val minutes = entity.startTimeMinutes % 60
                String.format("%02d:%02d", hours, minutes)
            },
            onEdit = { entity -> showTimePicker(timeSlotToEdit = entity) },
            onDelete = { entity -> viewModel.deleteTimeSlot(entity) },
            onItemClick = { clickedEntity ->
                parentViewModel.handleEvent(AddAppointmentEvent.TimeSlotClicked(clickedEntity))
            },
            onActionsShown = {
                parentViewModel.handleEvent(AddAppointmentEvent.ClearTimeSlotSelection)
            }
        )
        timeSlotAdapter?.actions = actions
    }


    private fun setupClickListeners() {
        binding?.btnAddTimeSlot?.setOnClickListener {
            actions?.dismissActions()
            showTimePicker(timeSlotToEdit = null)
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ПОДПИСКА №1: На список временных слотов
                // Обновляем список через submitList
                launch {
                    viewModel.timeSlots.collect { allSlots ->
                        timeSlotAdapter?.submitList(allSlots)

                        // Логика закрытия экшенов, если элемент удален
                        if (actions?.activeItemId != null && allSlots.none { it.id == actions?.activeItemId }) {
                            actions?.dismissActions()
                        }
                    }
                }

                // ПОДПИСКА №2: На состояние родительской ViewModel (для выделения)
                // Обновляем выделение через новый метод setSelectedItems
                launch {
                    parentViewModel.state.collectLatest { parentState ->
                        val selectedIds = parentState.selectedTimeSlots.map { it.id }.toSet()
                        timeSlotAdapter?.setSelectedItems(selectedIds)
                    }
                }
            }
        }
    }

    private fun setupFragmentResultListeners() {
        childFragmentManager.setFragmentResultListener(
            REQUEST_KEY_ADD,
            viewLifecycleOwner
        ) { _, bundle ->
            val hour = bundle.getInt(ComposePickerDialogFragment.RESULT_HOUR)
            val minute = bundle.getInt(ComposePickerDialogFragment.RESULT_MINUTE)
            viewModel.addTimeSlot(hour, minute)
        }
        childFragmentManager.setFragmentResultListener(
            REQUEST_KEY_EDIT,
            viewLifecycleOwner
        ) { _, bundle ->
            val hour = bundle.getInt(ComposePickerDialogFragment.RESULT_HOUR)
            val minute = bundle.getInt(ComposePickerDialogFragment.RESULT_MINUTE)
            editingTimeSlotId?.let { id ->
                viewModel.updateTimeSlot(id, hour, minute)
            }
            editingTimeSlotId = null
        }
    }

    private fun showTimePicker(timeSlotToEdit: TimeSlotEntity?) {
        val isEditing = timeSlotToEdit != null
        val title = if (isEditing) "Редактировать слот" else "Добавить слот времени"
        val resultKey: String
        val initialHour: Int
        val initialMinute: Int

        if (isEditing) {
            editingTimeSlotId = timeSlotToEdit.id
            resultKey = REQUEST_KEY_EDIT
            initialHour = timeSlotToEdit.startTimeMinutes / 60
            initialMinute = timeSlotToEdit.startTimeMinutes % 60
        } else {
            resultKey = REQUEST_KEY_ADD
            initialHour = 0
            initialMinute = 0
        }

        ComposePickerDialogFragment.newInstance(
            title = title,
            resultKey = resultKey,
            initialHour = initialHour,
            initialMinute = initialMinute
        )
            .show(childFragmentManager, "ComposePickerDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.rvTimeSlots?.adapter = null
        timeSlotAdapter = null
        actions = null
        binding = null
    }
}