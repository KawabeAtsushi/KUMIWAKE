package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.Name


class NameListAdapter(private val context: Context, private val nameList: List<Name>) : BaseAdapter() {
    private var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, 1)
    private var nowData = "ￚ no data ￚ"
    private var preData = "ￚ no data ￚ"
    private var mSelection = SparseBooleanArray()

    override fun getCount(): Int {
        return nameList.size
    }

    override fun getItem(position: Int): Name? {
        return nameList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        return getItem(position)!!.sex != "initial"
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val nameTextView: TextView
        val listItem = getItem(position)
        val v: View?
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (isEnabled(position)) {

            v = inflater.inflate(R.layout.row_member, null)

            if (mSelection.get(listItem!!.id)) {
                v?.setBackgroundColor(ContextCompat.getColor(MyApplication.context!!, R.color.checked_list))
            }

            setSexIcon(v!!, position)

            nameTextView = v.findViewById<View>(R.id.memberName) as TextView
            nameTextView.text = listItem.name

        } else {
            //イニシャルRow
            v = inflater.inflate(R.layout.row_initial, null)
            addInitial(position, v)
        }
        return v
    }

    private fun setSexIcon(v: View, position: Int) {
        val memberIcon: ImageView = v.findViewById<View>(R.id.memberIcon) as ImageView
        if (getItem(position)!!.sex == context.getText(R.string.man)) {
            memberIcon.setImageResource(R.drawable.member_img)
        } else {
            memberIcon.setColorFilter(ContextCompat.getColor(MyApplication.context!!, R.color.woman))
        }
    }

    private fun addInitial(position: Int, v: View?) {
        val nowItem = getItem(position)

        when (nowSort) {
            MemberListAdapter.MB_READ -> nowData = nowItem!!.read
            MemberListAdapter.MB_AGE -> nowData = nowItem!!.age.toString()
        }

        if (position != 0) {
            val preItem = getItem(position - 2)

            when (nowSort) {
                MemberListAdapter.MB_READ -> preData = preItem!!.read
                MemberListAdapter.MB_AGE -> preData = preItem!!.age.toString()
            }
        }

        //イニシャルRowとメンバーRowが交互に登録されているので表示するRowを選ぶ
        //一行目の場合と、前のイニシャルRowと異なる場合に表示
        if ((nowData != preData || position == 0) && nowSort != MemberListAdapter.MB_ID) {
            val initialText = v?.findViewById<View>(R.id.initial) as TextView
            initialText.text = nowData
        } else {
            if (v != null) {
                v.layoutParams = params
            }
        }
    }

    fun setNewSelection(id: Int, value: Boolean) {
        mSelection.append(id, value)
        notifyDataSetChanged()
    }

    fun removeSelection(id: Int) {
        mSelection.delete(id)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        mSelection.clear()
        notifyDataSetChanged()
    }


    fun isPositionChecked(id: Int): Boolean {
        return mSelection.get(id)
    }

    companion object {
        internal var nowSort = MemberListAdapter.MB_ID
        internal var sortType = "ASC"
    }

}
