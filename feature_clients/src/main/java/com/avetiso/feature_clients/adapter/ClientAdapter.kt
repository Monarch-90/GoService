package com.avetiso.feature_clients.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.databinding.ItemClientBinding

class ClientAdapter : ListAdapter<ClientEntity, ClientAdapter.ClientViewHolder>(DiffCallback) {

    var actions: RecyclerViewActions<ClientEntity>? = null
    private var selectedClient: ClientEntity? = null

    // Метод для обновления выделения, как в первом шаге
    fun updateSelection(client: ClientEntity?) {
        val oldClient = selectedClient
        selectedClient = client

        // Обновляем старый и новый элементы, чтобы перерисовать их состояние
        if (oldClient != null) {
            val oldPosition = currentList.indexOf(oldClient)
            if (oldPosition != -1) notifyItemChanged(oldPosition)
        }
        if (client != null) {
            val newPosition = currentList.indexOf(client)
            if (newPosition != -1) notifyItemChanged(newPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = getItem(position)
        holder.bind(client, client == selectedClient)
        actions?.bindViewHolderActions(holder, client)
    }

    class ClientViewHolder(
        private val binding: ItemClientBinding,
    ) : ActionsViewHolder(binding) {

        // Используем ID из <include>
        override val actionsContainer: View = binding.actionsContainer.root
        override val editButton: View = binding.actionsContainer.btnEdit
        override val deleteButton: View = binding.actionsContainer.btnDelete

        fun bind(client: ClientEntity, isSelected: Boolean) {
            binding.tvClientName.text = client.name
            binding.tvClientPhone.text = client.phoneNumber
            binding.tvInstagram.text = client.instagram

            // Управляем видимостью индикатора выделения
            binding.viewSelectedCheck.isVisible = isSelected

            binding.ivIconDiscount.isVisible = client.discount > 0
        }

        // Полностью повторяем логику из первого шага
        override fun toggleActions(show: Boolean) {
            actionsContainer.isVisible = show
            binding.llClientItemContainer.alpha = if (show) 0.2f else 1.0f

            if (show) {
                binding.viewSelectedCheck.isVisible = false
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ClientEntity>() {
            override fun areItemsTheSame(a: ClientEntity, b: ClientEntity) = a.id == b.id
            override fun areContentsTheSame(a: ClientEntity, b: ClientEntity) = a == b
        }
    }
}