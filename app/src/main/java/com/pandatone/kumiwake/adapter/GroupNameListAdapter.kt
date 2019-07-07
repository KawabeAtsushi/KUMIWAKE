package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class GroupNameListAdapter(private val context: Context, private val groupList: List<GroupListAdapter.Group>) : BaseAdapter() {

    @SuppressLint("UseSparseArrays")
    private var gSelection = HashMap<Int, Boolean>()

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): Any {
        return groupList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val nameTextView: TextView
        val numberOfMemberTextView: TextView
        var v: View? = convertView
        val listItem = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_group, null)
        }
        if (gSelection[position] != null) {
            v!!.setBackgroundColor(ContextCompat.getColor(MyApplication.context!!,R.color.checked_list))
        } else {
            v = inflater.inflate(R.layout.row_group, null)
        }

        nameTextView = v!!.findViewById<View>(R.id.groupName) as TextView
        numberOfMemberTextView = v.findViewById<View>(R.id.theNumberOfMember) as TextView
        nameTextView.text = listItem.group
        numberOfMemberTextView.text = "${listItem.belongNo}${MyApplication.context?.getText(R.string.people)}"

        return v
    }

    fun setNewSelection(position: Int, value: Boolean) {
        gSelection[position] = value
        notifyDataSetChanged()
    }

    fun removeSelection(position: Int) {
        gSelection.remove(position)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        gSelection = HashMap()
        notifyDataSetChanged()
    }

    fun isPositionChecked(position: Int): Boolean {
        val result = gSelection[position]
        return result ?: false
    }

}
