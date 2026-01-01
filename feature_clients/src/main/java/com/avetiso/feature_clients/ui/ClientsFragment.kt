package com.avetiso.feature_clients.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.adapter.ClientAdapter
import com.avetiso.feature_clients.databinding.FragmentClientsBinding
import com.avetiso.feature_clients.mvi.ClientsViewModel
import com.avetiso.navigation.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClientsFragment : Fragment(R.layout.fragment_clients) {

    private var binding: FragmentClientsBinding? = null
    private val viewModel: ClientsViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    private var clientAdapter: ClientAdapter? = null
    private var actions: RecyclerViewActions<ClientEntity>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientsBinding.bind(view)

        // ✅ Слушаем ответ от диалога здесь, во фрагменте
        childFragmentManager.setFragmentResultListener(
            ClientsConstants.Requests.CLIENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.onDeleteConfirmed()
            }
        }

        setupRecyclerView()
        setupListeners()
        observeState()

        setFragmentResultListener(ClientsConstants.Requests.CLIENT_UPDATED) { _, _ ->
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
            getItemId = { it.id },
            onItemClick = { client ->
                val action = ClientsFragmentDirections.actionClientsFragmentToClientDetailsFragment(client.id)
                findNavController().navigate(action)
            },
            onEdit = { client ->
                // Используем навигатор для перехода на экран редактирования, передавая ID
                clientsNavigator.navigateToAddEditClient(findNavController(), client.id)
            },
            onDeleteClicked = { client ->
                // 1. Запоминаем в VM
                viewModel.onDeleteIconClicked(client)
                // 2. Показываем диалог
                DeleteDialogFragment.newInstance(
                    requestKey = ClientsConstants.Requests.CLIENT_DELETE,
                    message = getString(com.avetiso.core.R.string.delete_dialog_message, client.name)
                ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
            }
        )
        adapter.actions = actions
    }

    private fun setupListeners() {
        // Кнопка "Добавить клиента"
        binding?.btnAddClient?.setOnClickListener {
            clientsNavigator.navigateToAddEditClient(findNavController(), null)
        }

        // Поле поиска
        binding?.etSearch?.addTextChangedListener {
            viewModel.onSearchQueryChanged(it.toString())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    clientAdapter?.submitList(state.clients)
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