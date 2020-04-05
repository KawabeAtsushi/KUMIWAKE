package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class HistoryFragmentViewAdapter(private val context: Context, private val historyList: List<History>) : BaseAdapter() {

    @SuppressLint("UseSparseArrays")
    private var gSelection = SparseBooleanArray()

    override fun getCount(): Int {
        return historyList.size
    }

    override fun getItem(position: Int): History {
        return historyList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("InflateParams", "SetTextI18n", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val nameTextView: TextView
        val v: View?
        val history = getItem(position)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        v = inflater.inflate(R.layout.row_history, null)

        if (gSelection.get(history.id)) {
            v!!.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(context, R.color.checked_list))
        }

        setModeIcon(v,position)

        nameTextView = v?.findViewById<View>(R.id.historyName) as TextView
        nameTextView.text = history.name

        return v
    }

    //アイコン描画
    private fun setModeIcon(v: View, position: Int) {
        val modeIcon: ImageView = v.findViewById(R.id.modeIcon)
        if (getItem(position).mode == 1) {
            modeIcon.setImageResource(R.drawable.ic_sekigime_24px)
        }
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
