package com.pandatone.kumiwake.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.Name
import com.pandatone.kumiwake.member.Sort
import java.util.*

class NameListAdapter(private val context: Context, private val nameList: List<Name>) : BaseAdapter() {
    internal var params: LinearLayout.LayoutParams

    init {
        this.params = LinearLayout.LayoutParams(0, 1)
    }

    override fun getCount(): Int {
        return nameList.size
    }

    override fun getItem(position: Int): Name? {
        return nameList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        return getItem(position)!!.sex != "initial"
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val nameTextView: TextView
        val listItem = getItem(position)
        var v: View = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (isEnabled(position)) {

            if (v == null || v.tag !== "member") {
                v = inflater.inflate(R.layout.row_member, null)
            }

            if (mSelection[position] != null) {
                v.setBackgroundColor(Sort.name_getContext()!!.resources.getColor(R.color.checked_list))
            } else {
                v = inflater.inflate(R.layout.row_member, null)
            }
            setSexIcon(v, position)

            if (listItem != null) {
                nameTextView = v.findViewById<View>(R.id.memberName) as TextView
                nameTextView.text = listItem.name
            }
        } else {
            if (v == null || v.tag !== "initial") {
                v = inflater.inflate(R.layout.row_initial, null)
            }
            if (nowData != null) {
                addInitial(position, v)
            }
        }

        return v
    }

    fun setSexIcon(v: View, position: Int) {
        val memberIcon: ImageView
        memberIcon = v.findViewById<View>(R.id.memberIcon) as ImageView
        if (getItem(position)!!.sex == "男") {
            memberIcon.setImageResource(R.drawable.member_img)
        } else {
            memberIcon.setColorFilter(Sort.name_getContext()!!.resources.getColor(R.color.woman))
        }
    }

    fun addInitial(position: Int, v: View) {
        val nowItem = getItem(position)

        when (nowSort) {
            "NAME" -> nowData = nowItem!!.name_read
            "AGE" -> nowData = nowItem!!.age.toString()
            "GRADE" -> nowData = nowItem!!.grade.toString()
        }

        if (position != 0) {
            val preItem = getItem(position - 2)

            when (nowSort) {
                "NAME" -> preData = preItem!!.name_read
                "AGE" -> preData = preItem!!.age.toString()
                "GRADE" -> preData = preItem!!.grade.toString()
            }
        }

        if ((nowData != preData || position == 0) && nowSort != "ID") {
            val initialText = v.findViewById<View>(R.id.initial) as TextView
            initialText.text = nowData
        } else {
            v.layoutParams = params
        }
    }

    fun setNewSelection(position: Int, value: Boolean) {
        mSelection[position] = value
        notifyDataSetChanged()
    }

    fun removeSelection(position: Int) {
        mSelection.remove(position)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        mSelection = HashMap()
        notifyDataSetChanged()
    }

    companion object {
        private var nowData: String? = "ￚ no data ￚ"
        private var preData = "ￚ no data ￚ"
        var nowSort = "ID"
        private var mSelection = HashMap<Int, Boolean>()

        fun isPositionChecked(position: Int): Boolean {
            val result = mSelection[position]
            return result ?: false
        }
    }
}
