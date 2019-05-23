package com.pandatone.kumiwake.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.FragmentGroup;
import com.pandatone.kumiwake.member.FragmentMember;
import com.pandatone.kumiwake.member.MemberMain;

/**
 * Created by atsushi_2 on 2016/02/23.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {
    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentMember();
            case 1:
                return new FragmentGroup();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return MemberMain.getContext().getString(R.string.member);
            case 1:
                return MemberMain.getContext().getString(R.string.group);
        }
        return null;
    }

    public Fragment findFragmentByPosition(ViewPager viewPager,
                                           int position) {
        return (Fragment) instantiateItem(viewPager, position);
    }

}