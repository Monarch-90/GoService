package com.avetiso.feature_sidebar.about.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_sidebar.R
import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.about.mvi.AboutEvent
import com.avetiso.feature_sidebar.about.mvi.AboutIntent
import com.avetiso.feature_sidebar.about.mvi.AboutState
import com.avetiso.feature_sidebar.about.mvi.AboutViewModel
import com.avetiso.feature_sidebar.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private val viewModel: AboutViewModel by viewModels()
    private var _binding: FragmentAboutBinding? = null
    private val binding: FragmentAboutBinding
        get() = checkNotNull(_binding) { SidebarConstants.About.BINDING_LIFECYCLE_ERROR }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeViewModel()
    }

    private fun setupToolbar() {
        // Отправляем MVI Intent, вместо прямой работы с NavController
        binding.toolbar.setNavigationOnClickListener {
            viewModel.processIntent(AboutIntent.OnBackClicked)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            viewModel.processIntent(AboutIntent.OnPrivacyPolicyClicked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun renderState(state: AboutState) {
        when (state) {
            is AboutState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.tvAbout.visibility = View.GONE
            }

            is AboutState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvAbout.visibility = View.GONE
            }

            is AboutState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.tvAbout.visibility = View.VISIBLE
                binding.tvAbout.text = state.text
            }

            is AboutState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.tvAbout.visibility = View.VISIBLE
                binding.tvAbout.text = state.message
            }
        }
    }

    private fun openWebPageSafely(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.sidebar_about_error_browser_not_found, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.sidebar_about_error_open_link, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.ShowToast -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }

            is AboutEvent.NavigateBack -> {
                // Выполняем навигацию ТОЛЬКО по событию из ViewModel
                findNavController().popBackStack()
            }

            is AboutEvent.OpenWebPage -> openWebPageSafely(event.url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}