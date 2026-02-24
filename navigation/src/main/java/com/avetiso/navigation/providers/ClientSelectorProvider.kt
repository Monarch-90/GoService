package com.avetiso.navigation.providers

import androidx.fragment.app.Fragment

/**
 * Этот интерфейс - контракт.
 * Он абстрактно описывает возможность получить экран для выбора клиента.
 */
interface ClientSelectorProvider {
    fun getClientSelectorFragment(): Fragment
}