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
import com.pandatone.kumiwake.member.Name
import com.pandatone.kumiwake.member.Sort
import java.util.*

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class MBListViewAdapter(private val context: Context, nameList: ArrayList<Name>, group_no: Int) : BaseAdapter() {
    private val inflater: LayoutInflater
    private var groupNo: Int = 0

    init {
        listElements = nameList
        groupNo = group_no
        ldNo = 1
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (group_no < 1000) {
            leaderNoArray = ArrayList(group_no)
        }
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
        return groupNo < 1000
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        var v = convertView
        val listItem = getItem(position)


        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_member, null)
        }

        setStarIcon(v!!, position)

        setSexIcon(v, position)
        nameTextView = v.findViewById<View>(R.id.memberName) as TextView
        nameTextView.text = listElements[position].name

        return v
    }


    private fun setSexIcon(v: View, position: Int) {
        val memberIcon: ImageView = v.findViewById<View>(R.id.memberIcon) as ImageView
        if (listElements[position].sex == "男") {
            memberIcon.setImageResource(R.drawable.member_img)
        } else {
            memberIcon.setColorFilter(Sort.memberContext()!!.resources.getColor(R.color.woman))
        }
    }

    private fun setStarIcon(v: View, position: Int) {
        val starIcon: ImageView = v.findViewById<View>(R.id.starIcon) as ImageView
        val memberIcon: ImageView = v.findViewById<View>(R.id.memberIcon) as ImageView
        val leaderNo: TextView = v.findViewById<View>(R.id.leaderNo) as TextView

        if (listElements[position].role.matches((".*" + Sort.memberContext()!!.getText(R.string.leader) + ".*").toRegex())) {
            memberIcon.visibility = View.GONE
            starIcon.visibility = View.VISIBLE
            leaderNo.visibility = View.GONE

            if (groupNo != 1000 && ldNo != groupNo + 1 && !listElements[position].role.matches((".*" + "LD" + ".*").toRegex())) {
                leaderNoArray!!.add(ldNo)
                val newRole = StringBuilder()
                newRole.append(listElements[position].role)
                newRole.append(",LD$ldNo")
                listElements[position] = Name(listElements[position].id, listElements[position].name, listElements[position].sex, listElements[position].age,
                        listElements[position].grade, listElements[position].belong,
                        newRole.toString(), listElements[position].read)
                ldNo++
            } else if (groupNo != 1000 && ldNo == groupNo + 1
                    && !listElements[position].role.matches((".*" + "LD" + ".*").toRegex())) {
                val list = KumiwakeCustom.deleteLeaderList(listElements[position].role)
                val newRole = StringBuilder()
                for (j in list.indices) {
                    newRole.append(list[j])
                    if (j != list.size - 1) {
                        newRole.append(",")
                    }
                }
                listElements[position] = Name(listElements[position].id, listElements[position].name,
                        listElements[position].sex, listElements[position].age,
                        listElements[position].grade, listElements[position].belong,
                        newRole.toString(), listElements[position].read)
            }

            if (groupNo != 2000 && listElements[position].role.matches((".*" + "LD" + ".*").toRegex())) {
                leaderNo.visibility = View.VISIBLE
                val roleArray = listElements[position].role.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*roleArray))
                leaderNo.text = list[list.size - 1].substring(2).toString()
            }
        } else {
            memberIcon.visibility = View.VISIBLE
            starIcon.visibility = View.GONE
            leaderNo.visibility = View.GONE
            setSexIcon(v, position)
        }
    }


    companion object {

        var listElements: ArrayList<Name> = ArrayList()
        var leaderNoArray: ArrayList<Int>? = null
        var ldNo = 1

        fun setRowHeight(listView: ListView, listAdp: MBListViewAdapter) {
            var totalHeight = 33

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


