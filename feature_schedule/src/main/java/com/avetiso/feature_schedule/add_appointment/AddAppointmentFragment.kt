package com.avetiso.feature_schedule.add_appointment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.databinding.FragmentAddAppointmentBinding
import com.avetiso.feature_schedule.databinding.ViewStepperBinding
import kotlinx.coroutines.launch

class AddAppointmentFragment : Fragment(R.layout.fragment_add_appointment) {

    private var binding: FragmentAddAppointmentBinding? = null
    private val viewModel: AddAppointmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddAppointmentBinding.bind(view)

        val currentBinding = binding ?: return

        currentBinding.viewPager.adapter = AddAppointmentViewPagerAdapter(this)
        currentBinding.viewPager.isUserInputEnabled = false

        currentBinding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        currentBinding.buttonNext.setOnClickListener {
            viewModel.handleEvent(AddAppointmentEvent.NextButtonClicked)
        }

        // Подписываемся на изменения состояния
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AddAppointmentState) {
        val currentBinding = binding ?: return

        // Обновляем ViewPager
        currentBinding.viewPager.setCurrentItem(state.currentStep, true)

        // Обновляем кнопку
        currentBinding.buttonNext.isEnabled = state.isNextButtonEnabled
        if (state.currentStep == ADD_APPOINTMENT_PAGE_COUNT - 1) {
            currentBinding.buttonNext.text = "Готово"
        } else {
            currentBinding.buttonNext.text = "Далее"
        }

        // Обновляем степпер
        updateStepper(currentBinding.stepper, state.currentStep)
    }

    private fun updateStepper(stepperBinding: ViewStepperBinding, currentStep: Int) {
        val activeBg =
            ContextCompat.getDrawable(requireContext(), R.drawable.stepper_indicator_active)
        val inactiveBg =
            ContextCompat.getDrawable(requireContext(), R.drawable.stepper_indicator_inactive)
        val activeColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        stepperBinding.step1Text.background = if (currentStep == 0) activeBg else inactiveBg
        stepperBinding.step1Text.setTextColor(if (currentStep == 0) activeColor else inactiveColor)

        stepperBinding.step2Text.background = if (currentStep == 1) activeBg else inactiveBg
        stepperBinding.step2Text.setTextColor(if (currentStep == 1) activeColor else inactiveColor)

        stepperBinding.step3Text.background = if (currentStep == 2) activeBg else inactiveBg
        stepperBinding.step3Text.setTextColor(if (currentStep == 2) activeColor else inactiveColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}