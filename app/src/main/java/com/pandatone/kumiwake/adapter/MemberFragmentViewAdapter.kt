package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.StatusHolder.mbNowSort
import com.pandatone.kumiwake.member.function.Member

//FragmentMember用リストadapter

class MemberFragmentViewAdapter(private val context: Context, private val memberList: List<Member>) : BaseAdapter() {
    private var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, 1)
    private var nowData = "ￚ no data ￚ"
    private var preData = "ￚ no data ￚ"
    private var mSelection = SparseBooleanArray()

    override fun getCount(): Int {
        return memberList.size
    }

    override fun getItem(position: Int): Member {
        return memberList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        return getItem(position).sex != StatusHolder.index
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val nameTextView: TextView
        val member = getItem(position)
        val v: View?
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (isEnabled(position)) {

            v = inflater.inflate(R.layout.row_member, null)

            if (mSelection.get(member.id)) {
                v?.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(context, R.color.checked_list))
            }

            setSexIcon(v!!, position)

            nameTextView = v.findViewById<View>(R.id.memberName) as TextView
            nameTextView.text = member.name

        } else {
            //イニシャルRow
            v = inflater.inflate(R.layout.row_initial, null)
            addInitial(position, v)
        }
        return v
    }

    //性別アイコン描画
    private fun setSexIcon(v: View, position: Int) {
        val memberIcon: ImageView = v.findViewById<View>(R.id.memberIcon) as ImageView
        if (getItem(position).sex == context.getText(R.string.man)) {
            memberIcon.setColorFilter(PublicMethods.getColor(context, R.color.man))
        } else {
            memberIcon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
        }
    }

    //頭文字行挿入
    private fun addInitial(position: Int, v: View?) {
        val nowItem = getItem(position)

        when (mbNowSort) {
            MemberAdapter.MB_READ -> nowData = nowItem.read
            MemberAdapter.MB_AGE -> nowData = nowItem.age.toString()
        }

        if (position >= 2) {
            val preItem = getItem(position - 2)

            when (mbNowSort) {
                MemberAdapter.MB_READ -> preData = preItem.read
                MemberAdapter.MB_AGE -> preData = preItem.age.toString()
            }
        }

        //イニシャルRowとメンバーRowが交互に登録されているので表示するRowを選ぶ
        //一行目の場合と、前のイニシャルRowと異なる場合に表示
        if ((nowData != preData || position == 0) && mbNowSort != MemberAdapter.MB_ID) {
            val initialText = v?.findViewById<View>(R.id.initial) as TextView
            initialText.text = nowData
        } else {
            if (v != null) {
                v.layoutParams = params
            }
        }
    }

    //メンバーIDとチェックを紐づけて設定
    fun setNewSelection(id: Int, value: Boolean) {
        mSelection.append(id, value)
        notifyDataSetChanged()
    }

    //チェック
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

}
