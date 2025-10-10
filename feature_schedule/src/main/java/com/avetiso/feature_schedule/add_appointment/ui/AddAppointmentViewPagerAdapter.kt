package com.avetiso.feature_schedule.add_appointment.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step1.ui.Step1SelectServiceFragment
import com.avetiso.feature_schedule.add_appointment.steps.step2.ui.Step2SelectTimeFragment

const val ADD_APPOINTMENT_PAGE_COUNT = 3

class AddAppointmentViewPagerAdapter(
    fragment: Fragment,
    private val clientSelectorFragmentFactory: () -> Fragment,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = ADD_APPOINTMENT_PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Step1SelectServiceFragment()
            1 -> Step2SelectTimeFragment()
            2 -> clientSelectorFragmentFactory()
            else -> throw IllegalStateException("Invalid position for ViewPager")
        }
    }
}