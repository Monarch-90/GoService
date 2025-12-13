package com.avetiso.common_ui.dialogs

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

// Generic тип VB позволяет нам использовать любой ViewBinding
abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    // Абстрактный метод, чтобы наследник сказал, как раздувать макет
    abstract fun bindView(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = bindView(inflater, container)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Настройка ширины (чтобы диалог был красивым, а не узким/широким)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // 90% от ширины экрана
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        // Прозрачный фон обязателен для работы скруглений cardView/background
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}