package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.ModeKeys
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.member.function.Member

/**
 * Created by atsushi_2 on 2024/04/28.
 */
class ReKumiwake(
    val c: Activity,
    private val resultArray: ArrayList<ArrayList<Member>>,
    private val groupNameArray: Array<String>
) {
    private var newMemberArray: ArrayList<Member> = ArrayList()


    //メンバーを利用選択ダイアログ
    fun selectModeDialog() {
        val builder = AlertDialog.Builder(c)
        val inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.select_mode_dialog_layout,
            c.findViewById<View>(R.id.info_layout) as ViewGroup?
        )

        setupGroupDropDown(view)

        val duplicateCheckBox = view.findViewById<CheckBox>(R.id.duplicate_check)

        // 組分け
        val kumiwakeUnit: View = view.findViewById(R.id.kumiwake_select_button)
        val kumiwakeButton: ImageButton = kumiwakeUnit.findViewById(R.id.icon_button)
        (kumiwakeUnit.findViewById<TextView>(R.id.button_text)!!).setText(R.string.kumiwake)
        kumiwakeButton.backgroundTintList =
            ColorStateList.valueOf(PublicMethods.getColor(c, R.color.red_title))
        kumiwakeButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kumiwakeButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Kumiwake.key
            PublicMethods.setStatus(this.c, Theme.Kumiwake.primaryColor)
            goTo(duplicateCheckBox.isChecked)
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeHistory.key)
        }

        //　席決め
        val sekigimeUnit: View = view.findViewById<Button>(R.id.sekigime_select_button)
        val sekigimeButton: ImageButton = sekigimeUnit.findViewById(R.id.icon_button)
        (sekigimeUnit.findViewById<TextView>(R.id.button_text)!!).setText(R.string.sekigime)
        sekigimeButton.backgroundTintList =
            ColorStateList.valueOf(PublicMethods.getColor(c, R.color.green_title))
        sekigimeButton.setImageResource(R.drawable.ic_sekigime_24px)
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

    //メンバーを利用して組み分け/席決め
    private fun goTo(notDuplicate: Boolean) {
        StatusHolder.normalMode = true
        NormalMode.memberArray = newMemberArray
        startActivity(c, Intent(c, NormalMode::class.java), null)
        StatusHolder.notDuplicate = notDuplicate
        if (notDuplicate) {
            HistoryMethods.historyResultArray = resultArray
        }
    }

    private fun setNewMemberArray(position: Int) {
        var memberArray = ArrayList<Member>()
        if (position == 0) {
            resultArray.forEach { result ->
                memberArray.addAll(result)
            }
        } else {
            memberArray = resultArray[position - 1]
        }
        memberArray.removeAll { it.id == -1 }
        newMemberArray = memberArray
    }

    private fun setupGroupDropDown(view: View) {
        val groupDropdown = view.findViewById<View>(R.id.group_dropdown) as AutoCompleteTextView
        groupDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            val manager = c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(groupDropdown.windowToken, 0)
        }
        val adapter = ArrayAdapter<String>(c, R.layout.dropdown_item_layout)
        val list = ArrayList<String>()
        list.add(c.getString(R.string.all_member))
        for (group in groupNameArray) {
            list.add(group)
        }
        setNewMemberArray(0)
        adapter.addAll(list)
        groupDropdown.setAdapter(adapter)
        groupDropdown.hint = list[0]
        groupDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            setNewMemberArray(position)
        }
    }
}