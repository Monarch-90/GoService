package com.avetiso.common_ui.compose_picker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.compose_picker.components.DateRangePickerDialogContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeDateRangePickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ThemeComposePicker {
                val title = arguments?.getString(CommonConstants.Args.TITLE).orEmpty()
                val requestKey = arguments?.getString(CommonConstants.Args.REQUEST_KEY)
                    ?: CommonConstants.Request.DATE_RANGE_PICKER

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(ComposePickerLayoutConstants.DIALOG_EDGE_MARGIN),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = ComposePickerLayoutConstants.DATE_RANGE_MAX_WIDTH)
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        DateRangePickerDialogContent(
                            title = title,
                            onConfirm = { startMillis, endMillis ->
                                setFragmentResult(
                                    requestKey,
                                    bundleOf(
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
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            WindowCompat.setDecorFitsSystemWindows(this, false)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
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
