package com.pandatone.kumiwake.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.GroupListAdapter
import com.pandatone.kumiwake.member.Sort
import java.util.*

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class GPListViewAdapter(private val context: Context, nameList: ArrayList<GroupListAdapter.Group>) : BaseAdapter() {

    private var listElements: ArrayList<GroupListAdapter.Group> = nameList

    override fun getCount(): Int {
        return listElements.size
    }

    override fun getItem(position: Int): String {
        return listElements[position].toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        val memberNoTextView: TextView
        var v = convertView
        val listItem = getItem(position)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_group, null)
        }

        nameTextView = v!!.findViewById<View>(R.id.groupName) as TextView
        memberNoTextView = v.findViewById<View>(R.id.memberNo) as TextView
        memberNoTextView.visibility = View.VISIBLE
        nameTextView.text = listElements[position].group
        memberNoTextView.text = listElements[position].belongNo.toString() + Sort.name_getContext()!!.getText(R.string.person)

        return v
    }

    companion object {

        fun setRowHeight(listView: ListView, listAdapter: GPListViewAdapter) {
            var totalHeight = 0

            for (j in 0 until listAdapter.count) {
                val item = listAdapter.getView(j, null, listView)
                item.measure(0, 0)
                totalHeight += item.measuredHeight
            }

            listView.layoutParams.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
            listView.requestLayout()
        }

    }
}

