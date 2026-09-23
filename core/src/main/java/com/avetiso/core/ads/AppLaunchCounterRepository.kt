package com.avetiso.core.ads

interface AppLaunchCounterRepository {

    suspend fun incrementAndGetLaunchCount(): Int
}