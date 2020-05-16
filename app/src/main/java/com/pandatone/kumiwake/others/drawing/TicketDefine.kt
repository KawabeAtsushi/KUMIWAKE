package com.pandatone.kumiwake.others.drawing

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.EditOthersViewAdapter
import com.pandatone.kumiwake.member.function.Group
import kotlinx.android.synthetic.main.kumiwake_custom.*


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class TicketDefine : AppCompatActivity() {
    private lateinit var ticketListView: ListView
    private lateinit var totalAssinedTextView: TextView
    private var editTicketAdapter: EditOthersViewAdapter? = null
    private var roleArray: ArrayList<Group> = ArrayList()
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.ticket_difinition)
        findViews()
        onAddTicket()
        editTicketAdapter = EditOthersViewAdapter(this, roleArray, totalAssinedTextView, true)
        setViews()
        ticketListView.adapter = editTicketAdapter
        ticketColors.add(Color.parseColor("#6b6b6b"))
        setKeyboardListener()
    }

    //View宣言
    private fun findViews() {
        ticketListView = findViewById(R.id.ticketListView)
        totalAssinedTextView = findViewById(R.id.total_ticket_no)
        val totalStr = getString(R.string.ticket_number) + "0"
        totalAssinedTextView.text = totalStr
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<Button>(R.id.drawing_result_button).setOnClickListener { onNextClicked() }
        findViewById<Button>(R.id.add_ticket).setOnClickListener { onAddTicket() }
    }


    //組み分け確認画面に遷移ボタン
    private fun onNextClicked() {
        var ticketSum = 0
        var allowToNext = true
        for (i in 0 until ticketListView.count) {
            val ticketNo = editTicketAdapter!!.getNumber(i)
            ticketSum += ticketNo
            if (ticketSum <= 0) {
                allowToNext = false
            }
        }

        if (allowToNext) {
            val intent = Intent(this, DrawingResult::class.java)
            intent.putExtra("tickets", createTicketsArray())
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            error_member_no_txt.visibility = View.VISIBLE
        }
    }

    //役職追加
    private fun onAddTicket() {
        val ticketNo = roleArray.size
        val ticketName = getString(R.string.ticket) + " " + (ticketNo + 1).toString()
        ticketColors.add(Color.parseColor("#6b6b6b"))
        updateRoleArray()
        roleArray.add(Group(ticketNo, ticketName, "", 1))
        editTicketAdapter?.notifyDataSetChanged()
        editTicketAdapter?.setRowHeight(ticketListView)
    }

    //roleArrayの内容更新
    private fun updateRoleArray() {
        for (i in 0 until ticketListView.count) {
            val ticketName = editTicketAdapter!!.getName(i)
            val ticketNo = editTicketAdapter!!.getNumber(i)
            val tickets = roleArray[i]
            tickets.name = ticketName
            tickets.belongNo = ticketNo
        }
    }

    private fun createTicketsArray(): ArrayList<String> {
        val ticketArray: ArrayList<String> = ArrayList()
        var total = 0
        for (i in 0 until ticketListView.count) {
            val ticketName = editTicketAdapter!!.getName(i)
            val ticketNo = editTicketAdapter!!.getNumber(i)
            total += ticketNo
            if (ticketNo != 0) {
                for (t in 0 until ticketNo)
                    ticketArray.add(ticketName)
            } else {
                ticketColors.removeAt(i)
            }
        }
        return ticketArray
    }

    //キーボードによるレイアウト崩れを防ぐ
    private fun setKeyboardListener() {
        val activityRootView = findViewById<View>(R.id.custom_root_layout)
        val view = findViewById<View>(R.id.drawing_result_button)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val r = Rect()

            override fun onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r)
                val heightDiff = activityRootView.rootView.height - r.height()
                if (heightDiff > screenHeight * 0.2) {
                    view.visibility = View.GONE
                } else {
                    view.visibility = View.VISIBLE
                }
            }
        })
    }

    companion object {
        var ticketColors: ArrayList<Int> = ArrayList()
    }
}