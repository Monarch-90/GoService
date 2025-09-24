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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.chargemap.compose.numberpicker.NumberPicker

class ComposeTimePickerDialogFragment : DialogFragment() {

    private var selectedHour by mutableIntStateOf(0)
    private var selectedMinute by mutableIntStateOf(0)

    private val titleText: String by lazy {
        requireArguments().getString(ARG_TITLE, "")
    }

    var onConfirm: ((hour: Int, minute: Int) -> Boolean)? = null
    var onDismissListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedHour = requireArguments().getInt(ARG_INITIAL_HOUR)
        selectedMinute = requireArguments().getInt(ARG_INITIAL_MINUTE)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Вызываем наш слушатель, если он был установлен
        onDismissListener?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ThemeComposePicker {
                PickerDialogContent(
                    title = titleText,
                    hour = selectedHour,
                    minute = selectedMinute,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it },
                    onConfirm = {
                        // Вызываем внешний колбэк для валидации
                        val isSuccess = onConfirm?.invoke(selectedHour, selectedMinute) ?: true

                        // Закрываем диалог ТОЛЬКО в случае успеха
                        if (isSuccess) {
                            dismiss()
                        }
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

        fun newInstance(
            title: String,
            initialHour: Int,
            initialMinute: Int,
        ): ComposeTimePickerDialogFragment {
            return ComposeTimePickerDialogFragment().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_INITIAL_HOUR to initialHour,
                    ARG_INITIAL_MINUTE to initialMinute
                )
            }
        }
    }
}

@Composable
private fun PickerDialogContent(
    title: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val minuteDisplayValues = (0..55 step 5).map { String.format("%02d", it) }
    val minuteIndex = minute / 5

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
                    value = hour,
                    onValueChange = onHourChange,
                    range = 0..23,
                    label = { "$it ч" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
                NumberPicker(
                    value = minuteIndex,
                    onValueChange = { newIndex -> onMinuteChange(newIndex * 5) },
                    range = 0 until minuteDisplayValues.size,
                    label = { minuteDisplayValues[it] + " мин" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("ОК") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}