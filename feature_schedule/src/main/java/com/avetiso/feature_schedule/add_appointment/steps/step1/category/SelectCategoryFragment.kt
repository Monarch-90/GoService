package com.avetiso.feature_schedule.add_appointment.steps.step1.category

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.databinding.DialogAddCategoryBinding
import com.avetiso.feature_schedule.databinding.FragmentSelectCategoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectCategoryFragment : Fragment(R.layout.fragment_select_category) {

    private var binding: FragmentSelectCategoryBinding? = null
    private val viewModel: SelectCategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoryBinding.bind(view)

        setupMenu()

        val categoryAdapter = CategoryAdapter { category ->
            // Возвращаем результат предыдущему экрану
            setFragmentResult("category_selection", bundleOf("selected_category_name" to category.name))
            findNavController().navigateUp()
        }

        binding?.recyclerViewCategories?.adapter = categoryAdapter
        binding?.toolbar?.setNavigationOnClickListener { findNavController().navigateUp() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    categoryAdapter.submitList(categories)
                }
            }
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.select_category_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_add_category) {
                    showAddCategoryDialog()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Новая категория")
            .setView(dialogBinding.root)
            .setPositiveButton("Добавить", null) // Обработчик установим ниже
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = dialogBinding.inputEditTextCategoryName.text.toString()
                if (name.isNotBlank()) {
                    viewModel.addCategory(name)
                    dialog.dismiss()
                } else {
                    dialogBinding.inputLayoutCategoryName.error = "Название не может быть пустым"
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}