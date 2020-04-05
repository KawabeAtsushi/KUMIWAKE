package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.kumiwake.function.KumiwakeMethods
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.ui.members.FragmentGroupMain
import java.text.SimpleDateFormat

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
class HistoryInfo(val c: Activity) {
    private lateinit var history: TextView
    private lateinit var date: TextView
    private lateinit var result: LinearLayout
    private lateinit var okBt: Button

    fun infoDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.history_info, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

        history = view.findViewById<View>(R.id.infoName) as TextView
        date = view.findViewById<View>(R.id.infoDate) as TextView
        result = view.findViewById<View>(R.id.result) as LinearLayout
        okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.history)
        builder.setView(view)

        setInfo(item)
        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item: History) {
        history.text = item.name

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dt = df.parse(item.time)
        val jpFormat = SimpleDateFormat("yyyy年MM月dd日 HH時mm分ss秒")
        date.text = jpFormat.format(dt)
        val resultArray = HistoryMethods.stringToResultArray(c,item.result)
        //setView
        for((i, result) in resultArray.withIndex()){
            addResultView(result,i)
        }
    }

    private fun addResultView(resultArray: ArrayList<Member>, i: Int) {
        val groupName: TextView
        val arrayList: ListView
        val v = c.layoutInflater.inflate(R.layout.result_parts, null)
        result.addView(v)
        groupName = v.findViewById<View>(R.id.result_group) as TextView
        val text = c.getString(R.string.group) + (i+1).toString()
        groupName.text = text
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = SmallMBListAdapter(c, resultArray, false, showLeaderNo = false)
        arrayList.adapter = adapter
        v.layoutParams = PublicMethods.setMargin(c,4, 6, 4, 6)
        v.background = c.getDrawable(R.drawable.group_review_layout)
        adapter.setRowHeight(arrayList)
    }
}