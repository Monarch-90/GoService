package com.avetiso.core.usecase

import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AppLaunchCounterRepository
import javax.inject.Inject

class ShouldShowAppOpenAdUseCase @Inject constructor(
    private val appLaunchCounterRepository: AppLaunchCounterRepository
) {

    suspend operator fun invoke(): Boolean {
        val launchCount =
            appLaunchCounterRepository.incrementAndGetLaunchCount()

        return launchCount %
                AppConstants.AdConstants.APP_OPEN_AD_FREQUENCY == 0
    }
}