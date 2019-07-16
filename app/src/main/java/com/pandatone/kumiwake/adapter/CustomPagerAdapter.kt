package com.pandatone.kumiwake.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.FragmentGroup
import com.pandatone.kumiwake.member.FragmentMember



/**
 * Created by atsushi_2 on 2016/02/23.
 */
class CustomPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> return FragmentMember()
            1 -> return FragmentGroup()
        }
        return null
    }

    override fun getItemPosition(`object`: Any): Int {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return MyApplication.context?.getString(R.string.member)
            1 -> return MyApplication.context?.getString(R.string.group)
        }
        return null
    }

    fun findFragmentByPosition(viewPager: ViewPager,
                               position: Int): Fragment {
        return instantiateItem(viewPager, position) as Fragment
    }

}