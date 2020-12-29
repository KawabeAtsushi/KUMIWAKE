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
import androidx.core.text.HtmlCompat
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Member

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class SmallMBListAdapter(private val context: Context, memberList: ArrayList<Member>, private val leaderArray: ArrayList<Member?> = ArrayList(), val showLeaderNo: Boolean = false, val nameIsSpanned: Boolean = false) : BaseAdapter() {
    private val inflater: LayoutInflater
    private var listElements: ArrayList<Member> = ArrayList()

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
        return showLeaderNo
    }

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val v = inflater.inflate(R.layout.mini_row_member, null)

        val memberIcon: ImageView = v.findViewById(R.id.memberIcon)
        val leaderNo: TextView = v.findViewById(R.id.leaderNo)
        val nameTextView = v.findViewById<TextView>(R.id.memberName)

        if (leaderArray.isNotEmpty()) {
            val starIcon: ImageView = v.findViewById(R.id.starIcon)
            setStarIcon(leaderArray, memberIcon, starIcon, leaderNo, position)
        }
        setSexIcon(memberIcon, position)
        if (nameIsSpanned) {
            nameTextView.text = HtmlCompat.fromHtml(listElements[position].name, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else {
            nameTextView.text = listElements[position].name
        }

        return v
    }

    private fun setSexIcon(memberIcon: ImageView, position: Int) {

        val sex = listElements[position].sex
        when {
            PublicMethods.isMan(sex) -> {
                memberIcon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            }
            PublicMethods.isWoman(sex) -> {
                memberIcon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
            }
            else -> {
                memberIcon.setColorFilter(PublicMethods.getColor(context, R.color.gray))
            }
        }
    }

    private fun setStarIcon(leaderArray: ArrayList<Member?>, memberIcon: ImageView, starIcon: ImageView, leaderNo: TextView, position: Int) {
        val sex = listElements[position].sex
        when {
            PublicMethods.isMan(sex) -> {
                starIcon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            }
            PublicMethods.isWoman(sex) -> {
                starIcon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
            }
        }

        val member = listElements[position]
        if (leaderArray.contains(member)) {
            memberIcon.visibility = View.GONE
            starIcon.visibility = View.VISIBLE
            leaderNo.visibility = View.GONE

            if (showLeaderNo) {
                leaderNo.visibility = View.VISIBLE
                leaderNo.text = (leaderArray.indexOf(member) + 1).toString()
            }
        }
    }

    fun setRowHeight(listView: ListView) {
        var totalHeight = 40

        for (j in 0 until this.count) {
            val item = this.getView(j, null, listView)
            item.measure(0, 0)
            totalHeight += item.measuredHeight
        }

        listView.layoutParams.height = totalHeight + listView.dividerHeight * (this.count - 1)
        listView.requestLayout()
    }

}


