package com.pandatone.kumiwake.ui.others

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.others.SelectMember
import com.pandatone.kumiwake.others.drawing.TicketDefine


class OthersFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val root = inflater.inflate(R.layout.fragment_others, container, false)

        val historyButton: Button = root.findViewById(R.id.history_button)
        historyButton.setOnClickListener {
            startActivity(Intent(activity, HistoryMain::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.History.key)
        }

        val orderButton: Button = root.findViewById(R.id.order_button)
        orderButton.setOnClickListener {
            StatusHolder.order = true
            startActivity(Intent(activity, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Order.key)
        }

        val roleButton: Button = root.findViewById(R.id.role_button)
        roleButton.setOnClickListener {
            StatusHolder.order = false
            startActivity(Intent(activity, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Role.key)
        }

        val drawingButton: Button = root.findViewById(R.id.drawing_button)
        drawingButton.setOnClickListener {
            startActivity(Intent(activity, TicketDefine()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Drawing.key)
        }

        return root
    }
}