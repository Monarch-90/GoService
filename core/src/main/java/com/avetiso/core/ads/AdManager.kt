package com.avetiso.core.ads

import android.app.Activity
import android.view.ViewGroup

interface AdManager {
    fun loadAppOpenAd()
    fun showAppOpenAd(activity: Activity, onAdClosed: () -> Unit)

    fun showBanner(container: ViewGroup)

    fun loadRewardedAd()
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit)
}