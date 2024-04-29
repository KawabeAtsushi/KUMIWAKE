package com.pandatone.kumiwake.member.function

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.MemberFragmentViewAdapter


class Filtering(
    val activity: Activity,
    private val mbAdapter: MemberAdapter,
    private val memberList: ArrayList<Member>,
) {

    private val groupList: ArrayList<Group> = GroupAdapter(activity).getAllGroups()

    //filterダイアログ生成
    fun showFilterDialog(activity: Activity, listAdp: MemberFragmentViewAdapter) {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(
            R.layout.filter_member,
            activity.findViewById<View>(R.id.filter_member) as? ViewGroup
        )
        val belongDropdown =
            layout.findViewById<View>(R.id.filter_belong_dropdown) as AutoCompleteTextView
        belongDropdown.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            val manager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(belongDropdown.windowToken, 0)
        }
        val adapter = ArrayAdapter<String>(activity, R.layout.dropdown_item_layout)
        val list = ArrayList<String>() // 新インスタンスを生成
        list.add(activity.getString(R.string.no_selected))
        for (group in groupList) {
            list.add(group.name)
        }
        adapter.addAll(list)
        belongDropdown.setAdapter(adapter)
        builder.setTitle(activity.getText(R.string.filtering))
        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.setNeutralButton(R.string.clear, null)
        // back keyを使用不可に設定
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()

        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            StatusHolder.mbNowSort = MemberAdapter.MB_ID
            StatusHolder.mbSortType = "ASC"
            mbAdapter.sortNames(StatusHolder.mbNowSort, StatusHolder.mbSortType, memberList)
            filter(layout, belongDropdown, false)
            listAdp.notifyDataSetChanged()
            dialog.dismiss()
        }

        val clearBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        clearBtn.setOnClickListener {
            filter(layout, belongDropdown, true)
        }
    }

    //フィルタリングメソッド
    private fun filter(layout: View, dropdown: AutoCompleteTextView, clear: Boolean) {

        val belong: String = dropdown.text.toString()
        var belongId = ""
        val sexGroup = layout.findViewById<View>(R.id.sexGroup) as RadioGroup
        val sexButton = layout.findViewById<View>(sexGroup.checkedRadioButtonId) as RadioButton
        val errorAgeRange = layout.findViewById<View>(R.id.error_age_range) as TextView
        var sex = sexButton.text as String
        val maxAge1 = layout.findViewById<View>(R.id.max_age) as TextInputEditText
        val minAge1 = layout.findViewById<View>(R.id.min_age) as TextInputEditText

        if (clear) {
            dropdown.setText("")
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
            errorAgeRange.visibility = View.INVISIBLE

            if (sex == activity.getString(R.string.all)) {
                sex = ""
            }

            if (belong == activity.getString(R.string.no_selected)) {
                belongId = ""
            } else {
                groupList.firstOrNull { it.name == belong }
                    ?.let { belongId = it.id.toString() }
            }
            mbAdapter.filterName(sex, minAge, maxAge, belongId, memberList)
        }
    }

    //年齢：editTxt -> int
    private fun getValue(toText: EditText): Int {
        val text = toText.text.toString()
        var a = 0
        if (text.isNotEmpty()) {
            a = Integer.parseInt(text)
        }
        return a
    }
}