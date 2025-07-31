package com.avetiso.feature_schedule.add_appointment.steps.step1.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.CategoryDao
import com.avetiso.core.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectCategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
) : ViewModel() {

    val categories = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun addOrUpdateCategory(name: String, id: Long? = null) {
        viewModelScope.launch {
            if (id == null) {
                categoryDao.insertCategory(CategoryEntity(name = name))
            } else {
                categoryDao.updateCategory(CategoryEntity(id = id, name = name))
            }
        }
    }
}