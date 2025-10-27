package com.avetiso.feature_clients.selector.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

        setFragmentResultListener("client_updated_request") { _, _ ->
            viewModel.onSearchQueryChanged(viewModel.state.value.searchQuery)
        }
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val adapter = ClientAdapter().also { clientAdapter = it }
        currentBinding.rvClients.adapter = adapter

        // Отключает анимацию на андроид 15
        currentBinding.rvClients.itemAnimator = null

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvClients,
            adapter = adapter,
            getItemId = { client -> client.id },
            getItemName = { client -> client.name },
            onEdit = { client ->
                // Навигация на экран редактирования
                clientsNavigator.navigateToAddEditClient(findNavController(), client.id)
            },
            onDelete = { client ->
                viewModel.deleteClient(client)
            },
            onItemClick = { client ->
                viewModel.onClientSelected(client)
            },
            onActionsShown = {
                viewModel.clearClientSelection()
                setFragmentResult("client_selection_request", bundleOf("selected_client" to null))
            }
        )
        // Передаем actions в адаптер
        adapter.actions = actions
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
                launch {
                    viewModel.state
                        .map { it.selectedClient }
                        .distinctUntilChanged()
                        .collect { selectedClient ->
                            // 1. Обновляем UI (подсветка)
                            clientAdapter?.updateSelection(selectedClient) // Безопасный вызов
                            // 2. Отправляем результат родителю
                            setFragmentResult("client_selection_request", bundleOf("selected_client" to selectedClient))
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding?.rvClients?.adapter = null
        binding = null
        clientAdapter = null
        actions = null
        super.onDestroyView()
    }
}