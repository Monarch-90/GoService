package com.avetiso.common_ui.compose_picker

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.chargemap.compose.numberpicker.NumberPicker

class ComposeTimePickerDialogFragment : DialogFragment() {

    private var selectedHour by mutableIntStateOf(0)
    private var selectedMinute by mutableIntStateOf(0)

    private val titleText: String by lazy {
        requireArguments().getString(ARG_TITLE, "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedHour = requireArguments().getInt(ARG_INITIAL_HOUR)
        selectedMinute = requireArguments().getInt(ARG_INITIAL_MINUTE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ThemeComposePicker {
                val initialHour = requireArguments().getInt(ARG_INITIAL_HOUR)
                val initialMinute = requireArguments().getInt(ARG_INITIAL_MINUTE)
                val title = requireArguments().getString(ARG_TITLE, "")
                val requestKey = requireArguments().getString(ARG_REQUEST_KEY) ?: "time_picker_result"
                val extraId = requireArguments().getLong(ARG_EXTRA_ID, -1L)

                TimePickerDialogContent(
                    title = title,
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    onConfirm = { hour, minute ->
                        // Возвращаем результат через FragmentManager (переживает поворот)
                        setFragmentResult(requestKey, bundleOf(
                            RESULT_HOUR to hour,
                            RESULT_MINUTE to minute,
                            RESULT_EXTRA_ID to extraId
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
        private const val ARG_TITLE = "arg_title"
        private const val ARG_INITIAL_HOUR = "arg_initial_hour"
        private const val ARG_INITIAL_MINUTE = "arg_initial_minute"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_EXTRA_ID = "arg_extra_id"

        const val RESULT_HOUR = "result_hour"
        const val RESULT_MINUTE = "result_minute"
        const val RESULT_EXTRA_ID = "result_extra_id"

        fun newInstance(
            requestKey: String,
            title: String,
            initialHour: Int,
            initialMinute: Int,
            extraId: Long = -1L
        ): ComposeTimePickerDialogFragment {
            return ComposeTimePickerDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_TITLE to title,
                    ARG_INITIAL_HOUR to initialHour,
                    ARG_INITIAL_MINUTE to initialMinute,
                    ARG_EXTRA_ID to extraId
                )
            }
        }
    }
}

@Composable
private fun TimePickerDialogContent(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {

    var selectedHour by rememberSaveable { mutableIntStateOf(initialHour) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(initialMinute) }
    val minuteDisplayValues = (0..55 step 5).map { String.format("%02d", it) }
    val minuteIndex = selectedMinute / 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                NumberPicker(
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                    range = 0..23,
                    label = { "$it ч" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
                NumberPicker(
                    value = minuteIndex,
                    onValueChange = { newIndex -> selectedMinute = newIndex * 5 },
                    range = 0 until minuteDisplayValues.size,
                    label = { minuteDisplayValues[it] + " мин" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) { Text("ОК") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}