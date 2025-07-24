package com.avetiso.feature_schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avetiso.feature_schedule.databinding.FragmentScheduleBinding

// Пока без Hilt и ViewModel, просто создаем View
class ScheduleFragment : Fragment() {

    private var binding: FragmentScheduleBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentScheduleBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Здесь мы будем настраивать наш UI, например, вешать слушатели на кнопки
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Обязательно очищаем binding, чтобы избежать утечек памяти
        binding = null
    }
}