package com.pandatone.kumiwake.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.ListFragment
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.FragmentGroupChoiceMode
import com.pandatone.kumiwake.member.FragmentMemberChoiceMode
import com.pandatone.kumiwake.member.members.FragmentGroupMain
import com.pandatone.kumiwake.member.members.FragmentMemberMain


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class CustomPagerAdapter(private var context: Context, fm: FragmentManager, val main: Boolean) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): ListFragment {
        when (position) {
            0 -> return if (main) {
                FragmentMemberMain()
            } else {
                FragmentMemberChoiceMode()
            }

            1 -> return if (main) {
                FragmentGroupMain()
            } else {
                FragmentGroupChoiceMode()
            }
        }
        throw IllegalStateException("tableNo $position is invalid for this viewpager")
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
            0 -> return context.getString(R.string.member)
            1 -> return context.getString(R.string.group)
        }
        return null
    }

    fun findFragmentByPosition(viewPager: ViewPager): Fragment {
        return instantiateItem(viewPager, viewPager.currentItem) as Fragment
    }

}