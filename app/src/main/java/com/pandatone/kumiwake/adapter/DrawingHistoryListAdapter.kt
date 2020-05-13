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
import com.pandatone.kumiwake.others.drawing.TicketDefine
import java.util.*

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class DrawingHistoryListAdapter(private val context: Context, private val kinds: ArrayList<String>, private val tickets: ArrayList<String>, private val picked: ArrayList<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return kinds.size
    }

    override fun getItem(position: Int): String {
        return kinds[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        val pickedNoTextView: TextView
        val remainNoTextView: TextView
        var v = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (v == null) {
            v = inflater.inflate(R.layout.row_drawing_history, null)
        }

        val icon = v!!.findViewById<ImageView>(R.id.drawing_row_icon)
        icon.setColorFilter(TicketDefine.ticketColors[position])
        val ticket = kinds[position]
        nameTextView = v.findViewById<View>(R.id.ticketName) as TextView
        pickedNoTextView = v.findViewById<View>(R.id.pickedNo) as TextView
        remainNoTextView = v.findViewById<View>(R.id.remainNo) as TextView
        nameTextView.text = ticket
        pickedNoTextView.text = pickedNo(ticket).toString()
        remainNoTextView.text = remainNo(ticket).toString()

        return v
    }

    private fun pickedNo(ticket:String):Int{
        return picked.count { it == ticket }
    }

    private fun remainNo(ticket:String):Int{
        val pickedNo = picked.count { it == ticket }
        val allNo = tickets.count { it == ticket }
        return allNo - pickedNo
    }

}

