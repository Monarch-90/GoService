package com.avetiso.feature_sidebar.about.mvi

sealed interface AboutIntent {
    object LoadAboutText : AboutIntent
    object OnBackClicked : AboutIntent
    data object OnPrivacyPolicyClicked : AboutIntent
}