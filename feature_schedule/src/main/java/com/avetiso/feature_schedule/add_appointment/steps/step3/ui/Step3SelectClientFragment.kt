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
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.ClientEntity
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
    private var actions: RecyclerViewActions<ClientEntity>? = null

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
        clientAdapter = ClientAdapter()
        binding?.rvClients?.adapter = clientAdapter

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding!!.rvClients,
            adapter = clientAdapter!!,
            getItemId = { client -> client.id },
            getItemName = { client -> client.name },
            onEdit = { client ->
                // Навигация на экран редактирования
                val direction = R.id.action_addAppointmentFragment_to_addEditClientFragment
                val args = Bundle().apply { putParcelable("clientToEdit", client) }
                findNavController().navigate(direction, args)
            },
            onDelete = { client ->
                viewModel.deleteClient(client)
            },
            onItemClick = { client ->
                parentViewModel.handleEvent(AddAppointmentEvent.ClientSelected(client))
            },
            onActionsShown = {
                parentViewModel.handleEvent(AddAppointmentEvent.ClearClientSelection)
            }
        )
        // Передаем actions в адаптер
        clientAdapter?.actions = actions
    }

    private fun setupListeners() {
        binding?.etSearch?.addTextChangedListener {
            viewModel.onSearchQueryChanged(it.toString())
        }
        binding?.btnAddClient?.setOnClickListener {
            findNavController().navigate(R.id.action_addAppointmentFragment_to_addEditClientFragment)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на список клиентов из локальной VM
                launch {
                    viewModel.clients.collect { clients ->
                        clientAdapter?.submitList(clients)
                    }
                }
                // Подписка на родительский state для подсветки выбранного
                launch {
                    parentViewModel.state.collect { parentState ->
                        clientAdapter?.updateSelection(parentState.selectedClient)
                    }
                }
                // Слушаем результат с экрана добавления/редактирования
                launch {
                    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("client_updated")
                        ?.observe(viewLifecycleOwner) { updated ->
                            if (updated) {
                                viewModel.onSearchQueryChanged(binding?.etSearch?.text.toString())
                                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("client_updated")
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
        actions = null
    }
}