package com.avetiso.common_ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.app.AlertDialog
import androidx.core.content.ContextCompat
import com.avetiso.common_ui.R
import com.avetiso.common_ui.databinding.DeleteDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {

    fun showDeleteConfirmationDialog(
        context: Context,
        itemName: String,
        onConfirm: () -> Unit,
    ) {
        // Надуваем макет
        val binding = DeleteDialogBinding.inflate(LayoutInflater.from(context))

        // Устанавливаем текст
        binding.tvMessage.text = context.getString(R.string.delete_dialog_message, itemName)

        // Создаем диалог
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .create()

        // Слушатели
        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnPositive.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
    }

    fun showYesNoDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Да",
        negativeText: String = "Нет",
        onPositiveClicked: () -> Unit,
        onNegativeClicked: () -> Unit = {},
    ) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)

            .setPositiveButton(positiveText) { dialogInterface, _ ->
                onPositiveClicked()
                dialogInterface.dismiss()
            }

            .setNegativeButton(negativeText) { dialogInterface, _ ->
                onNegativeClicked()
                dialogInterface.dismiss()
            }

            .create()

        // 1. Устанавливаем фон (скругленные углы)
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)

        // 2. Настраиваем цвета кнопок при показе (как в SelectCategoryFragment)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setTextColor(ContextCompat.getColor(context, com.avetiso.core.R.color.grey))
            negativeButton.setTextColor(ContextCompat.getColor(context, com.avetiso.core.R.color.grey))
        }

        dialog.show()
    }
}