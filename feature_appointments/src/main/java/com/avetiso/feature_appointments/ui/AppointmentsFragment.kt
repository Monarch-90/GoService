package com.avetiso.feature_appointments.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import com.avetiso.feature_appointments.AppointmentsConstants
import com.avetiso.feature_appointments.R
import com.avetiso.feature_appointments.adapter.AppointmentsAdapter
import com.avetiso.feature_appointments.databinding.FragmentAppointmentsBinding
import com.avetiso.feature_appointments.model.AppointmentsListItem
import com.avetiso.feature_appointments.mvi.AppointmentsEvent
import com.avetiso.feature_appointments.mvi.AppointmentsIntent
import com.avetiso.feature_appointments.mvi.AppointmentsViewModel
import com.avetiso.navigation.routers.ScheduleNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentsFragment : Fragment(R.layout.fragment_appointments) {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    private val viewModel: AppointmentsViewModel by viewModels()

    @Inject
    lateinit var scheduleNavigator: ScheduleNavigator

    private var appointmentsAdapter: AppointmentsAdapter? = null
    private var actions: RecyclerViewActions<AppointmentsListItem>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("AppTrace", "AppointmentsFragment: onViewCreated STARTED")
        _binding = FragmentAppointmentsBinding.bind(view)

        setupRecyclerView()
        setupResultListeners()
        observeViewModel()
        setupSearch()

        android.util.Log.d("AppTrace", "AppointmentsFragment: onViewCreated FINISHED")
    }

    private fun setupRecyclerView() {
        val adapter = AppointmentsAdapter(
            onStatusClicked = { appointment ->
                showAppointmentStatusDialog(
                    appointment = appointment,
                    rescheduleRequestKey = AppointmentsConstants.Requests.RESCHEDULE_DATE_KEY,
                    onStatusSelected = { status ->
                        viewModel.processIntent(AppointmentsIntent.ChangeStatus(appointment.id, status))
                    }
                )
            },
            onNoteClicked = { appointment ->
                viewModel.processIntent(AppointmentsIntent.OnNoteClicked(appointment.id, appointment.note))
            }
        ).also { appointmentsAdapter = it }

        binding.rvAppointments.adapter = adapter
        binding.rvAppointments.itemAnimator = null

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding.rvAppointments,
            adapter = adapter,
            getItemId = { item ->
                when (item) {
                    // ID для самой записи
                    is AppointmentsListItem.AppointmentItem -> item.appointment.id
                    // Для заголовка ID будет сама дата (она уникальна в рамках списка)
                    is AppointmentsListItem.DateHeader -> item.date
                }
            },
            onEdit = { item ->
                if (item is AppointmentsListItem.AppointmentItem) {
                    viewModel.processIntent(AppointmentsIntent.OnEditClicked(item.appointment.id))
                }
            },
            onDeleteClicked = { item ->
                if (item is AppointmentsListItem.AppointmentItem) {
                    viewModel.processIntent(AppointmentsIntent.OnDeleteClicked(item.appointment.id))
                }
            },
            onItemClick = { },
            triggerMode = TriggerMode.SWIPE_REVEAL
        )
        adapter.actions = actions
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            AppointmentsConstants.Requests.INPUT_NOTE_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(AppConstants.Result.RESULT_TEXT) ?: ""
            viewModel.processIntent(AppointmentsIntent.SaveNote(text))
        }

        childFragmentManager.setFragmentResultListener(
            AppointmentsConstants.Requests.APPOINTMENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.processIntent(AppointmentsIntent.ConfirmDelete)
            }
        }

        childFragmentManager.setFragmentResultListener(
            AppointmentsConstants.Requests.RESCHEDULE_DATE_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedMillis = bundle.getLong(AppConstants.Result.RESULT_DATE)
            val appointmentId = bundle.getLong(AppConstants.Result.DATE_RESULT_EXTRA_ID)
            if (appointmentId != AppConstants.ID_NONE) {
                val sdf = SimpleDateFormat(AppConstants.Format.FULL_DATE_FORMAT, Locale.getDefault())
                val newDate = sdf.format(Date(selectedMillis))
                viewModel.processIntent(
                    AppointmentsIntent.ChangeStatus(appointmentId, com.avetiso.core.model.AppointmentStatus.RESCHEDULED, newDate)
                )
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->

                        android.util.Log.d("AppTrace", "AppointmentsFragment: State collected, items size = ${state.items.size}")
                        appointmentsAdapter?.submitList(state.items)
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""
            viewModel.processIntent(AppointmentsIntent.UpdateSearchQuery(query))
        }
    }

    private fun handleEvent(event: AppointmentsEvent) {
        when (event) {
            is AppointmentsEvent.ShowDeleteDialog -> {
                showAppointmentDeleteDialog(AppointmentsConstants.Requests.APPOINTMENT_DELETE)
            }

            is AppointmentsEvent.ShowNoteDialog -> {
                showAppointmentNoteDialog(event.currentNote, AppointmentsConstants.Requests.INPUT_NOTE_KEY)
            }

            is AppointmentsEvent.NavigateToEdit -> {
                scheduleNavigator.navigateToAddEditAppointment(findNavController(), event.appointmentId, null)
            }

            is AppointmentsEvent.ShowToast -> {
                Toast.makeText(requireContext(), event.messageResId, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        binding.rvAppointments.adapter = null
        appointmentsAdapter = null
        actions = null
        _binding = null
        super.onDestroyView()
    }
}