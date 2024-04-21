package com.pandatone.kumiwake.history

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R


/**
 * Created by atsushi_2 on 2016/02/23.
 */
class HistoryPagerAdapter(private var context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return FragmentHistory()
            1 -> return FragmentKeeps()
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
            0 -> return context.getString(R.string.history)
            1 -> return context.getString(R.string.favorite)
        }
        return null
    }

    fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment {
        return instantiateItem(viewPager, position) as Fragment
    }

}