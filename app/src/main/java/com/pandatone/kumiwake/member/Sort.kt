package com.pandatone.kumiwake.member

import android.app.Activity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter

/**
 * Created by atsushi_2 on 2016/03/30.
 */
object Sort {

    private const val ASC = "ASC"   //昇順 (1.2.3....)
    private const val DESC = "DESC" //降順 (3.2.1....)
    private lateinit var NS: String
    private lateinit var ST: String
    internal var initial = 0

    fun memberSort(builder: androidx.appcompat.app.AlertDialog.Builder, activity: Activity,listAdp:MemberFragmentViewAdapter) {

        val mbAdapter = MemberAdapter(ArrayList(),activity)

        val items = arrayOf(
                activity.getString(R.string.registration_ascending),
                activity.getString(R.string.registration_descending),
                activity.getString(R.string.name_ascending),
                activity.getString(R.string.name_descending),
                activity.getString(R.string.age_ascending),
                activity.getString(R.string.age_descending))

        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> {
                    NS = MemberAdapter.MB_ID
                    ST = ASC
                }
                1 -> {
                    NS = MemberAdapter.MB_ID
                    ST = DESC
                }
                2 -> {
                    NS = MemberAdapter.MB_READ
                    ST = ASC
                }
                3 -> {
                    NS = MemberAdapter.MB_READ
                    ST = DESC
                }
                4 -> {
                    NS = MemberAdapter.MB_AGE
                    ST = ASC
                }
                5 -> {
                    NS = MemberAdapter.MB_AGE
                    ST = DESC
                }
            }
            mbAdapter.sortNames(NS, ST)
            MemberFragmentViewAdapter.nowSort = NS
            MemberFragmentViewAdapter.sortType = ST
            listAdp.notifyDataSetChanged() //loadName()を呼ばない！
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)
    }

    fun groupSort(builder: androidx.appcompat.app.AlertDialog.Builder, activity: Activity) {

        val gpAdapter = GroupAdapter(activity)

        val items = arrayOf(
                activity.getString(R.string.registration_ascending),
                activity.getString(R.string.registration_descending),
                activity.getString(R.string.name_ascending),
                activity.getString(R.string.name_descending),
                activity.getString(R.string.member_ascending),
                activity.getString(R.string.member_descending))
        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> gpAdapter.sortGroups(GroupAdapter.GP_ID, ASC)
                1 -> gpAdapter.sortGroups(GroupAdapter.GP_ID, DESC)
                2 -> gpAdapter.sortGroups(GroupAdapter.GP_NAME, ASC)
                3 -> gpAdapter.sortGroups(GroupAdapter.GP_NAME, DESC)
                4 -> gpAdapter.sortGroups(GroupAdapter.GP_BELONG, ASC)
                5 -> gpAdapter.sortGroups(GroupAdapter.GP_BELONG, DESC)
            }
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

}
