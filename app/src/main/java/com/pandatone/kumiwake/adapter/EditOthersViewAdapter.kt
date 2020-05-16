package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class EditOthersViewAdapter(val context: Context, val groupList: List<Group>, private val totalCountTextView: TextView, private val drawingMode: Boolean) : BaseAdapter() {
    private var setFocus = 0
    private var outFocus = 0

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
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val nameEditText: EditText
        val numberEditText: EditText
        var v = convertView
        val group = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
        }

        nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
        nameEditText.isFocusable = true
        nameEditText.isFocusableInTouchMode = true
        numberEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
        numberEditText.isFocusable = true
        numberEditText.isFocusableInTouchMode = true
        nameEditText.setText(group.name)
        numberEditText.setText(group.belongNo.toString())
        v.findViewById<View>(R.id.leader).visibility = View.GONE
        val icon = v.findViewById<ImageView>(R.id.rowIconGroup)
        icon.setImageResource(R.drawable.ic_star_circle_24dp)
        if (drawingMode) {
            icon.setOnClickListener { DialogWarehouse(null).colorPickerDialog(context, position, icon) }
        }
        val totalStrUnit = if (drawingMode) {
            context.getString(R.string.ticket_unit)
        } else {
            context.getString(R.string.people)
        }
        v.findViewById<TextView>(R.id.personTex).text = totalStrUnit

        nameEditTextList[position] = nameEditText
        numberEditTextList[position] = numberEditText

        numberEditText.setOnFocusChangeListener { _, hasFocus ->
            //hasFocus: 入力開始 -> true,移動 -> false
            if (hasFocus) {
                setFocus = position
            } else {
                outFocus = position
            }
            if (setFocus != outFocus) {//別のところにフォーカスが移ったら
                val beforeEditText = numberEditTextList[outFocus]
                if (beforeEditText?.text.toString() == "" || beforeEditText?.text.toString() == "-") {
                    beforeEditText?.setText("0")
                }
            }
        }

        //数の入力変更の際に呼ばれる処理
        numberEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {

                if (text.toString() != "" && text.toString() != "-") {
                    val afterNo = Integer.parseInt(text.toString())

                    if (afterNo < 0) {
                        numberEditText.setTextColor(Color.RED)
                    } else {
                        numberEditText.setTextColor(PublicMethods.getColor(context, R.color.gray))
                    }
                    val totalStrInit = if (drawingMode) {
                        context.getString(R.string.ticket_number)
                    } else {
                        context.getString(R.string.assigned)
                    }
                    val totalStr = "$totalStrInit ${countTotal()}${totalStrUnit}"
                    totalCountTextView.text = totalStr
                }

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )

        return v
    }

    //countTotalAssignedMember
    private fun countTotal(): Int {
        var totalNo = 0
        for (i in 0 until numberEditTextList.size) {
            totalNo += getNumber(i)
        }
        return totalNo
    }

    //ListViewのEditTextのマップ
    @SuppressLint("UseSparseArrays")
    private val nameEditTextList = HashMap<Int, EditText>()

    @SuppressLint("UseSparseArrays")
    private val numberEditTextList = HashMap<Int, EditText>()

    //i番目の名前を取得
    fun getName(position: Int): String {
        var name = R.string.nothing.toString()
        val nameEditText = nameEditTextList[position]
        if (nameEditText!!.text != null) {
            name = nameEditText.text.toString()
        }
        return name
    }

    //i番目の数を取得
    fun getNumber(position: Int): Int {
        var number = 0
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
            item?.measure(0, 0)
            totalHeight += item!!.measuredHeight
        }

        listView.layoutParams.height = totalHeight + listView.dividerHeight * (this.count - 1)
        listView.requestLayout()
    }

}

