package com.avetiso.goservice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.yandex.mobile.ads.common.YandexAds

@HiltAndroidApp
class GoServiceHilt : Application() {
    override fun onCreate() {
        super.onCreate()

        // Инициализация Yandex Mobile Ads SDK
        YandexAds.initialize(this) {
            // В будущем здесь можно добавить Timber для логирования старта
        }
    }
}