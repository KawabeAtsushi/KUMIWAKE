package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.function.Member

/**
 * Created by atsushi_2 on 2016/04/17.
 */
class HistoryInfo(val c: Activity) {
    private lateinit var history: TextView
    private lateinit var date: TextView
    private lateinit var result: LinearLayout
    private lateinit var okBt: Button
    private lateinit var goToBt: MaterialButton
    private lateinit var editNameBt: ImageButton
    private var resultArray: ArrayList<ArrayList<Member>> = ArrayList()
    private var groupNameArray: Array<String> = emptyArray()

    //履歴クリック時の情報表示ダイアログ
    fun infoDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.history_info,
            c.findViewById<View>(R.id.info_layout) as ViewGroup?
        )

        history = view.findViewById(R.id.infoName)
        date = view.findViewById(R.id.infoDate)
        result = view.findViewById<View>(R.id.result) as LinearLayout
        okBt = view.findViewById(R.id.closeBt)
        goToBt = view.findViewById(R.id.goToBt)
        editNameBt = view.findViewById(R.id.edit_name)

        builder.setTitle(R.string.history)
        builder.setView(view)

        setInfo(item)
        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
        goToBt.setOnClickListener { ReKumiwake(c, resultArray, groupNameArray).selectModeDialog() }
        editNameBt.setOnClickListener { editTextDialog(item) }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item: History) {
        history.text = HistoryMethods.changeDateFormat(item.name)
        date.text = HistoryMethods.changeDateFormat(item.time)
        resultArray = HistoryMethods.stringToResultArray(c, item.result)
        groupNameArray = HistoryMethods.stringToResultGroupArray(item.resultGp)
        //setView
        for (i in resultArray.indices) {
            try {
                addResultView(i, groupNameArray[i])
            } catch (e: ArrayIndexOutOfBoundsException) { //前のバージョンだとグループ名ないので
                addResultView(i, c.getString(R.string.group) + (i + 1).toString())
            }
        }
    }

    //結果表示
    private fun addResultView(i: Int, groupName: String) {
        val groupTextView: TextView
        val arrayList: ListView
        val v = c.layoutInflater.inflate(R.layout.result_parts, null)
        result.addView(v)
        groupTextView = v.findViewById<View>(R.id.result_group) as TextView
        groupTextView.text = groupName
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = SmallMBListAdapter(c, resultArray[i])
        arrayList.adapter = adapter
        v.layoutParams = PublicMethods.setMargin(c, 4, 6, 4, 6)
        v.background = ContextCompat.getDrawable(c, R.drawable.group_review_layout)
        adapter.setRowHeight(arrayList)
    }

    //名前変更ダイアログ
    fun editTextDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.edittext_dialog_layout,
            c.findViewById<View>(R.id.info_layout) as ViewGroup?
        )

        val editText = view.findViewById<EditText>(R.id.edit_name)
        editText.hint = HistoryMethods.changeDateFormat(item.name)
        builder.setTitle(R.string.edit_title)
            .setView(view)
            .setPositiveButton(R.string.change) { _, _ ->
                val newName = editText.text.toString()
                if (newName != "") {
                    HistoryAdapter(c).updateHistoryState(item, editText.text.toString(), false)
                    if (::history.isInitialized) {
                        history.text = newName
                    }
                    FragmentHistory().loadName()
                    FragmentKeeps().loadName()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
}