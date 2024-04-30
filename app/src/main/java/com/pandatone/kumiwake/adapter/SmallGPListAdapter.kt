package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class SmallGPListAdapter(
    private val context: Context,
    val groupList: ArrayList<Group>,
    private val roleMode: Boolean = false,
) : BaseAdapter() {

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

        if (roleMode) {
            val icon = v?.findViewById<ImageView>(R.id.mini_group_icon)
            icon?.setImageResource(R.drawable.ic_star_circle_24dp)
            if (groupList[position].id == 1) {
                icon?.visibility = View.GONE
            }
        }

        nameTextView = v!!.findViewById<View>(R.id.groupName) as TextView
        memberNoTextView = v.findViewById<View>(R.id.memberNo) as TextView
        memberNoTextView.visibility = View.VISIBLE
        nameTextView.text = groupList[position].name
        memberNoTextView.text =
            groupList[position].belongNo.toString() + context.getString(R.string.people)

        return v
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

