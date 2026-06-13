package com.avetiso.feature_sidebar.about.domain.usecase

import com.avetiso.feature_sidebar.about.domain.model.AboutTextModel
import com.avetiso.feature_sidebar.about.domain.repository.AboutRepository
import javax.inject.Inject

class GetAboutTextUseCase @Inject constructor(
    private val repository: AboutRepository
) {
    suspend operator fun invoke(): Result<AboutTextModel> {
        return repository.getAboutText()
    }
}