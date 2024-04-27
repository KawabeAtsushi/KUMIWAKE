package com.pandatone.kumiwake.member.function

import android.app.Activity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder.gpNowSort
import com.pandatone.kumiwake.StatusHolder.gpSortType
import com.pandatone.kumiwake.StatusHolder.mbNowSort
import com.pandatone.kumiwake.StatusHolder.mbSortType
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.GroupFragmentViewAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter

/**
 * Created by atsushi_2 on 2016/03/30.
 */
object Sort {

    private const val ASC = "ASC"   //昇順 (1.2.3....)
    private const val DESC = "DESC" //降順 (3.2.1....)
    internal var initial = 0

    fun memberSort(
        activity: Activity,
        memberList: ArrayList<Member>,
        listAdp: MemberFragmentViewAdapter,
        mbAdapter: MemberAdapter
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)

        val items = arrayOf(
            activity.getString(R.string.registration_ascending),
            activity.getString(R.string.registration_descending),
            activity.getString(R.string.name_ascending),
            activity.getString(R.string.name_descending),
            activity.getString(R.string.age_ascending),
            activity.getString(R.string.age_descending)
        )

        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> {
                    mbNowSort = MemberAdapter.MB_ID
                    mbSortType = ASC
                }

                1 -> {
                    mbNowSort = MemberAdapter.MB_ID
                    mbSortType = DESC
                }

                2 -> {
                    mbNowSort = MemberAdapter.MB_READ
                    mbSortType = ASC
                }

                3 -> {
                    mbNowSort = MemberAdapter.MB_READ
                    mbSortType = DESC
                }

                4 -> {
                    mbNowSort = MemberAdapter.MB_AGE
                    mbSortType = ASC
                }

                5 -> {
                    mbNowSort = MemberAdapter.MB_AGE
                    mbSortType = DESC
                }
            }
            mbAdapter.sortNames(mbNowSort, mbSortType, memberList)
            listAdp.notifyDataSetChanged() //loadName()を呼ばない！
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)
        builder.show()
    }

    fun groupSort(
        activity: Activity,
        groupList: ArrayList<Group>,
        listAdp: GroupFragmentViewAdapter
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        val gpAdapter = GroupAdapter(activity)

        val items = arrayOf(
            activity.getString(R.string.registration_ascending),
            activity.getString(R.string.registration_descending),
            activity.getString(R.string.name_ascending),
            activity.getString(R.string.name_descending),
            activity.getString(R.string.member_ascending),
            activity.getString(R.string.member_descending)
        )
        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> {
                    gpNowSort = GroupAdapter.GP_ID
                    gpSortType = ASC
                }

                1 -> {
                    gpNowSort = GroupAdapter.GP_ID
                    gpSortType = DESC
                }

                2 -> {
                    gpNowSort = GroupAdapter.GP_NAME
                    gpSortType = ASC
                }

                3 -> {
                    gpNowSort = GroupAdapter.GP_NAME
                    gpSortType = DESC
                }

                4 -> {
                    gpNowSort = GroupAdapter.GP_BELONG
                    gpSortType = ASC
                }

                5 -> {
                    gpNowSort = GroupAdapter.GP_BELONG
                    gpSortType = DESC
                }
            }
            gpAdapter.sortGroups(gpNowSort, gpSortType, groupList)
            listAdp.notifyDataSetChanged() //loadName()を呼ばない！
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)
        builder.show()
    }

}
