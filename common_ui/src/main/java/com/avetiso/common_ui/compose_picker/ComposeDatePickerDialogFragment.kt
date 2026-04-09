package com.avetiso.common_ui.compose_picker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.compose_picker.components.DatePickerDialogContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeDatePickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ThemeComposePicker {
                val title = arguments?.getString(CommonConstants.Args.TITLE).orEmpty()
                val initialDateMillis = arguments?.getLong(CommonConstants.Args.INITIAL_DATE) ?: System.currentTimeMillis()
                val requestKey = arguments?.getString(CommonConstants.Args.REQUEST_KEY) ?: CommonConstants.Request.DATE_PICKER
                val extraId = arguments?.getLong(CommonConstants.Args.EXTRA_ID) ?: CommonConstants.Args.NO_ID

                // Вызываем компонент, который мы создадим следующим шагом
                DatePickerDialogContent(
                    title = title,
                    initialDateMillis = initialDateMillis,
                    onConfirm = { selectedMillis: Long ->
                        // Изолированный возврат результата через наши собственные константы
                        setFragmentResult(requestKey, bundleOf(
                            CommonConstants.Result.DATE to selectedMillis,
                            CommonConstants.Result.DATE_EXTRA_ID to extraId
                        ))
                        dismiss()
                    },
                    onDismiss = { dismiss() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    companion object {
        fun newInstance(
            requestKey: String,
            title: String,
            initialDateMillis: Long? = null,
            extraId: Long = CommonConstants.Args.NO_ID
        ): ComposeDatePickerDialogFragment {
            return ComposeDatePickerDialogFragment().apply {
                arguments = bundleOf(
                    CommonConstants.Args.REQUEST_KEY to requestKey,
                    CommonConstants.Args.TITLE to title,
                    CommonConstants.Args.INITIAL_DATE to (initialDateMillis ?: System.currentTimeMillis()),
                    CommonConstants.Args.EXTRA_ID to extraId
                )
            }
        }
    }
}