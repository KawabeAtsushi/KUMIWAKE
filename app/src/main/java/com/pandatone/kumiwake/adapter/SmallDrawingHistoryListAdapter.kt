package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.others.drawing.Ticket

/**
 * Created by atsushi_2 on 2016/04/16.
 */
class SmallDrawingHistoryListAdapter(private val context: Context, private val picked: MutableList<Ticket>) : BaseAdapter() {

    override fun getCount(): Int {
        return picked.size
    }

    override fun getItem(position: Int): Ticket {
        return picked[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        val numberTextView: TextView
        var v = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (v == null) {
            v = inflater.inflate(R.layout.row_simple_drawing_history, null)
        }

        numberTextView = v!!.findViewById<View>(R.id.pickNo) as TextView
        nameTextView = v.findViewById<View>(R.id.ticketName) as TextView
        numberTextView.text = (count - position).toString()
        nameTextView.text = HtmlCompat.fromHtml(getTicketNameWithColor(picked[position]), HtmlCompat.FROM_HTML_MODE_COMPACT)

        return v
    }

    //チケットの色取得(Hex)
    private fun getTicketNameWithColor(ticket: Ticket): String {
        val strColor = String.format("#%06X", 0xFFFFFF and ticket.color)
        return "<font color='${strColor}'>${ticket.name}</font>"
    }

}

