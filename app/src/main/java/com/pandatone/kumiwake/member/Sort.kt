package com.pandatone.kumiwake.member

import android.app.Activity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter

/**
 * Created by atsushi_2 on 2016/03/30.
 */
object Sort {

    private const val ASC = "ASC"   //昇順 (1.2.3....)
    private const val DESC = "DESC" //降順 (3.2.1....)
    private lateinit var NS: String
    private lateinit var ST: String
    internal var initial = 0

    fun memberSort(builder: androidx.appcompat.app.AlertDialog.Builder, activity: Activity,listAdp:NameListAdapter) {

        val dbAdapter = MemberListAdapter(activity)

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
                    NS = MemberListAdapter.MB_ID
                    ST = ASC
                }
                1 -> {
                    NS = MemberListAdapter.MB_ID
                    ST = DESC
                }
                2 -> {
                    NS = MemberListAdapter.MB_READ
                    ST = ASC
                }
                3 -> {
                    NS = MemberListAdapter.MB_READ
                    ST = DESC
                }
                4 -> {
                    NS = MemberListAdapter.MB_AGE
                    ST = ASC
                }
                5 -> {
                    NS = MemberListAdapter.MB_AGE
                    ST = DESC
                }
            }
            dbAdapter.sortNames(NS, ST)
            NameListAdapter.nowSort = NS
            NameListAdapter.sortType = ST
            listAdp.notifyDataSetChanged() //loadName()を呼ばない！
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)
    }

    fun groupSort(builder: androidx.appcompat.app.AlertDialog.Builder, activity: Activity) {

        val gpdbAdapter = GroupListAdapter(activity)

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
                0 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_ID, ASC)
                1 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_ID, DESC)
                2 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_NAME, ASC)
                3 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_NAME, DESC)
                4 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_BELONG, ASC)
                5 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_BELONG, DESC)
            }

            FragmentGroupChoiceMode.listAdp.notifyDataSetChanged()
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

}
