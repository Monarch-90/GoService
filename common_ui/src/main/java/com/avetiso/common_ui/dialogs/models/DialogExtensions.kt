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
        title = getString(R.string.Валюта_по_умолчанию),
        message = getString(R.string.Установить_название, currencyName),
        positiveText = getString(R.string.Да),
        negativeText = getString(R.string.Нет),
        isDestructive = false // По умолчанию обычный стиль (не красный)
    )

    // Используем childFragmentManager, так как диалог привязан к текущему экрану
    ConfirmationDialogFragment.newInstance(payload)
        .show(this.childFragmentManager, CommonConstants.ConfirmationDialogKeys.TAG)
}

fun Fragment.showDeleteCustomCurrencyDialog(
    currencies: Array<String>,
    onCurrencySelected: (String) -> Unit
) {
    val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
        .setTitle(com.avetiso.core.R.string.select_currency_to_delete)
        .setItems(currencies) { _, which ->
            onCurrencySelected(currencies[which])
        }
        // Используем твою кастомную строку из common_ui
        .setNegativeButton(R.string.Отмена_dialog, null)
        .create()

    // Навешиваем твои фирменные скругления в стиле Enterprise
    dialog.setOnShowListener {
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
    }
    dialog.show()
}