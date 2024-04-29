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
class SmallMBListAdapter(
    private val context: Context,
    memberList: ArrayList<Member>,
    private val leaderArray: ArrayList<Member?> = ArrayList(),
    val showLeaderNo: Boolean = false,
    val nameIsSpanned: Boolean = false,
    private val showSexIcon: Boolean = true,
    private val showNumberIcon: Boolean = false,
) : BaseAdapter() {
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

        val numberIcon: View = v.findViewById(R.id.numberIcon)
        val memberIconContainer: ViewGroup = v.findViewById(R.id.memberIconContainer)
        val nameTextView = v.findViewById<TextView>(R.id.memberName)

        val member: Member = listElements[position]

        if (showNumberIcon) {
            val numberTextView = numberIcon.findViewById<TextView>(R.id.number_textView)
            numberTextView.text = position.toString()
            numberIcon.visibility = View.VISIBLE
        }
        if (showSexIcon) {
            if (leaderArray.contains(member)) {
                setLeaderIcon(context, leaderArray, memberIconContainer, member)
            } else {
                setSexIcon(memberIconContainer, member)
            }
        } else {
            memberIconContainer.visibility = View.GONE
        }
        if (nameIsSpanned) {
            nameTextView.text = HtmlCompat.fromHtml(member.name, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else {
            nameTextView.text = member.name
        }

        return v
    }

    private fun setSexIcon(memberIconContainer: ViewGroup, member: Member) {
        val sexIcon = memberIconContainer.findViewById<ImageView>(R.id.memberSexIcon)

        when {
            PublicMethods.isMan(member.sex) -> {
                sexIcon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            }

            PublicMethods.isWoman(member.sex) -> {
                sexIcon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
            }

            else -> {
                sexIcon.setColorFilter(PublicMethods.getColor(context, R.color.gray))
            }
        }
    }

    private fun setLeaderIcon(
        context: Context,
        leaderArray: ArrayList<Member?>,
        memberIconContainer: ViewGroup,
        member: Member,
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val leaderIcon: View =
            inflater?.inflate(R.layout.leader_icon, null)
                ?: throw Exception()
        memberIconContainer.removeAllViews()
        memberIconContainer.addView(leaderIcon, memberIconContainer.layoutParams)
        val starIcon = leaderIcon.findViewById<ImageView>(R.id.starIcon)
        val leaderNo = leaderIcon.findViewById<TextView>(R.id.leaderNo)
        when {
            PublicMethods.isMan(member.sex) -> {
                starIcon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            }

            PublicMethods.isWoman(member.sex) -> {
                starIcon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
            }
        }

        if (showLeaderNo) {
            leaderNo.visibility = View.VISIBLE
            (leaderArray.indexOf(member) + 1).toString().also { leaderNo.text = it }
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


