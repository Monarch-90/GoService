package com.avetiso.feature_statistics.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.adapter.StatisticsAdapter
import com.avetiso.feature_statistics.databinding.FragmentStatisticsBinding
import com.avetiso.feature_statistics.mvi.StatisticsEvent
import com.avetiso.feature_statistics.mvi.StatisticsIntent
import com.avetiso.feature_statistics.mvi.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    private val viewModel: StatisticsViewModel by viewModels()

    private var statisticsAdapter: StatisticsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatisticsBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val adapter = StatisticsAdapter(
            onPeriodSelected = { period ->
                viewModel.processIntent(StatisticsIntent.ChangePeriod(period))
            },
            onFinanceClicked = {
                viewModel.processIntent(StatisticsIntent.OnFinancialCardClicked)
            },
            onInventorySeeAllClicked = {
                viewModel.processIntent(StatisticsIntent.OnInventorySeeAllClicked)
            },
            onInventoryCopyClicked = {
                viewModel.processIntent(StatisticsIntent.OnInventoryCopyToClipboardClicked)
            },
            onFreeSlotsClicked = {
                viewModel.processIntent(StatisticsIntent.OnGenerateFreeSlotsClicked)
            },
            onSharePriceClicked = {
                viewModel.processIntent(StatisticsIntent.OnSharePriceClicked)
            }
        ).also { statisticsAdapter = it }

        binding.rvStatistics.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
            // Отключаем системную анимацию, чтобы при обновлении стейта не было лишнего мерцания
            itemAnimator = null
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Подписка на состояние (UI)
                launch {
                    viewModel.state.collect { state ->
                        binding.progressBar.isVisible = state.isLoading
                        statisticsAdapter?.submitList(state.dashboardItems)
                    }
                }

                // 2. Подписка на разовые события (навигация, тосты, буфер обмена)
                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun handleEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.ShowToast -> {
                Toast.makeText(requireContext(), event.messageResId, Toast.LENGTH_SHORT).show()
            }

            is StatisticsEvent.CopyToClipboard -> {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("inventory", event.text)
                clipboard.setPrimaryClip(clip)
                // TODO: Можно добавить тост "Скопировано", вызовем его из ViewModel позже
            }

            is StatisticsEvent.NavigateToFinancialDetails -> {
                // TODO: Вызов навигатора
            }

            is StatisticsEvent.NavigateToInventory -> {
                // TODO: Вызов навигатора
            }

            is StatisticsEvent.NavigateToClientsStats -> {
                // TODO: Вызов навигатора
            }

            is StatisticsEvent.NavigateToCustomDateRangePicker -> {
                // TODO: Показать DatePickerDialog или перейти на экран выбора
            }
        }
    }

    override fun onDestroyView() {
        binding.rvStatistics.adapter = null
        statisticsAdapter = null
        _binding = null
        super.onDestroyView()
    }
}