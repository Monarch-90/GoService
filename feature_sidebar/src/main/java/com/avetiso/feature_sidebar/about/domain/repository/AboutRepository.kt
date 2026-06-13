package com.avetiso.feature_sidebar.about.domain.repository

import com.avetiso.feature_sidebar.about.domain.model.AboutTextModel

interface AboutRepository {
    suspend fun getAboutText(): Result<AboutTextModel>
}