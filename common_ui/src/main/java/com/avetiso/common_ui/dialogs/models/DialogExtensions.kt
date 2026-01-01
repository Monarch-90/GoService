package com.avetiso.common_ui.dialogs.models

import androidx.fragment.app.Fragment
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.dialogs.ConfirmationDialogFragment
import com.avetiso.core.AppConstants

fun Fragment.showChangeCurrencyDialog(
    currencyName: String,
    requestKey: String
) {
    val payload = ConfirmationDialogPayload(
        requestKey = requestKey,
        title = "Изменение настроек", // Позже вынесем в strings.xml
        message = "Установить $currencyName?",
        positiveText = "Да",
        negativeText = "Нет",
        isDestructive = false // По умолчанию обычный стиль (не красный)
    )

    // Используем childFragmentManager, так как диалог привязан к текущему экрану
    ConfirmationDialogFragment.newInstance(payload)
        .show(this.childFragmentManager, CommonConstants.ConfirmationDialogKeys.TAG)
}