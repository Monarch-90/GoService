package com.avetiso.feature_schedule.add_appointment

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.avetiso.feature_schedule.add_appointment.steps.step1.Step1SelectServiceFragment
import com.avetiso.feature_schedule.add_appointment.steps.step2.Step2SelectTimeFragment
import com.avetiso.feature_schedule.add_appointment.steps.step3.Step3SelectClientFragment

const val ADD_APPOINTMENT_PAGE_COUNT = 3

class AddAppointmentViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = ADD_APPOINTMENT_PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Step1SelectServiceFragment()
            1 -> Step2SelectTimeFragment()
            2 -> Step3SelectClientFragment()
            else -> throw IllegalStateException("Invalid position for ViewPager")
        }
    }
}