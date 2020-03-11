package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class GroupFragmentViewAdapter(private val context: Context, private val groupList: List<Group>) : BaseAdapter() {

    @SuppressLint("UseSparseArrays")
    private var gSelection = SparseBooleanArray()

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): Group {
        return groupList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("InflateParams", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val nameTextView: TextView
        val numberOfMemberTextView: TextView
        val v: View?
        val group = getItem(position)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        v = inflater.inflate(R.layout.row_group, null)

        if (gSelection.get(group.id)) {
            v!!.setBackgroundColor(PublicMethods.getColor(context, R.color.checked_list))
        }

        nameTextView = v?.findViewById<View>(R.id.groupName) as TextView
        numberOfMemberTextView = v.findViewById<View>(R.id.theNumberOfMember) as TextView
        nameTextView.text = group.name
        numberOfMemberTextView.text = "${group.belongNo}${context.getText(R.string.people)}"

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
