package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.ui

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
import androidx.fragment.app.setFragmentResult
import com.avetiso.feature_schedule.utils.MyPickerAppTheme
import com.chargemap.compose.numberpicker.NumberPicker

class DurationPickerDialogFragment : DialogFragment() {

    // --- ШАГ 1: Фрагмент теперь "владеет" состоянием ---
    private var selectedHour by mutableIntStateOf(0)
    private var selectedMinute by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализируем состояние из аргументов при создании фрагмента
        selectedHour = arguments?.getInt("hour") ?: 0
        selectedMinute = arguments?.getInt("minute") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyPickerAppTheme {
                    // --- ШАГ 2: Передаем состояние и лямбды для его обновления в Composable ---
                    DurationPickerDialog(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onHourChange = { selectedHour = it },
                        onMinuteChange = { selectedMinute = it },
                        onConfirm = { dismiss() }, // Кнопка "ОК" теперь просто закрывает диалог
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Делаем фон системного окна прозрачным, чтобы Compose мог рисовать свой фон
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // --- ШАГ 3: Отправляем результат ПРИ ЛЮБОМ ЗАКРЫТИИ ---
        // Этот метод вызывается всегда, и у нас есть доступ к последнему состоянию.
        setFragmentResult(
            "duration_selection",
            bundleOf("hour" to selectedHour, "minute" to selectedMinute)
        )
    }
}

/**
 * Эта Composable-функция теперь "глупая" (stateless).
 * Она не хранит состояние, а только отображает его и сообщает об изменениях наверх.
 */
@Composable
private fun DurationPickerDialog(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val minuteDisplayValues = (0..55 step 5).map { String.format("%02d", it) }
    // Индекс для пикера минут вычисляется на лету
    val minuteIndex = minute / 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите продолжительность") },
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
                    onValueChange = onHourChange, // Сообщаем наверх об изменении часа
                    range = 0..23,
                    label = { "$it ч" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
                NumberPicker(
                    value = minuteIndex,
                    onValueChange = { newIndex ->
                        onMinuteChange(newIndex * 5) // Сообщаем наверх об изменении минут
                    },
                    range = 0 until minuteDisplayValues.size,
                    label = { minuteDisplayValues[it] + " мин" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { // Просто вызываем onConfirm
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