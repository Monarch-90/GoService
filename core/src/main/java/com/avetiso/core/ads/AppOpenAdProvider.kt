
package com.avetiso.core.ads

import android.app.Activity

interface AppOpenAdProvider {
    val provider: AdsProvider

    suspend fun loadAppOpenAd()

    suspend fun showAppOpenAd(
        activity: Activity
    ): AppOpenAdShowResult
}
