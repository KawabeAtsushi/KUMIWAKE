package com.pandatone.kumiwake.adapter

import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.kumiwake.KumiwakeCustom
import com.pandatone.kumiwake.kumiwake.MainActivity
import com.pandatone.kumiwake.member.GroupListAdapter
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class EditGroupListAdapter(private val context: Context, private val groupList: List<GroupListAdapter.Group>) : BaseAdapter() {
    internal var beforeNo: Int = 0
    internal var afterNo: Int = 0

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): Any {
        return groupList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val nameEditText: EditText
        val numberOfMemberEditText: EditText
        val leader: TextView
        var v = convertView
        val listItem = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
        }

        if (listItem != null) {
            nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
            numberOfMemberEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
            leader = v.findViewById<View>(R.id.leader) as TextView
            nameEditText.setText(listItem.group)
            numberOfMemberEditText.setText(listItem.belongNo.toString())
            leader.text = "${MainActivity.context!!.getText(R.string.leader)} : ${MainActivity.context!!.getText(R.string.nothing)}"
            groupNameView[position] = nameEditText
            memberNoView[position] = numberOfMemberEditText
            numberOfMemberEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    if (numberOfMemberEditText.text.toString() != "" && numberOfMemberEditText.text.toString() != "-") {
                        KumiwakeCustom.scrollView.setOnTouchListener { v, event -> false }
                        beforeNo = Integer.parseInt(numberOfMemberEditText.text.toString())
                    } else {
                        KumiwakeCustom.scrollView.setOnTouchListener { v, event -> true }
                    }
                    numberOfMemberEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
                } else {

                    if (numberOfMemberEditText.text.toString() != "" && numberOfMemberEditText.text.toString() != "-") {
                        KumiwakeCustom.scrollView.setOnTouchListener { v, event -> false }

                        afterNo = Integer.parseInt(numberOfMemberEditText.text.toString())

                        val addNo = beforeNo - afterNo
                        KumiwakeCustom.changeBelongNo(position, addNo)
                        if (afterNo < 0) {
                            numberOfMemberEditText.setTextColor(Color.RED)
                        } else {
                            numberOfMemberEditText.setTextColor(Color.BLACK)
                        }
                    } else {
                        KumiwakeCustom.scrollView.setOnTouchListener { v, event -> true }
                        afterNo = 0
                    }
                }
            }

        }

        return v
    }

    companion object {
        private val groupNameView = HashMap<Int, EditText>()
        private val memberNoView = HashMap<Int, EditText>()

        fun getGroupName(position: Int): String {
            var groupName = MainActivity.context!!.getText(R.string.nothing).toString()
            val groupNameEditText = groupNameView[position]
            if (groupNameEditText!!.text != null) {
                groupName = groupNameEditText.text.toString()
            }
            return groupName
        }

        fun getMemberNo(position: Int): Int {
            var memberNo = 0
            val memberNoEditText = memberNoView[position]
            if (memberNoEditText!!.text.toString().length > 0) {
                memberNo = Integer.parseInt(memberNoEditText.text.toString())
            }
            return memberNo
        }


        fun setRowHeight(listView: ListView, listAdapter: EditGroupListAdapter) {
            var totalHeight = 0

            for (j in 0 until listAdapter.count) {
                val item = listAdapter.getView(j, null, listView)
                item?.measure(0, 0)
                totalHeight += item!!.measuredHeight
            }

            listView.layoutParams.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
            listView.requestLayout()
        }
    }

}

