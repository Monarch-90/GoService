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

    object About {
        const val ERROR_READING_FILE = "Ошибка при загрузке текста о приложении"
        const val UNKNOWN_ERROR = "Произошла неизвестная ошибка"
        const val BINDING_LIFECYCLE_ERROR = "Обращение к ViewBinding вне жизненного цикла View (после onDestroyView)"
        const val PRIVACY_POLICY_URL = "https://sites.google.com/view/go-service-privacy-policy"
        const val NO_BROWSER_FOUND_ERROR = "На устройстве не установлен браузер для открытия ссылки"
    }
}