package com.avetiso.feature_clients.adapter

import androidx.recyclerview.widget.DiffUtil
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientsListItem

object ClientsFeedListItemDiffCallback : DiffUtil.ItemCallback<ClientsListItem>() {

    override fun areItemsTheSame(
        oldItem: ClientsListItem,
        newItem: ClientsListItem
    ): Boolean {
        return when {
            oldItem is ClientContentItem && newItem is ClientContentItem -> {
                oldItem.client.id == newItem.client.id
            }

            oldItem is ClientBannerAdItem && newItem is ClientBannerAdItem -> true

            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: ClientsListItem,
        newItem: ClientsListItem
    ): Boolean {
        return oldItem == newItem
    }
}
