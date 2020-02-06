package com.pandatone.kumiwake.ui.members

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter

class MembersMenuAction(val context: Context) {

    val dbAdapter = MemberListAdapter(context)

    private fun filter(layout: View, spinner: Spinner, clear: Boolean) {

        val belong: String = spinner.selectedItem as String
        var belongId = ""
        val sexGroup = layout.findViewById<View>(R.id.sexGroup) as RadioGroup
        val sexButton = layout.findViewById<View>(sexGroup.checkedRadioButtonId) as RadioButton
        val errorAgeRange = layout.findViewById<View>(R.id.error_age_range) as TextView
        var sex = sexButton.text as String
        val maxAge1 = layout.findViewById<View>(R.id.max_age) as TextInputEditText
        val minAge1 = layout.findViewById<View>(R.id.min_age) as TextInputEditText

        if (clear) {
            spinner.setSelection(0)
            sexGroup.check(R.id.noSelect)
            maxAge1.setText("")
            minAge1.setText("")
        }

        val maxAge: Int = if (maxAge1.text.toString() != "") {
            getValue(maxAge1)
        } else {
            1000
        }
        val minAge: Int = if (minAge1.text.toString() != "") {
            getValue(minAge1)
        } else {
            0
        }

        if (maxAge < minAge) {
            errorAgeRange.visibility = View.VISIBLE
            errorAgeRange.setText(R.string.range_error)
        } else {
            errorAgeRange.visibility = View.GONE

            if (sex == context.getString(R.string.all)) {
                sex = ""
            }

            if (belong == context.getString(R.string.no_selected)) {
                belongId = ""
            } else {
                for (listItem in groupList) {
                    if (belong == listItem.group) {
                        belongId = listItem.id.toString() + ","
                    }
                }
            }

            dbAdapter.filterName(sex, minAge, maxAge, belongId)
        }
    }

    private fun filtering(builder: androidx.appcompat.app.AlertDialog.Builder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.filter_member, R.id.filter_member as ViewGroup)
        val belongSpinner = layout.findViewById<View>(R.id.filter_belong_spinner) as Spinner
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
        val list = ArrayList<String>() // 新インスタンスを生成
        list.add(context.getString(R.string.no_selected))

        for (listItem in groupList) {
            list.add(listItem.group)
        }

        adapter.addAll(list)
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)
        belongSpinner.adapter = adapter
        builder.setTitle(context.getText(R.string.filtering))
        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.setNeutralButton(R.string.clear, null)
        // back keyを使用不可に設定
        builder.setCancelable(false)
        val dialog2 = builder.create()
        dialog2.show()

        val okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            NameListAdapter.nowSort = MemberListAdapter.MB_ID
            NameListAdapter.sortType = "ASC"
            dbAdapter.sortNames(NameListAdapter.nowSort, NameListAdapter.sortType)
            filter(layout, belongSpinner, false)
            listAdp.notifyDataSetChanged()
            dialog2.dismiss()
        }

        val clearBtn = dialog2.getButton(AlertDialog.BUTTON_NEUTRAL)
        clearBtn.setOnClickListener {
            filter(layout, belongSpinner, true)
        }
    }

    private fun getValue(toText: EditText): Int {
        val text = toText.text.toString()
        var a = 0
        if (text.isNotEmpty()) {
            a = Integer.parseInt(text)
        }
        return a
    }
}