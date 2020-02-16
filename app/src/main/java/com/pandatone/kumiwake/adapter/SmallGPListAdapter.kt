package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.Group
import java.util.*

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class SmallGPListAdapter(private val context: Context, val groupList: ArrayList<Group>) : BaseAdapter() {

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): String {
        return groupList[position].toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        val memberNoTextView: TextView
        var v = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_group, null)
        }

        nameTextView = v!!.findViewById<View>(R.id.groupName) as TextView
        memberNoTextView = v.findViewById<View>(R.id.memberNo) as TextView
        memberNoTextView.visibility = View.VISIBLE
        nameTextView.text = groupList[position].name
        memberNoTextView.text = groupList[position].belongNo.toString() + context.getString(R.string.people)

        return v
    }

    companion object {

        fun setRowHeight(listView: ListView, listAdp: SmallGPListAdapter) {
            var totalHeight = 0

            for (j in 0 until listAdp.count) {
                val item = listAdp.getView(j, null, listView)
                item.measure(0, 0)
                totalHeight += item.measuredHeight
            }

            listView.layoutParams.height = totalHeight + listView.dividerHeight * (listAdp.count - 1)
            listView.requestLayout()
        }

    }
}
