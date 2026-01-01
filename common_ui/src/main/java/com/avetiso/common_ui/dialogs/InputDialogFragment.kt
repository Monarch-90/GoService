package com.avetiso.common_ui.dialogs

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.databinding.DialogInputBinding
import com.avetiso.core.AppConstants

class InputDialogFragment : BaseDialogFragment<DialogInputBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): DialogInputBinding {
        return DialogInputBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE) ?: ""
        val hint = arguments?.getString(ARG_HINT) ?: ""
        val initialValue = arguments?.getString(ARG_INITIAL_VALUE) ?: ""
        val requestKey = arguments?.getString(ARG_REQUEST_KEY) ?: ""
        val isMultiline = arguments?.getBoolean(ARG_IS_MULTILINE) ?: false
        val forbiddenValues = arguments?.getStringArrayList(ARG_FORBIDDEN_VALUES) ?: emptyList<String>()
        val allowEmpty = arguments?.getBoolean(ARG_ALLOW_EMPTY) ?: false

        binding.tvTitle.text = title
        binding.inputLayout.hint = hint
        binding.inputEditText.setText(initialValue)

        // Настройка поля ввода (однострочное или многострочное)
        if (isMultiline) {
            binding.inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            binding.inputEditText.minLines = 3
            binding.inputEditText.maxLines = 5
            binding.inputEditText.gravity = android.view.Gravity.TOP or android.view.Gravity.START
        } else {
            binding.inputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        // Сброс ошибки при вводе
        binding.inputEditText.addTextChangedListener {
            binding.inputLayout.error = null
        }

        binding.btnNegative.setOnClickListener { dismiss() }

        binding.btnPositive.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()

            when {
                text.isBlank() && !allowEmpty -> {
                    binding.inputLayout.error = "Поле не может быть пустым"
                }
                // ПРОВЕРКА НА ДУБЛИКАТ ВНУТРИ ДИАЛОГА
                forbiddenValues.any { it.equals(text, ignoreCase = true) } -> {
                    binding.inputLayout.error = "Такое поле уже существует"
                }

                else -> {
                    setFragmentResult(requestKey, bundleOf(AppConstants.Result.RESULT_TEXT to text))
                    dismiss()
                }
            }
        }
    }

    companion object {
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_HINT = "arg_hint"
        private const val ARG_INITIAL_VALUE = "arg_initial_value"
        private const val ARG_IS_MULTILINE = "arg_is_multiline"
        private const val ARG_FORBIDDEN_VALUES = "arg_forbidden_values"
        private const val ARG_ALLOW_EMPTY = "arg_allow_empty"

        fun newInstance(
            requestKey: String,
            title: String,
            hint: String,
            initialValue: String = "",
            isMultiline: Boolean = false,
            forbiddenValues: List<String> = emptyList(),
            allowEmpty: Boolean = false,
        ): InputDialogFragment {
            return InputDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_TITLE to title,
                    ARG_HINT to hint,
                    ARG_INITIAL_VALUE to initialValue,
                    ARG_IS_MULTILINE to isMultiline,
                    ARG_FORBIDDEN_VALUES to ArrayList(forbiddenValues),
                    ARG_ALLOW_EMPTY to allowEmpty,
                )
            }
        }
    }
}