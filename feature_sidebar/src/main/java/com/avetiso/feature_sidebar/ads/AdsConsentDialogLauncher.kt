package com.avetiso.feature_sidebar.ads

import androidx.fragment.app.FragmentActivity
import com.avetiso.common_ui.dialogs.AdsConsentDialogFactory
import com.avetiso.core.AppConstants

/**
 * UI entry point for the initial advertising consent dialog.
 *
 * Keeps the app module independent of common_ui while reusing its
 * existing ConfirmationDialogFragment through the sidebar feature.
 */
object AdsConsentDialogLauncher {

    fun showInitial(activity: FragmentActivity) {
        val fragmentManager = activity.supportFragmentManager
        val dialogTag = AppConstants.AdConstants.Privacy.INITIAL_DIALOG_TAG

        if (fragmentManager.isStateSaved ||
            fragmentManager.findFragmentByTag(dialogTag) != null
        ) {
            return
        }

        AdsConsentDialogFactory.create(
            activity,
            AppConstants.AdConstants.Privacy.INITIAL_RESULT_KEY
        ).apply {
            isCancelable = false
        }.show(fragmentManager, dialogTag)
    }
}
