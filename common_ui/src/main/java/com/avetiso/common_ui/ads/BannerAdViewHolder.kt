package com.avetiso.common_ui.ads

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BannerAdViewHolder(
    private val container: ViewGroup,
    private val onBannerBound: (ViewGroup) -> Unit,
    private val onBannerRecycled: (ViewGroup) -> Unit
) : RecyclerView.ViewHolder(container) {

    fun bind() {
        onBannerBound(container)
    }

    fun recycle() {
        onBannerRecycled(container)
    }
}
