package com.avetiso.core.ads

import android.app.Activity

interface AppOpenAdController {

    suspend fun loadAppOpenAd()

    suspend fun showAppOpenAd(
        activity: Activity
    ): AppOpenAdShowResult
}
