package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.button.MaterialButton
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.member.function.Member


/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
class HistoryInfo(val c: Activity) {
    private lateinit var history: TextView
    private lateinit var date: TextView
    private lateinit var result: LinearLayout
    private lateinit var okBt: Button
    private lateinit var goToBt: MaterialButton
    private lateinit var editNameBt: ImageButton
    private var resultArray: ArrayList<ArrayList<Member>> = ArrayList()

    fun infoDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.history_info, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

        history = view.findViewById<TextView>(R.id.infoName)
        date = view.findViewById<TextView>(R.id.infoDate)
        result = view.findViewById<View>(R.id.result) as LinearLayout
        okBt = view.findViewById<Button>(R.id.okBt)
        goToBt = view.findViewById<MaterialButton>(R.id.goToBt)
        editNameBt = view.findViewById<ImageButton>(R.id.edit_name)

        if (StatusHolder.sekigime) {
            goToBt.text = c.getString(R.string.sekigime_from_history)
            goToBt.icon = c.getDrawable(R.drawable.ic_sekigime_24px)
            goToBt.setBackgroundColor(PublicMethods.getColor(c, R.color.green_title))
        }

        builder.setTitle(R.string.history)
        builder.setView(view)

        setInfo(item)
        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
        goToBt.setOnClickListener { goTo() }
        editNameBt.setOnClickListener { editTextDialog(item) }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item: History) {
        history.text = HistoryMethods.changeDateFormat(item.name)
        date.text = HistoryMethods.changeDateFormat(item.time)
        resultArray = HistoryMethods.stringToResultArray(c, item.result)
        //setView
        for (i in 0 until resultArray.size) {
            addResultView(i)
        }
    }

    private fun addResultView(i: Int) {
        val groupName: TextView
        val arrayList: ListView
        val v = c.layoutInflater.inflate(R.layout.result_parts, null)
        result.addView(v)
        groupName = v.findViewById<View>(R.id.result_group) as TextView
        val text = c.getString(R.string.group) + (i + 1).toString()
        groupName.text = text
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = SmallMBListAdapter(c, resultArray[i])
        arrayList.adapter = adapter
        v.layoutParams = PublicMethods.setMargin(c, 4, 6, 4, 6)
        v.background = c.getDrawable(R.drawable.group_review_layout)
        adapter.setRowHeight(arrayList)
    }

    private fun goTo() {
        StatusHolder.normalMode = true
        val memberArray = ArrayList<Member>()
        resultArray.forEach { result ->
            memberArray.addAll(result)
        }
        memberArray.removeAll { it.id == -1 }
        NormalMode.memberArray = memberArray
        startActivity(c, Intent(c, NormalMode::class.java), null)
    }

    private fun editTextDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edittext_dialog_layout, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

        val editText = view.findViewById<EditText>(R.id.edit_name)
        editText.hint = HistoryMethods.changeDateFormat(item.name)
        builder.setTitle(R.string.edit_title)
                .setView(view)
                .setPositiveButton(R.string.change) { dialog, which ->
                    val newName = editText.text.toString()
                    if (newName != "") {
                        HistoryAdapter(c).updateHistoryState(item, editText.text.toString(), false)
                        history.text = newName
                        FragmentHistory().loadName()
                        FragmentKeeps().loadName()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }
}