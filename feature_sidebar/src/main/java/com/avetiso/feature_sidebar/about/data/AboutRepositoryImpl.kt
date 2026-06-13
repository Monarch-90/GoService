package com.avetiso.feature_sidebar.about.data

import android.content.Context
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.about.domain.model.AboutTextModel
import com.avetiso.feature_sidebar.about.domain.repository.AboutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class AboutRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AboutRepository {

    override suspend fun getAboutText(): Result<AboutTextModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.resources.openRawResource(R.raw.about)
            val text = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
            Result.success(AboutTextModel(content = text))
        } catch (e: Exception) {
            Result.failure(Exception(SidebarConstants.About.ERROR_READING_FILE, e))
        }
    }
}