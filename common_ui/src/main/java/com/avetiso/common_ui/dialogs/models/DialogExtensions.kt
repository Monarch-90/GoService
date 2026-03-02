package com.avetiso.common_ui.dialogs.models

import androidx.fragment.app.Fragment
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.R
import com.avetiso.common_ui.dialogs.ConfirmationDialogFragment

fun Fragment.showChangeCurrencyDialog(
    currencyName: String,
    requestKey: String
) {
    val payload = ConfirmationDialogPayload(
        requestKey = requestKey,
        title = getString(R.string.Изменение_настроек),
        message = getString(R.string.Установить_название, currencyName),
        positiveText = getString(R.string.Да),
        negativeText = getString(R.string.Нет),
        isDestructive = false // По умолчанию обычный стиль (не красный)
    )

    // Используем childFragmentManager, так как диалог привязан к текущему экрану
    ConfirmationDialogFragment.newInstance(payload)
        .show(this.childFragmentManager, CommonConstants.ConfirmationDialogKeys.TAG)
}