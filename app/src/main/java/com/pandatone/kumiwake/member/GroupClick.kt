package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.ui.members.FragmentGroupMain

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
object GroupClick {
    private lateinit var group: TextView
    private lateinit var number: TextView
    private lateinit var belongMb: TextView
    private lateinit var belongList: ListView
    lateinit var okBt: Button


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
    fun setInfo(c:Context,item : GroupListAdapter.Group,nameByBelong:ArrayList<Name>) {
        val adapter = MBListViewAdapter(c, nameByBelong, false, showLeaderNo = false)

        group.text = "${c.getText(R.string.group_name)} : ${item.group}"
        number.text = "${c.getText(R.string.number_of_member)} : ${adapter.count}${c.getString(R.string.people)}"
        belongMb.text = "${c.getText(R.string.belong)}${c.getText(R.string.member)}"
        belongList.adapter = adapter

        //メンバー数の更新
        val belongNo: Int
        val GPdbAdapter = GroupListAdapter(c)

        val id: Int = item.id
        belongNo = adapter.count
        GPdbAdapter.updateBelongNo(id.toString(), belongNo)
        FragmentGroupMain().loadName()
    }
}