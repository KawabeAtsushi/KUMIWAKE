package com.pandatone.kumiwake.member

import android.content.Context
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter

/**
 * Created by atsushi_2 on 2016/03/30.
 */
object Sort {

    val ASC = "ASC"   //昇順 (1.2.3....)
    val DESC = "DESC" //降順 (3.2.1....)
    internal var dbAdapter = MemberListAdapter(name_getContext())
    internal var gpdbAdapter = GroupListAdapter(name_getContext())
    var NS: String
    internal var initial = 2

    fun memberSort(builder: android.support.v7.app.AlertDialog.Builder) {

        val Items = arrayOf(name_getContext()!!.getString(R.string.name_ascending), name_getContext()!!.getString(R.string.name_descending), name_getContext()!!.getString(R.string.registration_ascending), name_getContext()!!.getString(R.string.registration_descending), name_getContext()!!.getString(R.string.age_ascending), name_getContext()!!.getString(R.string.age_descending), name_getContext()!!.getString(R.string.grade_ascending), name_getContext()!!.getString(R.string.grade_descending))
        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(Items, initial) { dialog, which -> initial = which }

        builder.setPositiveButton("OK") { dialog, which ->
            when (initial) {
                0 -> {
                    NS = "NAME"
                    dbAdapter.sortNames(dbAdapter.MB_NAME_READ, ASC)
                }
                1 -> {
                    NS = "NAME"
                    dbAdapter.sortNames(dbAdapter.MB_NAME_READ, DESC)
                }
                2 -> {
                    NS = "ID"
                    dbAdapter.sortNames(dbAdapter.MB_ID, ASC)
                }
                3 -> {
                    NS = "ID"
                    dbAdapter.sortNames(dbAdapter.MB_ID, DESC)
                }
                4 -> {
                    NS = "AGE"
                    dbAdapter.sortNames(dbAdapter.MB_AGE, ASC)
                }
                5 -> {
                    NS = "AGE"
                    dbAdapter.sortNames(dbAdapter.MB_AGE, DESC)
                }
                6 -> {
                    NS = "GRADE"
                    dbAdapter.sortNames(dbAdapter.MB_GRADE, ASC)
                }
                7 -> {
                    NS = "GRADE"
                    dbAdapter.sortNames(dbAdapter.MB_GRADE, DESC)
                }
            }
            NameListAdapter.nowSort = NS
            dbAdapter.notifyDataSetChanged()
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { dialog, which -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

    fun groupSort(builder: android.support.v7.app.AlertDialog.Builder) {

        val Items = arrayOf(name_getContext()!!.getString(R.string.name_ascending), name_getContext()!!.getString(R.string.name_descending), name_getContext()!!.getString(R.string.registration_ascending), name_getContext()!!.getString(R.string.registration_descending), name_getContext()!!.getString(R.string.member_ascending), name_getContext()!!.getString(R.string.member_descending))
        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(Items, initial) { dialog, which -> initial = which }

        builder.setPositiveButton("OK") { dialog, which ->
            when (initial) {
                0 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_NAME, ASC)
                1 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_NAME, DESC)
                2 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_ID, ASC)
                3 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_ID, DESC)
                4 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_BELONG, ASC)
                5 -> gpdbAdapter.sortGroups(gpdbAdapter.GP_BELONG, DESC)
            }

            gpdbAdapter.notifyDataSetChanged()
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { dialog, which -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

    /*
    public static void NameSort(String sortType) {

        if (sortType.equals(ASC)) {
            Collections.sort(FragmentMember.nameList, new Comparator<Name>() {
                @Override
                public int compare(Name n1, Name n2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(n1.getName(), n2.getName());
                }
            });
        }

        if (sortType.equals(DESC)) {
            Collections.sort(FragmentMember.nameList, new Comparator<Name>() {
                @Override
                public int compare(Name n1, Name n2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(n2.getName(), n1.getName());
                }
            });
        }

        FragmentMember.listAdapter.notifyDataSetChanged();
    }

    public static void GroupSort(String sortType) {

        if (sortType.equals(ASC)) {
            Collections.sort(FragmentGroup.nameList, new Comparator<GroupListAdapter.Group>() {
                @Override
                public int compare(GroupListAdapter.Group g1, GroupListAdapter.Group g2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(g1.getGroup(), g2.getGroup());
                }
            });
        }

        if (sortType.equals(DESC)) {
            Collections.sort(FragmentGroup.nameList, new Comparator<GroupListAdapter.Group>() {
                @Override
                public int compare(GroupListAdapter.Group g1, GroupListAdapter.Group g2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(g2.getGroup(), g1.getGroup());
                }
            });
        }

        FragmentMember.listAdapter.notifyDataSetChanged();
    }
    */

    fun name_getContext(): Context? {
        return MemberMain.context
    }
}
