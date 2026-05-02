package com.avetiso.feature_sidebar

internal object SidebarConstants {

    object Validation {
        // Регулярное выражение для проверки email
        const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    }

    object Api {
        // Заменить YOUR_FORM_ID_HERE на ID из Formspree
        const val FEEDBACK_ENDPOINT = "https://formspree.io/f/xnjwnpew"
        const val FORMSPREE_BASE_URL = "https://formspree.io/"

        const val FIELD_EMAIL = "Email:"
        const val FIELD_MESSAGE = "Message:"

        const val ACCEPT = "Accept: application/json"
    }
}