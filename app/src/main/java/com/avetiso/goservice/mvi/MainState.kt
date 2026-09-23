package com.avetiso.goservice.mvi

data class MainState(
    val pendingAdEffects: List<MainAdEffect> = emptyList()
)
