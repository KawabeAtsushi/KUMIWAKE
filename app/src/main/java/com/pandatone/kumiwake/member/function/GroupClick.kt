package com.pandatone.kumiwake.member.function

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.members.FragmentGroupMain

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
class GroupClick(val c: Activity) {
    private lateinit var group: TextView
    private lateinit var number: TextView
    private lateinit var belongMb: TextView
    private lateinit var belongList: ListView
    private lateinit var okBt: Button

    fun infoDialog(item: Group, memberByBelong: ArrayList<Member>) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.group_info,
            c.findViewById<View>(R.id.info_layout) as ViewGroup?
        )

        group = view.findViewById<View>(R.id.infoName) as TextView
        number = view.findViewById<View>(R.id.infoMBNo) as TextView
        belongMb = view.findViewById<View>(R.id.indexMember) as TextView
        belongList = view.findViewById<View>(R.id.belongList) as ListView
        belongList.emptyView = view.findViewById(R.id.emptyMemberList)
        okBt = view.findViewById<View>(R.id.closeBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)
        setInfo(item, memberByBelong)
        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item: Group, memberByBelong: ArrayList<Member>) {
        val adapter = SmallMBListAdapter(c, memberByBelong)

        group.text = item.name
        number.text = "${adapter.count}${c.getString(R.string.people)}"
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