package com.avetiso.common_ui.utils

import android.content.Context
import android.view.LayoutInflater
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

        // Фон (убедись, что этот ресурс доступен, или используй R.drawable.dialog_box_corners из common_ui, если он там)
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
    }
}