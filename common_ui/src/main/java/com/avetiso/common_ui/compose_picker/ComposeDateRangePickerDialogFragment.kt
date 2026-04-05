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
import com.avetiso.common_ui.compose_picker.components.DateRangePickerDialogContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeDateRangePickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ThemeComposePicker {
                val title = arguments?.getString(CommonConstants.Args.TITLE).orEmpty()
                val requestKey = arguments?.getString(CommonConstants.Args.REQUEST_KEY) ?: CommonConstants.Request.DATE_RANGE_PICKER

                // Компонент, который мы создадим следующим шагом
                DateRangePickerDialogContent(
                    title = title,
                    onConfirm = { startMillis: Long, endMillis: Long ->
                        // Возвращаем результат строго по изолированным контрактам UI-модуля
                        setFragmentResult(
                            requestKey, bundleOf(
                                CommonConstants.Result.START_DATE to startMillis,
                                CommonConstants.Result.END_DATE to endMillis
                            )
                        )
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

    override fun onStart() {
        super.onStart()
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.90).toInt() // 90% ширины (как в твоем BaseDialogFragment)
        val height = (displayMetrics.heightPixels * 1) // 85% ВЫСОТЫ! Вот она, магия растягивания!

        dialog?.window?.setLayout(width, height)
    }

    companion object {
        fun newInstance(
            requestKey: String,
            title: String
        ): ComposeDateRangePickerDialogFragment {
            return ComposeDateRangePickerDialogFragment().apply {
                arguments = bundleOf(
                    CommonConstants.Args.REQUEST_KEY to requestKey,
                    CommonConstants.Args.TITLE to title
                )
            }
        }
    }
}