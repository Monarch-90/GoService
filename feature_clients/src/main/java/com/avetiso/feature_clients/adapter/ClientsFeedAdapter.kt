package com.avetiso.feature_clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.ads.BannerAdViewHolder
import com.avetiso.common_ui.databinding.ItemClientBannerAdHostBinding
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.databinding.ItemClientBinding
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientsListItem

class ClientsFeedAdapter(
    private val onBannerBound: (ViewGroup) -> Unit,
    private val onBannerRecycled: (ViewGroup) -> Unit
) : ListAdapter<ClientsListItem, RecyclerView.ViewHolder>(ClientsFeedListItemDiffCallback) {

    var actions: RecyclerViewActions<ClientsListItem>? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ClientContentItem -> ClientsConstants.ViewType.CLIENT
            is ClientBannerAdItem -> ClientsConstants.ViewType.BANNER_AD
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            ClientsConstants.ViewType.CLIENT -> {
                ClientsFeedClientViewHolder(
                    ItemClientBinding.inflate(inflater, parent, false)
                )
            }

            ClientsConstants.ViewType.BANNER_AD -> {
                BannerAdViewHolder(
                    container = ItemClientBannerAdHostBinding
                        .inflate(inflater, parent, false)
                        .root,
                    onBannerBound = onBannerBound,
                    onBannerRecycled = onBannerRecycled
                )
            }

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is ClientContentItem -> {
                val clientHolder = holder as ClientsFeedClientViewHolder
                clientHolder.bind(item.client)
                actions?.bindViewHolderActions(clientHolder, item)
            }

            is ClientBannerAdItem -> {
                (holder as BannerAdViewHolder).bind()
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is BannerAdViewHolder) {
            holder.recycle()
        }
        super.onViewRecycled(holder)
    }
}
