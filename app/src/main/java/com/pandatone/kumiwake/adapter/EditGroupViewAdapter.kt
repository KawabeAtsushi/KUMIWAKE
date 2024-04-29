package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class EditGroupViewAdapter(
    val context: Context,
    val groupList: List<Group>,
    private val groupListView: ListView,
) : BaseAdapter() {
    private var beforeNo: Int = 0
    private var afterNo: Int = 0
    private var autoChange: Boolean = false
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
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameEditText: EditText
        val belongNoEditText: EditText
        val leader: TextView
        var v = convertView
        val group = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
        }

        nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
        nameEditText.hint = context.getString(R.string.group) + " " + (position + 1).toString()
        nameEditText.isFocusable = true
        nameEditText.isFocusableInTouchMode = true
        belongNoEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
        belongNoEditText.isFocusable = true
        belongNoEditText.isFocusableInTouchMode = true
        leader = v.findViewById<View>(R.id.leader) as TextView
        if (group.name != "") {
            nameEditText.setText(group.name)
        }
        belongNoEditText.setText(group.belongNo.toString())
        leader.text =
            "${context.getString(R.string.leader)} : ${context.getString(R.string.nothing)}"
        nameEditTextList[position] = nameEditText
        belongEditTextList[position] = belongNoEditText

        belongNoEditText.setOnFocusChangeListener { _, hasFocus ->
            //hasFocus: 入力開始 -> true,移動 -> false
            if (hasFocus) {
                setFocus = position
            } else {
                outFocus = position
            }
            if (setFocus != outFocus) {//別のところにフォーカスが移ったら
                val beforeEditText = belongEditTextList[outFocus]
                if (beforeEditText?.text.toString() == "" || beforeEditText?.text.toString() == "-") {
                    beforeEditText?.setText("0")
                }
            }
        }

        //所属人数の入力変更の際に呼ばれる処理
        belongNoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                beforeNo = if (text.toString() != "" && text.toString() != "-") {
                    Integer.parseInt(text.toString())
                } else {
                    0
                }
            }

            override fun afterTextChanged(text: Editable?) {

                if (text.toString() != "" && text.toString() != "-") {
                    afterNo = Integer.parseInt(text.toString())

                    if (afterNo < 0) {
                        belongNoEditText.setTextColor(Color.RED)
                    } else {
                        belongNoEditText.setTextColor(PublicMethods.getColor(context, R.color.gray))
                    }
                } else {
                    afterNo = 0
                }

                val addNo = beforeNo - afterNo

                if (!autoChange) {//changeBelongNo()での変更によって呼ばれないようにする
                    autoChange = true
                    changeBelongNo(position, addNo)
                    autoChange = false
                }

                if (text.toString() == "-") {
                    belongNoEditText.setText("")
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )

        return v
    }

    //人数配分の自動調整
    private fun changeBelongNo(position: Int, addNo: Int) {
        val et: EditText = if (position == groupListView.count - 1) {
            groupListView.getChildAt(0).findViewById<View>(R.id.editTheNumberOfMember) as EditText
        } else {
            groupListView.getChildAt(position + 1)
                .findViewById<View>(R.id.editTheNumberOfMember) as EditText
        }
        var nowNo = 0
        val newNo: Int

        if (et.text.toString().isNotEmpty()) {
            nowNo = Integer.parseInt(et.text.toString())
        }
        newNo = nowNo + addNo
        et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
        et.setText(newNo.toString())
        if (newNo < 0) {
            et.setTextColor(Color.RED)
        } else {
            et.setTextColor(PublicMethods.getColor(context, R.color.gray))
        }
    }

    //ListViewのEditTextのマップ
    @SuppressLint("UseSparseArrays")
    private val nameEditTextList = HashMap<Int, EditText>()

    @SuppressLint("UseSparseArrays")
    private val belongEditTextList = HashMap<Int, EditText>()

    //i番目のグループ名を取得
    fun getGroupName(position: Int): String {
        var groupName = context.getString(R.string.group) + " " + (position + 1).toString()
        val groupNameEditText = nameEditTextList[position]
        if (groupNameEditText!!.text.isNotEmpty()) {
            groupName = groupNameEditText.text.toString()
        }
        return groupName
    }

    //i番目のグループの所属人数を取得
    fun getMemberNo(position: Int): Int {
        var memberNo = 0
        val memberNoEditText = belongEditTextList[position]
        if (memberNoEditText!!.text.toString().isNotEmpty()) {
            memberNo = Integer.parseInt(memberNoEditText.text.toString())
        }
        return memberNo
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

