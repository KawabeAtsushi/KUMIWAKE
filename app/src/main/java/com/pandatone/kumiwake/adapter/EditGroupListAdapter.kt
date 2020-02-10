package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.kumiwake.KumiwakeCustom
import com.pandatone.kumiwake.member.Group
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class EditGroupListAdapter(private val context: Context, private val groupList: List<Group>, private val scrollView: ScrollView) : BaseAdapter() {
    private var beforeNo: Int = 0
    private var afterNo: Int = 0

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
        val numberOfMemberEditText: EditText
        val leader: TextView
        var v = convertView
        val group = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
        }

        nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
        numberOfMemberEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
        leader = v.findViewById<View>(R.id.leader) as TextView
        nameEditText.setText(group.name)
        numberOfMemberEditText.setText(group.belongNo.toString())
        leader.text = "${context.getString(R.string.leader)} : ${context.getString(R.string.nothing)}"
        groupNameView[position] = nameEditText
        memberNoView[position] = numberOfMemberEditText
        numberOfMemberEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (numberOfMemberEditText.text.toString() != "" && numberOfMemberEditText.text.toString() != "-") {
                    scrollView.setOnTouchListener { _, _ -> false }
                    beforeNo = Integer.parseInt(numberOfMemberEditText.text.toString())
                } else {
                    scrollView.setOnTouchListener { _, _ -> true }
                }
                numberOfMemberEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
            } else {

                if (numberOfMemberEditText.text.toString() != "" && numberOfMemberEditText.text.toString() != "-") {
                    scrollView.setOnTouchListener { _, _ -> false }

                    afterNo = Integer.parseInt(numberOfMemberEditText.text.toString())

                    val addNo = beforeNo - afterNo
                    KumiwakeCustom().changeBelongNo(position, addNo)
                    if (afterNo < 0) {
                        numberOfMemberEditText.setTextColor(Color.RED)
                    } else {
                        numberOfMemberEditText.setTextColor(Color.BLACK)
                    }
                } else {
                    scrollView.setOnTouchListener { _, _ -> true }
                    afterNo = 0
                }
            }
        }

        return v
    }

    companion object {
        @SuppressLint("UseSparseArrays")
        private val groupNameView = HashMap<Int, EditText>()
        @SuppressLint("UseSparseArrays")
        private val memberNoView = HashMap<Int, EditText>()

        fun getGroupName(position: Int): String {
            var groupName = R.string.nothing.toString()
            val groupNameEditText = groupNameView[position]
            if (groupNameEditText!!.text != null) {
                groupName = groupNameEditText.text.toString()
            }
            return groupName
        }

        fun getMemberNo(position: Int): Int {
            var memberNo = 0
            val memberNoEditText = memberNoView[position]
            if (memberNoEditText!!.text.toString().isNotEmpty()) {
                memberNo = Integer.parseInt(memberNoEditText.text.toString())
            }
            return memberNo
        }


        fun setRowHeight(listView: ListView, listAdp: EditGroupListAdapter) {
            var totalHeight = 0

            for (j in 0 until listAdp.count) {
                val item = listAdp.getView(j, null, listView)
                item?.measure(0, 0)
                totalHeight += item!!.measuredHeight
            }

            listView.layoutParams.height = totalHeight + listView.dividerHeight * (listAdp.count - 1)
            listView.requestLayout()
        }
    }

}

