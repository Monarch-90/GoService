package com.avetiso.core.ads

interface AdsSdkReadiness {

    suspend fun awaitReady(): Boolean
}
