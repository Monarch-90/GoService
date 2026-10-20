package com.avetiso.common_ui.dialogs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.CommonConstants
import com.avetiso.common_ui.R
import com.avetiso.common_ui.databinding.DialogConfirmationBinding
import com.avetiso.common_ui.dialogs.models.ConfirmationDialogPayload
import com.avetiso.core.AppConstants

class ConfirmationDialogFragment : BaseDialogFragment<DialogConfirmationBinding>() {

    private val payload: ConfirmationDialogPayload by lazy {
        val args = requireArguments()
        BundleCompat.getParcelable(
            args,
            CommonConstants.ConfirmationDialogKeys.ARG_PAYLOAD,
            ConfirmationDialogPayload::class.java
        )
            ?: throw IllegalArgumentException("Payload is required for ConfirmationDialogFragment")
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): DialogConfirmationBinding {
        return DialogConfirmationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Используем with(binding) для чистоты кода, но аккуратно
        with(binding) {
            tvTitle.text = payload.title
            tvMessage.text = payload.message

            // Используем ресурсы, если текст не передан явно
            btnPositive.text = payload.positiveText ?: getString(R.string.ok)
            btnNegative.text = payload.negativeText ?: getString(R.string.cancel)
            btnInfo.visibility = if (payload.infoUrl.isNullOrBlank()) View.GONE else View.VISIBLE
            btnInfo.text = payload.infoText.orEmpty()
        }
    }

    private fun setupListeners() {
        binding.btnInfo.setOnClickListener {
            val url = payload.infoUrl ?: return@setOnClickListener
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                // The user can still choose either option if a browser is unavailable.
            }
        }

        binding.btnPositive.setOnClickListener {
            sendResult(confirmed = true)
        }

        binding.btnNegative.setOnClickListener {
            sendResult(confirmed = false)
        }
    }

    private fun sendResult(confirmed: Boolean) {
        setFragmentResult(
            payload.requestKey,
            bundleOf(AppConstants.Result.RESULT_CONFIRMED to confirmed)
        )
        dismiss()
    }

    companion object {
        fun newInstance(payload: ConfirmationDialogPayload): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().apply {
                arguments = bundleOf(CommonConstants.ConfirmationDialogKeys.ARG_PAYLOAD to payload)
            }
        }
    }
}
