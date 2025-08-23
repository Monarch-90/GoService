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
    private var selectedClientId: Long? = null

    // Эта функция будет вызываться из Step3 для подсветки
    fun setSelectedClientId(id: Long?) {
        val oldId = selectedClientId
        selectedClientId = id
        // Обновляем старый и новый элементы для перерисовки
        oldId?.let { notifyItemChanged(currentList.indexOfFirst { c -> c.id == it }) }
        id?.let { notifyItemChanged(currentList.indexOfFirst { c -> c.id == it }) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = getItem(position)
        // Для подсветки в режиме выбора (step 3)
        val isSelectedForAppointment = client.id == selectedClientId
        holder.bind(client, isSelectedForAppointment)
        actions?.bindViewHolderActions(holder, client)
    }

    class ClientViewHolder(
        private val binding: ItemClientBinding,
    ) : ActionsViewHolder(binding) {

        override val actionsContainer: View = binding.actionsContainer.root
        override val editButton: View = binding.actionsContainer.btnEdit
        override val deleteButton: View = binding.actionsContainer.btnDelete

        fun bind(client: ClientEntity, isSelected: Boolean) {
            binding.textClientName.text = client.name
            binding.textClientPhone.text = client.phoneNumber
            binding.root.isSelected = isSelected // Используем isSelected для state в drawable
        }

        override fun toggleActions(show: Boolean) {
            actionsContainer.isVisible = show
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ClientEntity>() {
            override fun areItemsTheSame(a: ClientEntity, b: ClientEntity) = a.id == b.id
            override fun areContentsTheSame(a: ClientEntity, b: ClientEntity) = a == b
        }
    }
}