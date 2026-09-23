package com.avetiso.core.ads

import android.view.ViewGroup

interface BannerAdController {

    suspend fun showBanner(container: ViewGroup)
}
