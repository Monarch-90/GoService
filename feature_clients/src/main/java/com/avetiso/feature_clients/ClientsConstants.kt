package com.avetiso.feature_clients

internal object ClientsConstants {

    object ViewType {
        const val CLIENT = 0
        const val BANNER_AD = 1
    }

    object Args {
        const val CLIENT_ID = "clientId"
    }

    object Requests {
        const val CLIENT_UPDATED = "client_updated_request"
        const val CLIENT_DELETE = "client_delete_request"
        const val CLIENT_DELETE_FROM_SELECTOR = "client_delete_selector_request"
        const val INPUT_FIELD = "input_field_request"
    }

    object ResultKeys {
        const val IS_CLIENT_UPDATED = "is_client_updated"
    }

    object Pending {
        const val KEY_PENDING_DELETE_ID = "clients_pending_delete_id"
    }
}
