package com.avetiso.feature_clients.selector.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.adapter.ClientAdapter
import com.avetiso.feature_clients.databinding.FragmentClientSelectorBinding
import com.avetiso.feature_clients.selector.mvi.ClientSelectorViewModel
import com.avetiso.navigation.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ClientSelectorFragment : Fragment(R.layout.fragment_client_selector) {

    private var binding: FragmentClientSelectorBinding? = null
    private var clientAdapter: ClientAdapter? = null
    private var actions: RecyclerViewActions<ClientEntity>? = null
    private val viewModel: ClientSelectorViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientSelectorBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        clientAdapter = ClientAdapter()
        binding?.rvClients?.adapter = clientAdapter

        // Отключает анимацию на андроид 15
        binding?.rvClients?.itemAnimator = null

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = binding!!.rvClients,
            adapter = clientAdapter!!,
            getItemId = { client -> client.id },
            getItemName = { client -> client.name },
            onEdit = { client ->
                // Навигация на экран редактирования
                clientsNavigator.navigateToAddEditClient(findNavController(), client)
            },
            onDelete = { client ->
                viewModel.deleteClient(client)
            },
            onItemClick = { client ->
                // Обновляем состояние в локальном ViewModel
                viewModel.onClientSelected(client)

                // Отправляем результат обратно родительскому фрагменту
                setFragmentResult("client_selection_request", bundleOf("selected_client" to client))
                findNavController().navigateUp() // И закрываем себя
            },
            onActionsShown = {
                // При показе действий сбрасываем выбор в локальном ViewModel
                val currentState = viewModel.state.value
                viewModel.onClientSelected(currentState.selectedClient ?: return@RecyclerViewActions)
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
            clientsNavigator.navigateToAddEditClient(findNavController(), null)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Подписка на список клиентов
                launch {
                    viewModel.clients.collect { clients ->
                        clientAdapter?.submitList(clients)
                    }
                }
                // Подписка на состояние выбора для подсветки
                launch {
                    viewModel.state.collect { state ->
                        clientAdapter?.updateSelection(state.selectedClient)
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