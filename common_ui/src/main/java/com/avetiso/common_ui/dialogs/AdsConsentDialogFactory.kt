package com.avetiso.common_ui.dialogs

import android.content.Context
import com.avetiso.common_ui.dialogs.models.ConfirmationDialogPayload
import com.avetiso.core.AppConstants
import com.avetiso.core.R

object AdsConsentDialogFactory {
    fun create(context: Context, requestKey: String): ConfirmationDialogFragment {
        val payload = ConfirmationDialogPayload(
            requestKey = requestKey,
            title = context.getString(R.string.ads_consent_title),
            message = context.getString(R.string.ads_consent_description),
            positiveText = context.getString(R.string.ads_consent_accept),
            negativeText = context.getString(R.string.ads_consent_decline),
            infoText = context.getString(R.string.ads_consent_privacy_link),
            infoUrl = AppConstants.AdConstants.Privacy.POLICY_URL
        )
        return ConfirmationDialogFragment.newInstance(payload)
    }
}
