package com.avetiso.common_ui.compose_picker.managers

import androidx.fragment.app.Fragment
import com.avetiso.common_ui.CommonConstants

/**
 * Менеджер для инкапсуляции работы с Fragment Result API.
 * Избавляет вызывающие фрагменты от прямого взаимодействия с Bundle и строковыми ключами.
 */
class PickerResultManager(
    private val fragment: Fragment
) {

    fun setupTimePickerListener(
        requestKey: String = CommonConstants.Request.TIME_PICKER,
        onResult: (hour: Int, minute: Int, extraId: Long) -> Unit
    ) {
        fragment.childFragmentManager.setFragmentResultListener(
            requestKey,
            fragment.viewLifecycleOwner
        ) { _, bundle ->
            val hour = bundle.getInt(CommonConstants.Result.HOUR)
            val minute = bundle.getInt(CommonConstants.Result.MINUTE)
            val extraId = bundle.getLong(
                CommonConstants.Result.TIME_EXTRA_ID,
                CommonConstants.Args.NO_ID
            )
            onResult(hour, minute, extraId)
        }
    }

    fun setupDatePickerListener(
        requestKey: String = CommonConstants.Request.DATE_PICKER,
        onResult: (dateMillis: Long, extraId: Long) -> Unit
    ) {
        fragment.childFragmentManager.setFragmentResultListener(
            requestKey,
            fragment.viewLifecycleOwner
        ) { _, bundle ->
            val dateMillis = bundle.getLong(CommonConstants.Result.DATE)
            val extraId = bundle.getLong(
                CommonConstants.Result.DATE_EXTRA_ID,
                CommonConstants.Args.NO_ID
            )
            onResult(dateMillis, extraId)
        }
    }

    fun setupDateRangePickerListener(
        requestKey: String = CommonConstants.Request.DATE_RANGE_PICKER,
        onResult: (startMillis: Long, endMillis: Long) -> Unit
    ) {
        fragment.childFragmentManager.setFragmentResultListener(
            requestKey,
            fragment.viewLifecycleOwner
        ) { _, bundle ->
            val startMillis = bundle.getLong(CommonConstants.Result.START_DATE)
            val endMillis = bundle.getLong(CommonConstants.Result.END_DATE)

            // Защита от потенциальных багов Compose DateRangePicker
            if (startMillis != 0L && endMillis != 0L) {
                onResult(startMillis, endMillis)
            }
        }
    }
}