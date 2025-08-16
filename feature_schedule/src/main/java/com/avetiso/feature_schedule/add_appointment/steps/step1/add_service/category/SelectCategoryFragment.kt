package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.category

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.databinding.DialogAddCategoryBinding
import com.avetiso.feature_schedule.databinding.FragmentSelectCategoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "TouchDebug"

@AndroidEntryPoint
class SelectCategoryFragment : Fragment(R.layout.fragment_select_category) {

    private var binding: FragmentSelectCategoryBinding? = null
    private val viewModel: SelectCategoryViewModel by viewModels()
    private var actions: RecyclerViewActions<CategoryEntity>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoryBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        val categoryAdapter = CategoryAdapter()

        currentBinding.rvCategories.adapter = categoryAdapter
        currentBinding.rvCategories.layoutManager = LinearLayoutManager(requireContext())

        actions = RecyclerViewActions(
            fragment = this,
            recyclerView = currentBinding.rvCategories,
            adapter = categoryAdapter,
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
            },
            onActionsShown = {}
        )

        // Просто присваиваем actions в адаптер.
        categoryAdapter.actions = actions
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

    private fun showCategoryInputDialog(category: CategoryEntity? = null) {
        val isEditMode = category != null
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)

        if (isEditMode) {
            dialogBinding.ietCategoryName.setText(category?.name)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isEditMode) "Редактировать категорию" else "Новая категория")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .setPositiveButton(if (isEditMode) "Сохранить" else "Добавить", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = dialogBinding.ietCategoryName.text.toString().trim()
                dialogBinding.ilCategoryName.error = null

                val currentCategories = viewModel.categories.value
                val hasDuplicate = currentCategories.any {
                    // В режиме редактирования исключаем проверку на саму себя
                    (if (isEditMode) it.id != category?.id else true) &&
                            it.name.equals(name, ignoreCase = true)
                }

                when {
                    name.isBlank() -> {
                        dialogBinding.ilCategoryName.error =
                            "Название не может быть пустым"
                    }

                    hasDuplicate -> {
                        dialogBinding.ilCategoryName.error =
                            "Такая категория уже существует"
                    }

                    else -> {
                        viewModel.addOrUpdateCategory(name, category?.id)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        actions = null
    }
}