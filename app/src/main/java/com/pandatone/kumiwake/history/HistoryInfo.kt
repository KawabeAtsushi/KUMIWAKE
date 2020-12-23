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
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.button.MaterialButton
import com.pandatone.kumiwake.*
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

    //履歴クリック時の情報表示ダイアログ
    fun infoDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.history_info, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

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
        goToBt.setOnClickListener { selectModeDialog() }
        editNameBt.setOnClickListener { editTextDialog(item) }
    }

    @SuppressLint("SetTextI18n")
    fun setInfo(item: History) {
        history.text = HistoryMethods.changeDateFormat(item.name)
        date.text = HistoryMethods.changeDateFormat(item.time)
        resultArray = HistoryMethods.stringToResultArray(c, item.result)
        val groupNameArray = HistoryMethods.stringToResultGroupArray(item.resultGp)
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
        v.background = ContextCompat.getDrawable(c,R.drawable.group_review_layout)
        adapter.setRowHeight(arrayList)
    }

    //メンバーを利用して組み分け/席決め
    private fun goTo(notDuplicate: Boolean) {
        StatusHolder.normalMode = true
        val memberArray = ArrayList<Member>()
        resultArray.forEach { result ->
            memberArray.addAll(result)
        }
        memberArray.removeAll { it.id == -1 }
        NormalMode.memberArray = memberArray
        startActivity(c, Intent(c, NormalMode::class.java), null)
        StatusHolder.notDuplicate = notDuplicate
        if (notDuplicate) {
            HistoryMethods.historyResultArray = resultArray
        }
    }

    //メンバーを利用選択ダイアログ
    private fun selectModeDialog() {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.select_mode_dialog_layout, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

        val kumiwakeButton = view.findViewById<Button>(R.id.kumiwake_select_button)
        val duplicateCheckBox = view.findViewById<CheckBox>(R.id.duplicate_check)
        kumiwakeButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Kumiwake.key
            PublicMethods.setStatus(this.c, Theme.Kumiwake.primaryColor)
            goTo(duplicateCheckBox.isChecked)
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeHistory.key)
        }
        val sekigimeButton = view.findViewById<Button>(R.id.sekigime_select_button)
        sekigimeButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Sekigime.key
            PublicMethods.setStatus(this.c, Theme.Sekigime.primaryColor)
            goTo(duplicateCheckBox.isChecked)
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeHistory.key)
        }
        builder.setTitle(R.string.mode_selection)
                .setView(view)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //名前変更ダイアログ
    fun editTextDialog(item: History) {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edittext_dialog_layout, c.findViewById<View>(R.id.info_layout) as ViewGroup?)

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