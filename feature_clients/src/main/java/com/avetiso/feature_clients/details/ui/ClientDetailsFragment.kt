package com.avetiso.feature_clients.details.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.databinding.FragmentClientDetailsBinding
import com.avetiso.feature_clients.databinding.ItemClientDetailFieldBinding
import com.avetiso.feature_clients.details.mvi.ClientDetailsEvent
import com.avetiso.feature_clients.details.mvi.ClientDetailsState
import com.avetiso.feature_clients.details.mvi.ClientDetailsViewModel
import com.avetiso.navigation.routers.ClientsNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClientDetailsFragment : Fragment(R.layout.fragment_client_details) {

    private var binding: FragmentClientDetailsBinding? = null
    private val viewModel: ClientDetailsViewModel by viewModels()

    @Inject
    lateinit var clientsNavigator: ClientsNavigator

    // Переменная для таймера бегущей строки
    private var marqueeJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientDetailsBinding.bind(view)

        setupListeners() // КЛИКИ: Исходящие действия (что я нажимаю)
        setupResultListeners() // ОТВЕТЫ: Входящие данные (что мне возвращают другие)
        observeState() // ДАННЫЕ: Подписка на ViewModel
        observeEvents()
    }

    private fun setupListeners() {
        val currentBinding = binding ?: return

        currentBinding.toolbar.setNavigationOnClickListener {
            viewModel.onBackClicked()
        }

        currentBinding.btnEdit.setOnClickListener {
            viewModel.onEditClicked()
        }

        currentBinding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(
            ClientsConstants.Requests.CLIENT_DELETE,
            viewLifecycleOwner
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)
            if (isConfirmed) {
                viewModel.onDeleteClicked()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ClientDetailsEvent.EditClient -> handleEditEvent()
                        is ClientDetailsEvent.NavigateBack -> findNavController().navigateUp()
                        is ClientDetailsEvent.DeleteClient -> handleDeleteEvent()
                    }
                }
            }
        }
    }

    private fun render(state: ClientDetailsState) {
        renderLoading(state.isLoading)

        state.client?.let { client ->
            renderToolbar(client.name)
            renderHeaderInfo(client)
            renderDetailFields(client)
        }
    }

    private fun renderLoading(isLoading: Boolean) {
        binding?.progressBar?.isVisible = isLoading
    }

    private fun renderToolbar(clientName: String) {
        val currentBinding = binding ?: return
        currentBinding.toolbar.title = ""
        currentBinding.tvToolbarTitle.text = clientName
        setupMarqueeEffect()
    }

    private fun renderHeaderInfo(client: ClientEntity) {
        val currentBinding = binding ?: return
        currentBinding.tvPhone.text = client.phoneNumber

        val formattedInstagram = formatInstagram(client.instagram)
        currentBinding.tvInstagram.apply {
            text = formattedInstagram
            isVisible = formattedInstagram.isNotBlank()
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun renderDetailFields(client: ClientEntity) {
        val currentBinding = binding ?: return
        currentBinding.infoContainer.removeAllViews()

        // 1. Скидка
        if (client.discount > 0) {
            addDetailField(
                label = getString(R.string.personal_discount),
                value = "${client.discount}%",
                iconResId = com.avetiso.core.R.drawable.ic_percent
            )
        }

        // 2. Источник привлечения
        if (client.source.isNotBlank()) {
            addDetailField(
                label = getString(R.string.lead_source),
                value = client.source,
                iconResId = com.avetiso.core.R.drawable.ic_label
            )
        }

        // 3. Кастомные поля
        client.customFields.forEach { (label, value) ->
            if (value.isNotBlank()) {
                addDetailField(
                    label = label,
                    value = value,
                    iconResId = com.avetiso.core.R.drawable.ic_info
                )
            }
        }

        // 4. Примечание
        if (client.note.isNotBlank()) {
            addDetailField(
                label = getString(com.avetiso.core.R.string.comment),
                value = client.note,
                iconResId = com.avetiso.core.R.drawable.ic_description
            )
        }
    }

    private fun setupMarqueeEffect() {
        val currentBinding = binding ?: return
        marqueeJob?.cancel()
        currentBinding.tvToolbarTitle.isSelected = false

        marqueeJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(AppConstants.Ui.MARQUEE_START_DELAY)
            if (isActive) {
                currentBinding.tvToolbarTitle.isSelected = true
            }
        }
    }

    private fun formatInstagram(instagram: String): String {
        val prefix = AppConstants.Format.INSTAGRAM_PREFIX
        return when {
            instagram.isBlank() -> ""
            !instagram.startsWith(prefix) -> "$prefix$instagram"
            else -> instagram
        }
    }

    private fun handleEditEvent() {
        viewModel.state.value.client?.id?.let { id ->
            clientsNavigator.navigateToAddEditClient(findNavController(), id)
        }
    }

    private fun handleDeleteEvent() {
        android.widget.Toast.makeText(
            requireContext(),
            getString(R.string.client_deleted),
            android.widget.Toast.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }

    private fun showDeleteConfirmationDialog() {
        val clientName = viewModel.state.value.client?.name ?: ""

        // Используем новый DeleteDialogFragment
        DeleteDialogFragment.newInstance(
            requestKey = ClientsConstants.Requests.CLIENT_DELETE,
            message = getString(com.avetiso.core.R.string.delete_dialog_message, clientName)
        ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
    }

    private fun addDetailField(label: String, value: String, iconResId: Int) {
        val currentBinding = binding ?: return

        val fieldBinding = ItemClientDetailFieldBinding.inflate(
            LayoutInflater.from(requireContext()),
            currentBinding.infoContainer,
            false
        )

        fieldBinding.tvLabel.text = label
        fieldBinding.tvValue.text = value
        fieldBinding.ivIcon.setImageResource(iconResId) // Устанавливаем иконку

        currentBinding.infoContainer.addView(fieldBinding.root)
    }

    override fun onDestroyView() {
        marqueeJob?.cancel()
        marqueeJob = null
        super.onDestroyView()
        binding = null
    }
}