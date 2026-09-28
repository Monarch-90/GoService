package com.avetiso.feature_clients.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
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
import com.avetiso.feature_clients.adapter.ClientsFeedAdapter
import com.avetiso.feature_clients.databinding.FragmentClientsBinding
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientsListItem
import com.avetiso.feature_clients.mvi.AddClientClickedIntent
import com.avetiso.feature_clients.mvi.ClientsEvent
import com.avetiso.feature_clients.mvi.ClientsViewModel
import com.avetiso.feature_clients.mvi.ConfirmClientDeleteIntent
import com.avetiso.feature_clients.mvi.DeleteClientClickedIntent
import com.avetiso.feature_clients.mvi.EditClientIntent
import com.avetiso.feature_clients.mvi.NavigateToAddEditClientEvent
import com.avetiso.feature_clients.mvi.NavigateToClientDetailsEvent
import com.avetiso.feature_clients.mvi.OpenClientIntent
import com.avetiso.feature_clients.mvi.ShowClientDeleteDialogEvent
import com.avetiso.feature_clients.mvi.UpdateClientsSearchQueryIntent
import com.avetiso.navigation.routers.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClientsFragment : Fragment(R.layout.fragment_clients) {

    private var binding: FragmentClientsBinding? = null
    private val viewModel: ClientsViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    @Inject
    lateinit var bannerAdController: BannerAdController

    private var clientAdapter: ClientsFeedAdapter? = null
    private var actions: RecyclerViewActions<ClientsListItem>? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientsBinding.bind(view)

        setupResultListeners()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            ClientsConstants.Requests.CLIENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.processIntent(ConfirmClientDeleteIntent)
            }
        }
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val adapter = ClientsFeedAdapter(
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
            onItemClick = { item ->
                if (item is ClientContentItem) {
                    viewModel.processIntent(OpenClientIntent(item.client.id))
                }
            },
            onEdit = { item ->
                if (item is ClientContentItem) {
                    viewModel.processIntent(EditClientIntent(item.client.id))
                }
            },
            onDeleteClicked = { item ->
                if (item is ClientContentItem) {
                    viewModel.processIntent(
                        DeleteClientClickedIntent(
                            clientId = item.client.id,
                            clientName = item.client.name
                        )
                    )
                }
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
        binding?.btnAddClient?.setOnClickListener {
            viewModel.processIntent(AddClientClickedIntent)
        }

        binding?.etSearch?.addTextChangedListener { editable ->
            viewModel.processIntent(
                UpdateClientsSearchQueryIntent(
                    query = editable?.toString().orEmpty()
                )
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        clientAdapter?.submitList(state.items)
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

    private fun handleEvent(event: ClientsEvent) {
        when (event) {
            is NavigateToClientDetailsEvent -> {
                val action = ClientsFragmentDirections
                    .actionClientsFragmentToClientDetailsFragment(event.clientId)
                findNavController().navigate(action)
            }

            is NavigateToAddEditClientEvent -> {
                clientsNavigator.navigateToAddEditClient(
                    findNavController(),
                    event.clientId
                )
            }

            is ShowClientDeleteDialogEvent -> {
                DeleteDialogFragment.newInstance(
                    requestKey = ClientsConstants.Requests.CLIENT_DELETE,
                    message = getString(
                        com.avetiso.core.R.string.delete_dialog_message,
                        event.clientName
                    )
                ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
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
