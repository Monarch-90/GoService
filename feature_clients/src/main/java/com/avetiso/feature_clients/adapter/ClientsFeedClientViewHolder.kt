package com.avetiso.feature_clients.adapter

import android.view.View
import androidx.core.view.isVisible
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.databinding.ItemClientBinding

class ClientsFeedClientViewHolder(
    private val binding: ItemClientBinding
) : ActionsViewHolder(binding) {

    override val actionsContainer: View = binding.actionsContainer.root
    override val editButton: View = binding.actionsContainer.btnEdit
    override val deleteButton: View = binding.actionsContainer.btnDelete

    fun bind(client: ClientEntity) {
        binding.tvClientName.text = client.name
        binding.viewSelectedCheck.isVisible = false
        binding.ivIconDiscount.isVisible = client.discount > 0
    }

    override fun toggleActions(show: Boolean) {
        actionsContainer.isVisible = show
        binding.llClientItemContainer.alpha = if (show) {
            AppConstants.Ui.ALPHA_DIMMED
        } else {
            AppConstants.Ui.ALPHA_OPAQUE
        }

        if (show) {
            binding.viewSelectedCheck.isVisible = false
        }
    }
}
