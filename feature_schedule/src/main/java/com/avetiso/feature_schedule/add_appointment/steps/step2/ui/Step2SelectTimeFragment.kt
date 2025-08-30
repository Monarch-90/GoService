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
import com.avetiso.common_ui.compose_picker.ComposePickerDialogFragment
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step2.adapter.TimeSlotAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step2.mvi.Step2Event
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
        val title = if (isEditing) "Редактировать слот" else "Добавить слот времени"

        val initialHour = timeSlotToEdit?.let { it.startTimeMinutes / 60 } ?: 0
        val initialMinute = timeSlotToEdit?.let { it.startTimeMinutes % 60 } ?: 0

        val dialog = ComposePickerDialogFragment.newInstance(
            title = title,
            initialHour = initialHour,
            initialMinute = initialMinute
        )

        dialog.onConfirm = { hour, minute ->
            val totalMinutes = hour * 60 + minute

            // Определяем, существует ли уже такой слот
            val isDuplicate = if (isEditing) {
                // При редактировании ищем дубликат, исключая сам редактируемый слот
                viewModel.timeSlots.value.any { it.startTimeMinutes == totalMinutes && it.id != timeSlotToEdit!!.id }
            } else {
                // При добавлении ищем любой слот с таким же временем
                viewModel.timeSlots.value.any { it.startTimeMinutes == totalMinutes }
            }

            if (isDuplicate) {
                // Если дубликат найден, показываем Toast и возвращаем false
                Toast.makeText(requireContext(), "Такой слот уже существует", Toast.LENGTH_SHORT).show()
                false // <-- Говорим пикеру не закрываться
            } else {
                // Если дубликата нет, вызываем метод ViewModel
                if (isEditing) {
                    viewModel.updateTimeSlot(timeSlotToEdit!!.id, hour, minute)
                } else {
                    viewModel.addTimeSlot(hour, minute)
                }
                // и возвращаем true
                true // <-- Говорим пикеру, что можно закрыться
            }
        }

        dialog.show(childFragmentManager, "ComposePickerDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.rvTimeSlots?.adapter = null
        timeSlotAdapter = null
        actions = null
        binding = null
    }
}