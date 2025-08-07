package com.avetiso.feature_schedule.add_appointment.steps.step1

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.avetiso.feature_schedule.utils.MyPickerAppTheme
import com.chargemap.compose.numberpicker.NumberPicker

class DurationPickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val initialHour = arguments?.getInt("hour") ?: 0
                val initialMinute = arguments?.getInt("minute") ?: 0

                MyPickerAppTheme {
                    DurationPickerDialog(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                        onTimeSelected = { hour, minute ->
                            setFragmentResult(
                                "duration_selection",
                                bundleOf("hour" to hour, "minute" to minute)
                            )
                            dismiss()
                        },
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }

    // ✅ ШАГ 1: Делаем фон окна диалога прозрачным
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }
}

@Composable
private fun DurationPickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinuteIndex by remember { mutableIntStateOf(initialMinute / 5) }

    val minuteDisplayValues = (0..55 step 5).map { String.format("%02d", it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите продолжительность") },
        containerColor = MaterialTheme.colorScheme.surface, // Цвет фона диалога из темы
        titleContentColor = MaterialTheme.colorScheme.primary, // Цвет заголовка из темы
        textContentColor = MaterialTheme.colorScheme.primary, // Цвет текста из темы
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
                    dividersColor = MaterialTheme.colorScheme.secondary, // Цвет разделителей
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.primary) // Цвет текста
                )
                NumberPicker(
                    value = selectedMinuteIndex,
                    onValueChange = { selectedMinuteIndex = it },
                    range = 0 until minuteDisplayValues.size,
                    label = { minuteDisplayValues[it] + " мин" },
                    dividersColor = MaterialTheme.colorScheme.secondary, // Цвет разделителей
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.primary) // Цвет текста
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(selectedHour, selectedMinuteIndex * 5)
            }) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}