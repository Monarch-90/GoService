package com.avetiso.feature_sidebar.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment(R.layout.fragment_about) {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}