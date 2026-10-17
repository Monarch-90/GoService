
package com.avetiso.core.ads

import android.view.ViewGroup

interface BannerAdProvider {
    val provider: AdsProvider

    suspend fun showBanner(container: ViewGroup)

    fun destroyBanner(container: ViewGroup)
}
