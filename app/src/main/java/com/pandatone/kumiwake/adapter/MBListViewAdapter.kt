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
import com.pandatone.kumiwake.kumiwake.KumiwakeCustom
import com.pandatone.kumiwake.member.Member
import java.util.*

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class MBListViewAdapter(private val context: Context, memberList: ArrayList<Member>, private val showStar: Boolean, showLeaderNo: Boolean) : BaseAdapter() {
    private val inflater: LayoutInflater
    private var listElements: ArrayList<Member> = ArrayList()
    private val showLdNo = showLeaderNo

    init {
        listElements = memberList
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }

    override fun getCount(): Int {
        return listElements.size
    }

    override fun getItem(position: Int): String {
        return listElements[position].toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        return showLdNo
    }

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val v = inflater.inflate(R.layout.mini_row_member, null)

        val memberIcon: ImageView = v.findViewById<View>(R.id.memberIcon) as ImageView
        val starIcon: ImageView = v.findViewById<View>(R.id.starIcon) as ImageView
        val leaderNo: TextView = v.findViewById<View>(R.id.leaderNo) as TextView
        val nameTextView = v.findViewById<View>(R.id.memberName) as TextView

        setStarIcon(memberIcon, starIcon, leaderNo, position)
        setSexIcon(memberIcon, position)
        nameTextView.text = listElements[position].name

        return v
    }

    private fun setSexIcon(memberIcon: ImageView, position: Int) {

        if (listElements[position].sex == context.getText(R.string.man)) {
            memberIcon.setImageResource(R.drawable.member_img)
        } else {
            memberIcon.setColorFilter(context.getColor(R.color.woman))
        }
    }

    private fun setStarIcon(memberIcon: ImageView, starIcon: ImageView, leaderNo: TextView, position: Int) {
        val leaderNoList = KumiwakeCustom.leaderNoList
        val id = listElements[position].id

        if (showStar && leaderNoList.contains(id)) {
            memberIcon.visibility = View.GONE
            starIcon.visibility = View.VISIBLE
            leaderNo.visibility = View.GONE

            if (showLdNo) {
                leaderNo.visibility = View.VISIBLE
                leaderNo.text = (leaderNoList.indexOf(id) + 1).toString()
            }

        } else {
            memberIcon.visibility = View.VISIBLE
            starIcon.visibility = View.GONE
            leaderNo.visibility = View.GONE
        }
    }


    companion object {

        fun setRowHeight(listView: ListView, listAdp: MBListViewAdapter) {
            var totalHeight = 40

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

class ViewHolder(var memberIcon: ImageView, var starIcon: ImageView, var leaderNo: TextView, var nameTextView: TextView)


