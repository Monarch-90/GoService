package com.avetiso.feature_schedule.add_appointment.steps.step1.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.ui.AddAppointmentFragmentDirections
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step1.adapter.AvailableServiceAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.Step1Event
import com.avetiso.feature_schedule.add_appointment.steps.step1.mvi.Step1SelectServiceViewModel
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
    private var actions: RecyclerViewActions<ServiceEntity>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStep1SelectServiceBinding.bind(view)

        setupRecyclerView()
        setupSearch()

        binding?.buttonAddService?.setOnClickListener {
            parentViewModel.handleEvent(AddAppointmentEvent.NavigateToAddService)
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("service_updated")
            ?.observe(viewLifecycleOwner) { updated ->
                if (updated) {
                    // Принудительно обновляем поиск, чтобы перезапросить данные
                    viewModel.handleEvent(Step1Event.SearchQueryChanged(binding?.editTextSearch?.text.toString()))
                    // Сбрасываем флаг, чтобы не было повторных обновлений
                    savedStateHandle.remove<Boolean>("service_updated")
                }
            }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на локальный ViewModel (для списка услуг из поиска)
                launch {
                    viewModel.state.collect { state ->
                        serviceAdapter?.submitList(state.availableServices)
                    }
                }

                // ПОДПИСКА НА РОДИТЕЛЯ:
                // Получаем от родителя инфо о том, какие услуги выбраны
                launch {
                    parentViewModel.state.collect { parentState ->
                        serviceAdapter?.updateSelection(parentState.selectedServices)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        // Создаем адаптер без лямбды
        serviceAdapter = AvailableServiceAdapter()
        binding?.recyclerViewSelectedServices?.adapter = serviceAdapter

        // Инициализируем наш механизм действий
        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding!!.recyclerViewSelectedServices,
            adapter = serviceAdapter!!,
            getItemId = { service -> service.id },
            getItemName = { service -> service.name },
            onEdit = { service ->
                // Ваша логика перехода на экран редактирования
                val direction =
                    AddAppointmentFragmentDirections.actionAddAppointmentFragmentToAddServiceFragment(
                        service
                    )
                // Используем основной NavController родительского фрагмента для навигации
                requireParentFragment().findNavController().navigate(direction)
            },
            onDelete = { service ->
                // Вызываем новый метод в ViewModel для удаления
                viewModel.deleteService(service)
            },
            onItemClick = { service ->
                // Здесь сохраняется ваша логика выбора услуги
                val isCurrentlySelected =
                    parentViewModel.state.value.selectedServices.contains(service)
                parentViewModel.handleEvent(
                    AddAppointmentEvent.ServiceSelected(
                        service,
                        !isCurrentlySelected
                    )
                )
            },
            onActionsShown = {
                parentViewModel.handleEvent(AddAppointmentEvent.ClearSelection)
            }
        )
        // Передаем actions в адаптер
        serviceAdapter?.actions = actions
    }

    private fun setupSearch() {
        binding?.editTextSearch?.addTextChangedListener { editable ->
            val query = editable.toString()
            viewModel.handleEvent(Step1Event.SearchQueryChanged(query))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        serviceAdapter = null
        actions = null
    }
}