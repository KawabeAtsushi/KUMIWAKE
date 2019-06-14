package com.pandatone.kumiwake.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager

import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.FragmentGroup
import com.pandatone.kumiwake.member.FragmentMember
import com.pandatone.kumiwake.member.MemberMain

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

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return MemberMain().applicationContext.getString(R.string.member)
            1 -> return MemberMain().applicationContext.getString(R.string.group)
        }
        return null
    }

    fun findFragmentByPosition(viewPager: ViewPager,
                               position: Int): Fragment {
        return instantiateItem(viewPager, position) as Fragment
    }

}