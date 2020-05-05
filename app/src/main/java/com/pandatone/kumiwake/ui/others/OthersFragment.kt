package com.pandatone.kumiwake.ui.kumiwake

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


class OthersFragment : Fragment() {

    private val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(requireFragmentManager())
        }

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

        return root
    }
}