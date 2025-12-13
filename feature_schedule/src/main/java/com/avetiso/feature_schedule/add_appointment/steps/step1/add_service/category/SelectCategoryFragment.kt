package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.databinding.FragmentSelectCategoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "TouchDebug"

@AndroidEntryPoint
class SelectCategoryFragment : Fragment(R.layout.fragment_select_category) {

    private var binding: FragmentSelectCategoryBinding? = null
    private val viewModel: SelectCategoryViewModel by viewModels()
    private var actions: RecyclerViewActions<CategoryEntity>? = null
    private var categoryAdapter: CategoryAdapter? = null

    // Запоминаем категорию, которую редактируем (null, если создаем новую)
    private var pendingCategory: CategoryEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoryBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        setupResultListeners()
        observeViewModel()
    }

    // Обработка результата ввода
    private fun setupResultListeners() {
        childFragmentManager.setFragmentResultListener(INPUT_CATEGORY_KEY, viewLifecycleOwner) { _, bundle ->
            val text = bundle.getString(InputDialogFragment.RESULT_TEXT) ?: return@setFragmentResultListener
            handleCategoryInput(text)
        }
    }

    private fun handleCategoryInput(name: String) {
        val currentCategories = viewModel.categories.value
        val isEditMode = pendingCategory != null

        // Проверка на дубликаты
        val hasDuplicate = currentCategories.any {
            // Если редактируем - исключаем саму себя из проверки
            (if (isEditMode) it.id != pendingCategory?.id else true) &&
                    it.name.equals(name, ignoreCase = true)
        }

        if (hasDuplicate) {
            Toast.makeText(requireContext(), "Такая категория уже существует", Toast.LENGTH_SHORT).show()
            // Опционально: можно тут же снова открыть диалог с введенным текстом, но Toast обычно достаточно
        } else {
            viewModel.addOrUpdateCategory(name, pendingCategory?.id)
        }

        // Сброс
        pendingCategory = null
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val adapter = CategoryAdapter().also { categoryAdapter = it }

        currentBinding.rvCategories.adapter = adapter
        currentBinding.rvCategories.layoutManager = LinearLayoutManager(requireContext())

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvCategories,
            adapter = adapter,
            getItemId = { category -> category.id },
            getItemName = { category -> category.name },
            onEdit = { category ->
                showCategoryInputDialog(category)
            },
            onDelete = { category ->
                viewModel.deleteCategory(category)
            },
            onItemClick = { category ->
                setFragmentResult(
                    "category_selection",
                    bundleOf("selected_category_name" to category.name)
                )
                findNavController().navigateUp()
            }
        )

        // Просто присваиваем actions в адаптер.
        adapter.actions = actions
    }

    private fun setupClickListeners() {
        val currentBinding = binding ?: return

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
        currentBinding.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }
        currentBinding.fabAddCategory.setOnClickListener {
            if (actions?.activeItemId != null) {
                actions?.dismissActions()
            } else {
                showCategoryInputDialog()
            }
        }
    }

    private fun handleBackPress() {
        if (actions?.activeItemId != null) {
            actions?.dismissActions()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                (binding?.rvCategories?.adapter as? CategoryAdapter)?.submitList(
                    categories
                )
            }
        }
    }

    private fun setupSearch() {
        binding?.etSearch?.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable.toString())
        }
    }

    private fun showCategoryInputDialog(category: CategoryEntity? = null) {
        pendingCategory = category // Запоминаем для последующей обработки
        val isEditMode = category != null

        val title = if (isEditMode) "Редактировать категорию" else "Новая категория"
        val initialValue = category?.name ?: ""

        InputDialogFragment.newInstance(
            requestKey = INPUT_CATEGORY_KEY,
            title = title,
            hint = "Название категории",
            initialValue = initialValue
        ).show(childFragmentManager, InputDialogFragment.TAG)
    }

    companion object {
        private const val INPUT_CATEGORY_KEY = "input_category_request"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        categoryAdapter = null
        actions = null
    }
}