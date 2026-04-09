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
import com.avetiso.common_ui.compose_picker.components.TimePickerDialogContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeTimePickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ThemeComposePicker {
                val initialHour = arguments?.getInt(CommonConstants.Args.INITIAL_HOUR) ?: 0
                val initialMinute = arguments?.getInt(CommonConstants.Args.INITIAL_MINUTE) ?: 0
                val title = arguments?.getString(CommonConstants.Args.TITLE).orEmpty()
                val requestKey = arguments?.getString(CommonConstants.Args.REQUEST_KEY) ?: CommonConstants.Request.TIME_PICKER
                val extraId = arguments?.getLong(CommonConstants.Args.EXTRA_ID) ?: CommonConstants.Args.NO_ID

                TimePickerDialogContent(
                    title = title,
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    onConfirm = { hour, minute ->
                        // Полностью изолированный от core-модуля возврат результата
                        setFragmentResult(requestKey, bundleOf(
                            CommonConstants.Result.HOUR to hour,
                            CommonConstants.Result.MINUTE to minute,
                            CommonConstants.Result.TIME_EXTRA_ID to extraId
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
            initialHour: Int,
            initialMinute: Int,
            extraId: Long = CommonConstants.Args.NO_ID
        ): ComposeTimePickerDialogFragment {
            return ComposeTimePickerDialogFragment().apply {
                arguments = bundleOf(
                    CommonConstants.Args.REQUEST_KEY to requestKey,
                    CommonConstants.Args.TITLE to title,
                    CommonConstants.Args.INITIAL_HOUR to initialHour,
                    CommonConstants.Args.INITIAL_MINUTE to initialMinute,
                    CommonConstants.Args.EXTRA_ID to extraId
                )
            }
        }
    }
}