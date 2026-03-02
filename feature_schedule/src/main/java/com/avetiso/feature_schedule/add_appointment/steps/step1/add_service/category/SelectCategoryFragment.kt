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
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.add_appointment.AppointmentConstants
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
        childFragmentManager.setFragmentResultListener(
            AppointmentConstants.Request.INPUT_CATEGORY,
            viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(AppConstants.Result.RESULT_TEXT) ?: return@setFragmentResultListener
            handleCategoryInput(text)
        }

        childFragmentManager.setFragmentResultListener(
            AppointmentConstants.Request.DELETE_CATEGORY,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(AppConstants.Result.DELETE_CONFIRMED)) {
                viewModel.onDeleteConfirmed()
            }
        }
    }

    private fun handleCategoryInput(name: String) {
        // Проверка на дубликаты
        val hasDuplicate = viewModel.isDuplicate(name)

        if (hasDuplicate) {
            Toast.makeText(requireContext(), R.string.Такая_категория_уже_существует, Toast.LENGTH_SHORT).show()
        } else {
            // Просто передаем данные. ViewModel сама знает, редактируем мы или создаем.
            viewModel.onCategoryNameInput(name)
        }
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val adapter = CategoryAdapter().also { categoryAdapter = it }

        currentBinding.rvCategories.adapter = adapter
        currentBinding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvCategories.itemAnimator = null

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvCategories,
            adapter = adapter,
            getItemId = { category -> category.id },
            onEdit = { category ->
                showCategoryInputDialog(category)
            },
            onDeleteClicked = { category ->
                // 1. Сохраняем состояние во ViewModel
                viewModel.onDeleteIconClicked(category)

                // 2. Показываем диалог
                DeleteDialogFragment.newInstance(
                    requestKey = AppointmentConstants.Request.DELETE_CATEGORY,
                    message = getString(com.avetiso.core.R.string.delete_dialog_message, category.name)
                ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
            },
            onItemClick = { category ->
                setFragmentResult(
                    AppointmentConstants.Request.SELECTION_CATEGORY,
                    bundleOf(AppointmentConstants.Result.SELECTED_CATEGORY_NAME to category.name)
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
                showCategoryInputDialog(null)
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
        val context = binding?.root?.context
        val isEditMode = category != null

        // СООБЩАЕМ VIEWMODEL О НАМЕРЕНИИ
        if (isEditMode) {
            viewModel.onEditCategoryClicked(category!!)
        } else {
            viewModel.onAddCategoryClicked()
        }

        val title = if (isEditMode) {
            context?.getString(R.string.Редактировать_категорию).toString()
        } else {
            context?.getString(R.string.Новая_категория).toString()
        }

        val initialValue = category?.name ?: ""

        InputDialogFragment.newInstance(
            requestKey = AppointmentConstants.Request.INPUT_CATEGORY,
            title = title,
            hint = context?.getString(R.string.Название_категории).toString(),
            initialValue = initialValue
        ).show(childFragmentManager, AppConstants.Result.INPUT_DIALOG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        categoryAdapter = null
        actions = null
    }
}