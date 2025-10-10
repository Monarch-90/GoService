package com.avetiso.feature_clients.selector

import androidx.fragment.app.Fragment
import com.avetiso.feature_clients.selector.ui.ClientSelectorFragment
import com.avetiso.navigation.ClientSelectorProvider
import javax.inject.Inject

/**
 * Конкретная реализация контракта.
 * Находится внутри модуля и знает, какой именно фрагмент нужно вернуть.
 */
class ClientSelectorProviderImpl @Inject constructor() : ClientSelectorProvider {
    override fun getClientSelectorFragment(): Fragment {
        // Возвращаем наш фрагмент, который теперь живет в этом же модуле
        return ClientSelectorFragment()
    }
}