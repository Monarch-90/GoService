package com.avetiso.feature_sidebar.contact_developers.mvi

sealed interface ContactDevelopersIntent
data class EmailChanged(val email: String) : ContactDevelopersIntent
data class MessageChanged(val message: String) : ContactDevelopersIntent
object SendClicked : ContactDevelopersIntent