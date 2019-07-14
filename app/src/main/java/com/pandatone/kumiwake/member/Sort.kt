package com.pandatone.kumiwake.member

import android.content.Context
import com.pandatone.kumiwake.MyApplication
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
    internal var initial = 2

    fun memberSort(builder: androidx.appcompat.app.AlertDialog.Builder) {

        val dbAdapter = MemberListAdapter(getContext())

        val items = arrayOf(MyApplication.context?.getString(R.string.name_ascending),
                MyApplication.context?.getString(R.string.name_descending),
                MyApplication.context?.getString(R.string.registration_ascending),
                MyApplication.context?.getString(R.string.registration_descending),
                MyApplication.context?.getString(R.string.age_ascending),
                MyApplication.context?.getString(R.string.age_descending))

        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> {
                    NS = "NAME"
                    dbAdapter.sortNames(MemberListAdapter.MB_NAME_READ, ASC)
                }
                1 -> {
                    NS = "NAME"
                    dbAdapter.sortNames(MemberListAdapter.MB_NAME_READ, DESC)
                }
                2 -> {
                    NS = "ID"
                    dbAdapter.sortNames(MemberListAdapter.MB_ID, ASC)
                }
                3 -> {
                    NS = "ID"
                    dbAdapter.sortNames(MemberListAdapter.MB_ID, DESC)
                }
                4 -> {
                    NS = "AGE"
                    dbAdapter.sortNames(MemberListAdapter.MB_AGE, ASC)
                }
                5 -> {
                    NS = "AGE"
                    dbAdapter.sortNames(MemberListAdapter.MB_AGE, DESC)
                }
            }
            NameListAdapter.nowSort = NS
            dbAdapter.notifyDataSetChanged()
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

    fun groupSort(builder: androidx.appcompat.app.AlertDialog.Builder) {

        val gpdbAdapter = GroupListAdapter(getContext())

        val items = arrayOf(MyApplication.context?.getString(R.string.name_ascending), MyApplication.context?.getString(R.string.name_descending), MyApplication.context?.getString(R.string.registration_ascending), MyApplication.context?.getString(R.string.registration_descending), MyApplication.context?.getString(R.string.member_ascending), MyApplication.context?.getString(R.string.member_descending))
        builder.setTitle(R.string.sorting)
        builder.setSingleChoiceItems(items, initial) { _, which -> initial = which }

        builder.setPositiveButton("OK") { _, _ ->
            when (initial) {
                0 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_NAME, ASC)
                1 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_NAME, DESC)
                2 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_ID, ASC)
                3 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_ID, DESC)
                4 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_BELONG, ASC)
                5 -> gpdbAdapter.sortGroups(GroupListAdapter.GP_BELONG, DESC)
            }

            gpdbAdapter.notifyDataSetChanged()
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)

    }

    private fun getContext(): Context {
        return MemberMain().context
    }

}
