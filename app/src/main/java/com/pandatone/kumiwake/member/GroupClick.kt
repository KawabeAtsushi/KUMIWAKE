package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
object GroupClick {
    internal lateinit var group: TextView
    internal lateinit var number: TextView
    internal lateinit var belongMb: TextView
    internal lateinit var belongList: ListView
    internal lateinit var okBt: Button

    val context = MyApplication.context


    fun groupInfoDialog(view: View, builder: AlertDialog.Builder) {

        group = view.findViewById<View>(R.id.infoName) as TextView
        number = view.findViewById<View>(R.id.infoNoOfMb) as TextView
        belongMb = view.findViewById<View>(R.id.infoMember) as TextView
        belongList = view.findViewById<View>(R.id.belongList) as ListView
        okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)

    }

    @SuppressLint("SetTextI18n")
    fun setInfo(position: Int) {
        val nameByBelong = FragmentMember().searchBelong(FragmentGroup.nameList[position].id.toString())
        val adapter = MBListViewAdapter(context!!, nameByBelong, 0)

        group.text = "${context.getText(R.string.group_name)} : ${FragmentGroup.nameList[position].group}"
        number.text = "${context.getText(R.string.number_of_member)} : ${adapter.count}${context.getString(R.string.people)}"
        belongMb.text = "${context.getText(R.string.belong)}${context.getText(R.string.member)}"
        belongList.adapter = adapter

        //メンバー数の更新
        val id: Int
        val belongNo: Int
        val name: String
        val name_read = "ￚ no data ￚ"
        val GPdbAdapter = GroupListAdapter(context)
        val listItem = FragmentGroup.nameList[position]

        id = listItem.id
        name = listItem.group
        belongNo = adapter.count
        GPdbAdapter.updateGroup(id, name, name_read, belongNo)
    }
}



