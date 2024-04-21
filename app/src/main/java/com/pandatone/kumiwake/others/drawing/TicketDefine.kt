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
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.adapter.EditOthersViewAdapter
import com.pandatone.kumiwake.databinding.TicketDifinitionBinding
import com.pandatone.kumiwake.member.function.Group


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class TicketDefine : AppCompatActivity() {
    private lateinit var binding: TicketDifinitionBinding

    private lateinit var ticketListView: ListView
    private lateinit var totalAssignedTextView: TextView
    private var editTicketAdapter: EditOthersViewAdapter? = null
    private var ticketArray: ArrayList<Group> = ArrayList()
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        PublicMethods.setStatus(this, Theme.Others.primaryColor)
        binding = TicketDifinitionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViews()
        editTicketAdapter = EditOthersViewAdapter(this, ticketArray, totalAssignedTextView, true)
        onAddTicket()
        setViews()
        ticketListView.adapter = editTicketAdapter
        ticketColors.clear()
        ticketColors.add(Color.parseColor("#6b6b6b"))
        setKeyboardListener()
    }

    //View宣言
    private fun findViews() {
        ticketListView = findViewById(R.id.ticketListView)
        totalAssignedTextView = findViewById(R.id.total_ticket_no)
        val totalStr = getString(R.string.ticket_number) + "0"
        totalAssignedTextView.text = totalStr
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<Button>(R.id.drawing_result_button).setOnClickListener { onNextClicked() }
        findViewById<ImageButton>(R.id.add_ticket).setOnClickListener { onAddTicket() }
    }


    //組み分け確認画面に遷移ボタン
    private fun onNextClicked() {
        var ticketSum = 0
        var allowToNext = true
        for (i in 0 until ticketListView.count) {
            val ticketNo = editTicketAdapter!!.getNumber(i, false)
            ticketSum += ticketNo
        }
        if (ticketSum <= 0) {
            allowToNext = false
        }

        if (allowToNext) {
            val intent = Intent(this, DrawingResult::class.java)
            intent.putExtra("tickets", createResultTicketsArray())
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            binding.errorIncorrectNumber.visibility = View.VISIBLE
        }
    }

    //くじ追加
    private fun onAddTicket() {
        val ticketNo = ticketArray.size
        ticketColors.add(Color.parseColor("#6b6b6b"))
        updateTicketArray()
        ticketArray.add(Group(ticketNo, "", "", -1))
        editTicketAdapter?.notifyDataSetChanged()
        editTicketAdapter?.setRowHeight(ticketListView)
        editTicketAdapter?.setTotalCount()
    }

    //ticketArrayの内容更新
    private fun updateTicketArray() {
        for (i in 0 until ticketListView.count) {
            val ticketName = editTicketAdapter!!.getName(i, true)
            val ticketNo = editTicketAdapter!!.getNumber(i, true)
            val tickets = ticketArray[i]
            tickets.name = ticketName
            tickets.belongNo = ticketNo
        }
    }

    private fun createResultTicketsArray(): ArrayList<Ticket> {
        val resultTicketArray: ArrayList<Ticket> = ArrayList()
        for (i in 0 until ticketListView.count) {
            val ticketName = editTicketAdapter!!.getName(i, false)
            val ticketNo = editTicketAdapter!!.getNumber(i, false)
            val intColor = ticketColors[i]
            val ticket = Ticket(i, ticketName, intColor)
            for (t in 0 until ticketNo) {
                resultTicketArray.add(ticket)
            }
            FirebaseAnalyticsEvents.ticketNames(ticketName)
        }
        return resultTicketArray
    }

    //キーボードによるレイアウト崩れを防ぐ
    private fun setKeyboardListener() {
        val activityRootView = findViewById<View>(R.id.custom_root_layout)
        val view = findViewById<View>(R.id.drawing_result_button)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
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
        var ticketColors: ArrayList<Int> = ArrayList() //チケットの種類の数だけ
    }
}