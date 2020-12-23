package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class EditOthersViewAdapter(val context: Context, val groupList: List<Group>, private val totalCountTextView: TextView, private val drawingMode: Boolean) : BaseAdapter() {

    private val name = if (drawingMode) context.getString(R.string.ticket) else context.getString(R.string.role)
    private val totalStrInit = if (drawingMode) {
        context.getString(R.string.ticket_number)
    } else {
        context.getString(R.string.assigned)
    }
    private val totalStrUnit = if (drawingMode) {
        context.getString(R.string.ticket_unit)
    } else {
        context.getString(R.string.people)
    }

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): Any {
        return groupList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameEditText: EditText
        val numberEditText: EditText
        var v = convertView
        val group = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
            v.findViewById<TextView>(R.id.personTex).text = totalStrUnit
        }

        nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
        nameEditText.hint = name + " " + (position + 1).toString()
        nameEditText.isFocusable = true
        nameEditText.isFocusableInTouchMode = true
        numberEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
        numberEditText.hint = "1"
        numberEditText.isFocusable = true
        numberEditText.isFocusableInTouchMode = true
        if (group.name != "") {
            nameEditText.setText(group.name)
        }
        if (group.belongNo >= 0) {
            numberEditText.setText(group.belongNo.toString())
        }
        v.findViewById<View>(R.id.leader).visibility = View.GONE
        val icon = v.findViewById<ImageView>(R.id.rowIconGroup)
        icon.setImageResource(R.drawable.ic_star_circle_24dp)
        if (drawingMode) {
            icon.setOnClickListener { DialogWarehouse(null).colorPickerDialog(context, position, icon) }
        }

        nameEditTextList[position] = nameEditText
        numberEditTextList[position] = numberEditText

        //数の入力変更の際に呼ばれる処理
        numberEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(text: Editable?) {
                setTotalCount()
            }

            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )

        return v
    }

    fun setTotalCount() {
        val totalStr = "$totalStrInit ${countTotal()}${totalStrUnit}"
        totalCountTextView.text = totalStr
    }

    //countTotalAssignedMember
    private fun countTotal(): Int {
        var totalNo = 0
        for (i in 0 until numberEditTextList.size) {
            totalNo += getNumber(i, false)
        }
        return totalNo
    }

    //ListViewのEditTextのマップ
    @SuppressLint("UseSparseArrays")
    private val nameEditTextList = HashMap<Int, EditText>()

    @SuppressLint("UseSparseArrays")
    private val numberEditTextList = HashMap<Int, EditText>()

    //i番目の名前を取得
    fun getName(position: Int, allowEmpty: Boolean): String {
        var name = if (allowEmpty) "" else name + " " + (position + 1).toString()
        val nameEditText = nameEditTextList[position]
        if (nameEditText!!.text.isNotEmpty()) {
            name = nameEditText.text.toString()
        }
        return name
    }

    //i番目の数を取得
    fun getNumber(position: Int, allowEmpty: Boolean): Int {
        var number = if (allowEmpty) -1 else 1
        val numberEditText = numberEditTextList[position]
        if (numberEditText!!.text.toString().isNotEmpty()) {
            number = Integer.parseInt(numberEditText.text.toString())
        }
        return number
    }


    fun setRowHeight(listView: ListView) {
        var totalHeight = 0

        for (j in 0 until this.count) {
            val item = this.getView(j, null, listView)
            item.measure(0, 0)
            totalHeight += item.measuredHeight
        }

        listView.layoutParams.height = totalHeight + listView.dividerHeight * (this.count - 1)
        listView.requestLayout()
    }

}

