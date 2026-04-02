package com.avetiso.feature_statistics

internal object StatisticsConstants {

    object ViewType {
        const val VIEW_TYPE_PERIOD = 1
        const val VIEW_TYPE_FINANCE = 2
        const val VIEW_TYPE_INVENTORY = 3
        const val VIEW_TYPE_WORKLOAD = 4
        const val VIEW_TYPE_QUICK_ACTIONS = 5
    }

    object Math {
        const val PERCENTAGE_MULTIPLIER = 100L
        const val COUNT = 0
        const val RATIO = 2
        const val LIMIT = 3
    }

    object Time {
        const val MILLIS_IN_DAY = 86400000L
        const val MILLIS_IN_WEEK = 604800000L
        const val MILLIS_IN_MONTH = 2592000000L
        const val CUSTOM_START = 0L
        const val MINUTE = 60

        // Границы начала суток
        const val HOUR_START = 0
        const val MINUTE_START = 0
        const val SECOND_START = 0
        const val MILLISECOND_START = 0

        // Границы конца суток
        const val HOUR_END = 23
        const val MINUTE_END = 59
        const val SECOND_END = 59
        const val MILLISECOND_END = 999

        // Смещение для последних 7 календарных суток (сегодня + 6 предыдущих)
        const val WEEK_OFFSET_DAYS = -6
    }

    object Request {
        const val CUSTOM_PERIOD = "statistics_custom_period_request"
    }
}