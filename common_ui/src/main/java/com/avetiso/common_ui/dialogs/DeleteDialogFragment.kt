package com.avetiso.common_ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.databinding.DialogDeleteBinding

class DeleteDialogFragment : BaseDialogFragment<DialogDeleteBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): DialogDeleteBinding {
        return DialogDeleteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE) ?: "Удаление"
        val message = arguments?.getString(ARG_MESSAGE) ?: ""
        val requestKey = arguments?.getString(ARG_REQUEST_KEY) ?: "delete_request"

        binding.tvTitle.text = title
        binding.tvMessage.text = message

        binding.btnPositive.setOnClickListener {
            // Возвращаем True
            setFragmentResult(requestKey, bundleOf(RESULT_CONFIRMED to true))
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            // Возвращаем False (хотя обычно на отмену просто закрывают, но для порядка можно вернуть)
            setFragmentResult(requestKey, bundleOf(RESULT_CONFIRMED to false))
            dismiss()
        }
    }

    companion object {
        const val TAG = "DeleteDialog"
        const val RESULT_CONFIRMED = "delete_confirmed"

        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(
            requestKey: String,
            message: String,
            title: String = "Удаление",
        ): DeleteDialogFragment {
            return DeleteDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_TITLE to title,
                    ARG_MESSAGE to message
                )
            }
        }
    }
}