package com.avetiso.common_ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.databinding.DialogConfirmationBinding

class ConfirmationDialogFragment : BaseDialogFragment<DialogConfirmationBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): DialogConfirmationBinding {
        return DialogConfirmationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем аргументы
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val message = arguments?.getString(ARG_MESSAGE) ?: ""
        val positiveText = arguments?.getString(ARG_POS_TEXT) ?: "ОК"
        val negativeText = arguments?.getString(ARG_NEG_TEXT) ?: "Отмена"
        val requestKey = arguments?.getString(ARG_REQUEST_KEY) ?: "default_request_key"
        val isDestructive = arguments?.getBoolean(ARG_IS_DESTRUCTIVE) ?: false

        // Наполняем View
        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnPositive.text = positiveText
        binding.btnNegative.text = negativeText

        // Если действие "разрушительное" (удаление), красим кнопку в красный
        if (isDestructive) {
            binding.btnPositive.setTextColor(requireContext().getColor(com.avetiso.core.R.color.red))
        }

        // Слушатели
        binding.btnPositive.setOnClickListener {
            // ✅ ГЛАВНЫЙ МОМЕНТ: Возвращаем результат родителю через Fragment Result API
            setFragmentResult(requestKey, bundleOf(RESULT_CONFIRMED to true))
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            setFragmentResult(requestKey, bundleOf(RESULT_CONFIRMED to false))
            dismiss()
        }
    }

    companion object {
        const val TAG = "ConfirmationDialog"
        const val RESULT_CONFIRMED = "result_confirmed"

        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_POS_TEXT = "arg_pos_text"
        private const val ARG_NEG_TEXT = "arg_neg_text"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_IS_DESTRUCTIVE = "arg_is_destructive"

        fun newInstance(
            requestKey: String, // Ключ, по которому родитель узнает этот ответ
            title: String,
            message: String,
            positiveText: String = "ОК",
            negativeText: String = "Отмена",
            isDestructive: Boolean = false, // Если true, кнопка будет красной
        ): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_TITLE to title,
                    ARG_MESSAGE to message,
                    ARG_POS_TEXT to positiveText,
                    ARG_NEG_TEXT to negativeText,
                    ARG_IS_DESTRUCTIVE to isDestructive
                )
            }
        }
    }
}