package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class GroupNameListAdapter(private val context: Context, private val groupList: List<GroupListAdapter.Group>) : BaseAdapter() {

    @SuppressLint("UseSparseArrays")
    private var gSelection = SparseBooleanArray()

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
        val v: View?
        val listItem = groupList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        v = inflater.inflate(R.layout.row_group, null)

        if (gSelection.get(listItem.id)) {
            v!!.setBackgroundColor(ContextCompat.getColor(MyApplication.context!!, R.color.checked_list))
        }

        nameTextView = v?.findViewById<View>(R.id.groupName) as TextView
        numberOfMemberTextView = v.findViewById<View>(R.id.theNumberOfMember) as TextView
        nameTextView.text = listItem.group
        numberOfMemberTextView.text = "${listItem.belongNo}${MyApplication.context?.getText(R.string.people)}"

        return v
    }

    fun setNewSelection(id: Int, value: Boolean) {
        gSelection.append(id, value)
        notifyDataSetChanged()
    }

    fun removeSelection(id: Int) {
        gSelection.delete(id)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        gSelection.clear()
        notifyDataSetChanged()
    }

    fun isPositionChecked(id: Int): Boolean {
        return gSelection.get(id)
    }

}
