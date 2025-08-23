package com.avetiso.feature_schedule.add_appointment.steps.step3.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_clients.adapter.ClientAdapter
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentEvent
import com.avetiso.feature_schedule.add_appointment.mvi.AddAppointmentViewModel
import com.avetiso.feature_schedule.add_appointment.steps.step3.mvi.Step3SelectClientViewModel
import com.avetiso.feature_schedule.databinding.FragmentStep3SelectClientBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Step3SelectClientFragment : Fragment(R.layout.fragment_step3_select_client) {

    private var binding: FragmentStep3SelectClientBinding? = null
    private var clientAdapter: ClientAdapter? = null

    private val viewModel: Step3SelectClientViewModel by viewModels()
    private val parentViewModel: AddAppointmentViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStep3SelectClientBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        clientAdapter = ClientAdapter().apply {
            // В `common_ui` ItemActionTouchListener вызывает onItemClick.
            // Здесь мы "перехватываем" этот клик для выбора клиента.
            val listener = com.avetiso.common_ui.actions.ItemActionTouchListener(
                context = requireContext(),
                recyclerView = binding!!.recyclerViewClients,
                onLongPress = { /* No-op on this screen */ },
                onItemClick = { position ->
                    val client = currentList.getOrNull(position) ?: return@ItemActionTouchListener
                    parentViewModel.handleEvent(AddAppointmentEvent.ClientSelected(client))
                },
                onEmptySpaceClick = { /* No-op */ }
            )
            binding?.recyclerViewClients?.addOnItemTouchListener(listener)
        }
        binding?.recyclerViewClients?.adapter = clientAdapter
    }


    private fun setupListeners() {
        binding?.editTextSearch?.addTextChangedListener {
            viewModel.onSearchQueryChanged(it.toString())
        }
        binding?.fabAddClient?.setOnClickListener {
            // Навигация к общему экрану добавления клиента
            findNavController().navigate(R.id.action_addAppointmentFragment_to_addEditClientFragment)
        }
    }

    private fun observeState() {
        val navController = findNavController()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на список клиентов
                launch {
                    viewModel.clients.collect { clients ->
                        clientAdapter?.submitList(clients)
                    }
                }
                // Подписка на родительский state для подсветки выбранного
                launch {
                    parentViewModel.state.collect { parentState ->
                        clientAdapter?.setSelectedClientId(parentState.selectedClient?.id)
                    }
                }
                // Слушаем результат с экрана добавления/редактирования клиента
                launch {
                    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("client_updated")
                        ?.observe(viewLifecycleOwner) { updated ->
                            if (updated) {
                                // Принудительно обновляем поиск, чтобы перезапросить данные
                                viewModel.onSearchQueryChanged(binding?.editTextSearch?.text.toString())
                                navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("client_updated")
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        clientAdapter = null
    }
}