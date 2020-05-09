package com.pandatone.kumiwake.ui.others

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.others.SelectMember


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
        }

        val orderButton: Button = root.findViewById(R.id.order_button)
        orderButton.setOnClickListener {
            StatusHolder.order = true
            startActivity(Intent(activity, SelectMember()::class.java))
        }

        val roleButton: Button = root.findViewById(R.id.role_button)
        roleButton.setOnClickListener {
            StatusHolder.order = false
            startActivity(Intent(activity, SelectMember()::class.java))
        }

        return root
    }
}