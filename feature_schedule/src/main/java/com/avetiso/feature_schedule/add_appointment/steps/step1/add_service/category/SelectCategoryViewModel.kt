package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
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
    private var pendingCategory: CategoryEntity? = null
    private var categoryPendingDelete: CategoryEntity? = null

    val categories = _searchQuery
        .debounce(AppConstants.Time.SEARCH_DEBOUNCE) // Ждем 300 мс после ввода, чтобы не делать лишних запросов
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

    // 1. Вызывается при нажатии на кнопку удаления
    fun onDeleteIconClicked(category: CategoryEntity) {
        categoryPendingDelete = category
    }

    // 2. Вызывается, когда пользователь подтвердил удаление в диалоге
    fun onDeleteConfirmed() {
        val category = categoryPendingDelete ?: return
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
        categoryPendingDelete = null
    }

    // 1. Фрагмент сообщает: "Пользователь хочет создать новую категорию"
    fun onAddCategoryClicked() {
        pendingCategory = null
    }

    // 2. Фрагмент сообщает: "Пользователь хочет редактировать эту категорию"
    fun onEditCategoryClicked(category: CategoryEntity) {
        pendingCategory = category
    }

    // 3. Фрагмент сообщает: "Диалог вернул это название"
    fun onCategoryNameInput(name: String) {
        // Логика проверки на дубликаты должна быть здесь или в UseCase,
        // но для простоты оставим проверку в UI слое (или перенесем сюда полностью).
        // В рамках текущего рефакторинга мы фокусируемся на сохранении состояния.

        viewModelScope.launch {
            val categoryToEdit = pendingCategory

            if (categoryToEdit == null) {
                createCategory(name)
            } else {
                updateCategory(categoryToEdit.id, name)
            }

            // Сбрасываем состояние после успешной операции
            pendingCategory = null
        }
    }

    // Вызывается UI для проверки на дубликат ПЕРЕД закрытием/сохранением
    fun isDuplicate(name: String): Boolean {
        val currentList = categories.value
        val idToExclude = pendingCategory?.id // null, если создание

        return currentList.any { category ->
            // Проверяем совпадение имен, исключая саму себя (при редактировании)
            category.id != idToExclude && category.name.equals(name, ignoreCase = true)
        }
    }

    private suspend fun createCategory(name: String) {
        categoryDao.insertCategory(CategoryEntity(name = name))
    }

    private suspend fun updateCategory(id: Long, name: String) {
        categoryDao.updateCategory(CategoryEntity(id = id, name = name))
    }
}