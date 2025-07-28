package com.avetiso.feature_schedule.add_appointment.steps.step1

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.Step1Event
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.Step1SelectServiceViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step1.adapter.AvailableServiceAdapter
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.databinding.FragmentStep1SelectServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Step1SelectServiceFragment : Fragment(R.layout.fragment_step1_select_service) {

    private var binding: FragmentStep1SelectServiceBinding? = null

    // ViewModel для этого экрана
    private val viewModel: Step1SelectServiceViewModel by viewModels()

    // Общий ViewModel для всего процесса, чтобы управлять главной кнопкой "Далее"
    private val parentViewModel: AddAppointmentViewModel by viewModels({ requireParentFragment() })

    private var serviceAdapter: AvailableServiceAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStep1SelectServiceBinding.bind(view)

        setupRecyclerView()

        binding?.buttonAddService?.setOnClickListener {
            parentViewModel.handleEvent(AddAppointmentEvent.NavigateToAddService)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Обновляем список в адаптере
                    serviceAdapter?.submitList(state.availableServices)

                    // Сообщаем родительскому ViewModel, можно ли нажимать "Далее"
                    val isStepComplete = state.selectedServices.isNotEmpty()
                    parentViewModel.handleEvent(AddAppointmentEvent.StepDataChanged(isStepComplete))
                }
            }
        }
    }

    private fun setupRecyclerView() {
        serviceAdapter = AvailableServiceAdapter { service, isSelected ->
            viewModel.handleEvent(Step1Event.ServiceSelected(service, isSelected))
        }
        binding?.recyclerViewSelectedServices?.adapter = serviceAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        serviceAdapter = null
    }
}