package com.avetiso.feature_statistics.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.adapter.StatisticsAdapter
import com.avetiso.feature_statistics.databinding.FragmentStatisticsBinding
import com.avetiso.feature_statistics.mapper.StatisticsUiMapper
import com.avetiso.feature_statistics.mvi.CopyToClipboard
import com.avetiso.feature_statistics.mvi.NavigateToCompletedAppointments
import com.avetiso.feature_statistics.mvi.NavigateToFrequentClients
import com.avetiso.feature_statistics.mvi.NavigateToInventory
import com.avetiso.feature_statistics.mvi.ShowToast
import com.avetiso.feature_statistics.mvi.StatisticsEvent
import com.avetiso.feature_statistics.mvi.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    @Inject
    lateinit var uiMapper: StatisticsUiMapper

    private val adapter by lazy {
        // Вызов правильного метода processIntent из твоей ViewModel
        StatisticsAdapter(onIntent = { viewModel.processIntent(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatisticsBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Тихо обновляем данные каждый раз, когда возвращаемся на экран статистики.
        // Это решит проблему неактуальных данных после изменения статусов на других экранах.
        viewModel.processIntent(com.avetiso.feature_statistics.mvi.OnScreenResumed)
    }

    private fun setupRecyclerView() {
        binding.rvStatistics.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StatisticsFragment.adapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.state.collect { state ->
                        val uiItems = uiMapper.mapToUiList(state)
                        adapter.submitList(uiItems)
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

    private fun handleEvent(event: StatisticsEvent) {
        // Так как мы избавились от вложенности (nested classes),
        // обращаемся напрямую к корневым классам событий
        when (event) {
            is CopyToClipboard -> {
                // Используем правильные названия свойств из твоего data class
                copyToClipboard(event.textToCopy, event.successMessageResId)
            }
            is ShowToast -> {
                Toast.makeText(requireContext(), event.messageResId, Toast.LENGTH_SHORT).show()
            }
            is NavigateToCompletedAppointments -> {
                // Команда навигации к деталям финансов
            }
            is NavigateToFrequentClients -> {
                // Команда навигации к деталям загруженности
            }
            is NavigateToInventory -> {
                // Команда навигации к складу
            }
        }
    }

    private fun copyToClipboard(text: String, successMessageResId: Int) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.statistics_title), text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), successMessageResId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvStatistics.adapter = null
        _binding = null
    }
}