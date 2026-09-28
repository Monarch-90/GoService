package com.avetiso.feature_appointments.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
import com.avetiso.common_ui.compose_picker.managers.PickerResultManager
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.BannerAdController
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.feature_appointments.AppointmentsConstants
import com.avetiso.feature_appointments.R
import com.avetiso.feature_appointments.adapter.AppointmentsAdapter
import com.avetiso.feature_appointments.databinding.FragmentAppointmentsBinding
import com.avetiso.feature_appointments.model.AppointmentContentItem
import com.avetiso.feature_appointments.model.AppointmentDateHeaderItem
import com.avetiso.feature_appointments.model.AppointmentBannerAdItem
import com.avetiso.feature_appointments.model.AppointmentsListItem
import com.avetiso.feature_appointments.mvi.AppointmentsEvent
import com.avetiso.feature_appointments.mvi.AppointmentsViewModel
import com.avetiso.feature_appointments.mvi.ChangeAppointmentStatusIntent
import com.avetiso.feature_appointments.mvi.ConfirmAppointmentDeleteIntent
import com.avetiso.feature_appointments.mvi.DeleteAppointmentClickedIntent
import com.avetiso.feature_appointments.mvi.EditAppointmentIntent
import com.avetiso.feature_appointments.mvi.NavigateToEditAppointmentEvent
import com.avetiso.feature_appointments.mvi.OpenAppointmentNoteIntent
import com.avetiso.feature_appointments.mvi.SaveAppointmentNoteIntent
import com.avetiso.feature_appointments.mvi.ShowAppointmentDeleteDialogEvent
import com.avetiso.feature_appointments.mvi.ShowAppointmentNoteDialogEvent
import com.avetiso.feature_appointments.mvi.ShowAppointmentsToastEvent
import com.avetiso.feature_appointments.mvi.UpdateAppointmentsSearchQueryIntent
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

    @Inject
    lateinit var bannerAdController: BannerAdController

    private var appointmentsAdapter: AppointmentsAdapter? = null
    private var actions: RecyclerViewActions<AppointmentsListItem>? = null
    private var pickerResultManager: PickerResultManager? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAppointmentsBinding.bind(view)
        pickerResultManager = PickerResultManager(this)

        setupRecyclerView()
        setupResultListeners()
        observeViewModel()
        setupSearch()
    }

    private fun setupRecyclerView() {
        val adapter = AppointmentsAdapter(
            onBannerBound = ::showBanner,
            onBannerRecycled = bannerAdController::destroyBanner,
            onStatusClicked = { appointment ->
                showAppointmentStatusDialog(
                    appointment = appointment,
                    rescheduleRequestKey = AppointmentsConstants.Requests.RESCHEDULE_DATE_KEY,
                    onStatusSelected = { status ->
                        viewModel.processIntent(
                            ChangeAppointmentStatusIntent(
                                appointmentId = appointment.id,
                                status = status
                            )
                        )
                    }
                )
            },
            onNoteClicked = { appointment ->
                viewModel.processIntent(
                    OpenAppointmentNoteIntent(
                        appointmentId = appointment.id,
                        currentNote = appointment.note
                    )
                )
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
                    is AppointmentContentItem -> item.appointment.id
                    is AppointmentDateHeaderItem -> item.date
                    is AppointmentBannerAdItem -> item
                }
            },
            onEdit = { item ->
                if (item is AppointmentContentItem) {
                    viewModel.processIntent(
                        EditAppointmentIntent(item.appointment.id)
                    )
                }
            },
            onDeleteClicked = { item ->
                if (item is AppointmentContentItem) {
                    viewModel.processIntent(
                        DeleteAppointmentClickedIntent(item.appointment.id)
                    )
                }
            },
            onItemClick = { },
            canShowActions = { item -> item is AppointmentContentItem },
            triggerMode = TriggerMode.SWIPE_REVEAL
        )

        adapter.actions = actions
    }

    private fun showBanner(container: ViewGroup) {
        viewLifecycleOwner.lifecycleScope.launch {
            bannerAdController.showBanner(container)
        }
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            AppointmentsConstants.Requests.INPUT_NOTE_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(AppConstants.Result.RESULT_TEXT).orEmpty()
            viewModel.processIntent(SaveAppointmentNoteIntent(text))
        }

        childFragmentManager.setFragmentResultListener(
            AppointmentsConstants.Requests.APPOINTMENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.processIntent(ConfirmAppointmentDeleteIntent)
            }
        }

        pickerResultManager?.setupDatePickerListener(
            requestKey = AppointmentsConstants.Requests.RESCHEDULE_DATE_KEY
        ) { selectedMillis, appointmentId ->
            if (
                selectedMillis != 0L &&
                appointmentId != AppConstants.ID_NONE
            ) {
                val sdf = SimpleDateFormat(
                    AppConstants.Format.FULL_DATE_FORMAT,
                    Locale.getDefault()
                )
                val newDate = sdf.format(Date(selectedMillis))

                viewModel.processIntent(
                    ChangeAppointmentStatusIntent(
                        appointmentId = appointmentId,
                        status = AppointmentStatus.RESCHEDULED,
                        newDate = newDate
                    )
                )
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
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
            viewModel.processIntent(
                UpdateAppointmentsSearchQueryIntent(
                    editable?.toString().orEmpty()
                )
            )
        }
    }

    private fun handleEvent(event: AppointmentsEvent) {
        when (event) {
            ShowAppointmentDeleteDialogEvent -> {
                showAppointmentDeleteDialog(
                    AppointmentsConstants.Requests.APPOINTMENT_DELETE
                )
            }

            is ShowAppointmentNoteDialogEvent -> {
                showAppointmentNoteDialog(
                    currentNote = event.currentNote,
                    requestKey = AppointmentsConstants.Requests.INPUT_NOTE_KEY
                )
            }

            is NavigateToEditAppointmentEvent -> {
                scheduleNavigator.navigateToAddEditAppointment(
                    findNavController(),
                    event.appointmentId,
                    null
                )
            }

            is ShowAppointmentsToastEvent -> {
                Toast.makeText(
                    requireContext(),
                    event.messageResId,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        binding.rvAppointments.adapter = null
        appointmentsAdapter = null
        actions = null
        pickerResultManager = null
        _binding = null
        super.onDestroyView()
    }
}
