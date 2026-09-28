package com.avetiso.feature_clients.selector.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.BannerAdController
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.adapter.ClientAdapter
import com.avetiso.feature_clients.databinding.FragmentClientSelectorBinding
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientsListItem
import com.avetiso.feature_clients.selector.mvi.ClientSelectorViewModel
import com.avetiso.navigation.routers.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClientSelectorFragment : Fragment(R.layout.fragment_client_selector) {

    private var binding: FragmentClientSelectorBinding? = null
    private var clientAdapter: ClientAdapter? = null
    private var actions: RecyclerViewActions<ClientsListItem>? = null
    private val viewModel: ClientSelectorViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    @Inject
    lateinit var bannerAdController: BannerAdController

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientSelectorBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeState()

        setFragmentResultListener(ClientsConstants.Requests.CLIENT_UPDATED) { _, _ ->
            viewModel.onSearchQueryChanged(viewModel.state.value.searchQuery)
        }

        childFragmentManager.setFragmentResultListener(
            ClientsConstants.Requests.CLIENT_DELETE_FROM_SELECTOR,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.onDeleteConfirmed()
            }
        }
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val adapter = ClientAdapter(
            onBannerBound = ::showBanner,
            onBannerRecycled = bannerAdController::destroyBanner
        ).also { clientAdapter = it }

        currentBinding.rvClients.adapter = adapter
        currentBinding.rvClients.itemAnimator = null

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvClients,
            adapter = adapter,
            getItemId = { item ->
                when (item) {
                    is ClientContentItem -> item.client.id
                    is ClientBannerAdItem -> item
                }
            },
            onEdit = { item ->
                if (item is ClientContentItem) {
                    clientsNavigator.navigateToAddEditClient(
                        findNavController(),
                        item.client.id
                    )
                }
            },
            onDeleteClicked = { item ->
                if (item is ClientContentItem) {
                    viewModel.onDeleteIconClicked(item.client)
                    DeleteDialogFragment.newInstance(
                        requestKey = ClientsConstants.Requests.CLIENT_DELETE_FROM_SELECTOR,
                        message = getString(
                            com.avetiso.core.R.string.delete_dialog_message,
                            item.client.name
                        )
                    ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
                }
            },
            onItemClick = { item ->
                if (item is ClientContentItem) {
                    viewModel.onClientSelected(item.client)
                }
            },
            onActionsShown = {
                viewModel.clearClientSelection()
                setFragmentResult(
                    AppConstants.Requests.CLIENT_SELECT,
                    bundleOf(AppConstants.Result.SELECTED_CLIENT to null)
                )
            },
            canShowActions = { item -> item is ClientContentItem }
        )

        adapter.actions = actions
    }

    private fun showBanner(container: ViewGroup) {
        viewLifecycleOwner.lifecycleScope.launch {
            bannerAdController.showBanner(container)
        }
    }

    private fun setupListeners() {
        binding?.etSearch?.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable?.toString().orEmpty())
        }

        binding?.btnAddClient?.setOnClickListener {
            clientsNavigator.navigateToAddEditClient(findNavController(), null)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state
                        .map { state -> state.items }
                        .distinctUntilChanged()
                        .collect { items ->
                            clientAdapter?.submitList(items)
                        }
                }

                launch {
                    viewModel.state
                        .map { state -> state.selectedClient }
                        .distinctUntilChanged()
                        .collect { selectedClient ->
                            clientAdapter?.updateSelection(selectedClient)
                            setFragmentResult(
                                AppConstants.Requests.CLIENT_SELECT,
                                bundleOf(AppConstants.Result.SELECTED_CLIENT to selectedClient)
                            )
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
