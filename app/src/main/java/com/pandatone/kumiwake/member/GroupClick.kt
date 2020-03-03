package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.ui.members.FragmentGroupMain

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
class GroupClick(val c:Activity) {
    private lateinit var group: TextView
    private lateinit var number: TextView
    private lateinit var belongMb: TextView
    private lateinit var belongList: ListView
    lateinit var okBt: Button

    fun infoDialog(item : Group, memberByBelong:ArrayList<Member>) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.group_info, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

        group = view.findViewById<View>(R.id.infoName) as TextView
        number = view.findViewById<View>(R.id.infoNoOfMb) as TextView
        belongMb = view.findViewById<View>(R.id.infoMember) as TextView
        belongList = view.findViewById<View>(R.id.belongList) as ListView
        okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)
        setInfo(item,memberByBelong)
        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item : Group, memberByBelong:ArrayList<Member>) {
        val adapter = SmallMBListAdapter(c, memberByBelong, false, showLeaderNo = false)

        group.text = "${c.getText(R.string.group_name)} : ${item.name}"
        number.text = "${c.getText(R.string.number_of_member)} : ${adapter.count}${c.getString(R.string.people)}"
        belongMb.text = "${c.getText(R.string.belong)}${c.getText(R.string.member)}"
        belongList.adapter = adapter

        //メンバー数の更新
        val belongNo: Int
        val gpAdapter = GroupAdapter(c)

        val id: Int = item.id
        belongNo = adapter.count
        gpAdapter.updateBelongNo(id.toString(), belongNo)
        FragmentGroupMain().loadName()
    }
}