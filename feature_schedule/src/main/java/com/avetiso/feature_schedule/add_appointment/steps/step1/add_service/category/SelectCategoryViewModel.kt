package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.CategoryDao
import com.avetiso.core.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectCategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val categories = _searchQuery
        .debounce(300L) // Ждем 300 мс после ввода, чтобы не делать лишних запросов
        .flatMapLatest { query ->
            if (query.isBlank()) {
                categoryDao.getAllCategories() // Если поиск пуст, показываем всё
            } else {
                categoryDao.searchCategories(query) // Иначе - ищем
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

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