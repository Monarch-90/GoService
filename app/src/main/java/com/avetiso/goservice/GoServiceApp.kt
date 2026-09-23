package com.avetiso.goservice

import android.app.Application
import com.avetiso.core.ads.AdsSdkInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GoServiceHilt : Application() {

    @Inject
    lateinit var adsSdkInitializers: Set<@JvmSuppressWildcards AdsSdkInitializer>

    override fun onCreate() {
        super.onCreate()

        adsSdkInitializers.forEach { initializer ->
            initializer.initialize()
        }
    }
}
