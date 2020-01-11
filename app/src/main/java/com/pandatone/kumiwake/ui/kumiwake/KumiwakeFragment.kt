package com.pandatone.kumiwake.ui.kumiwake

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.Main2Activity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.kumiwake.QuickMode
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.ui.DialogWarehouse


class KumiwakeFragment : Fragment() {

    private lateinit var kumiwakeViewModel: KumiwakeViewModel
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

        kumiwakeViewModel =
                ViewModelProviders.of(this).get(KumiwakeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_kumiwake, container, false)

        val normalButton: TextView = root.findViewById(R.id.normal_mode_button)
        normalButton.setOnClickListener {
            SekigimeResult.Normalmode = true
            Main2Activity.sekigime = false
            startActivity(Intent(activity, NormalMode::class.java))
        }
        val quickButton: TextView = root.findViewById(R.id.quick_mode_button)
        quickButton.setOnClickListener {
            SekigimeResult.Normalmode = false
            Main2Activity.sekigime = false
            startActivity(Intent(activity, QuickMode::class.java))
        }

        val normalHelp: ImageButton = root.findViewById(R.id.hintForNormalMode)
        normalHelp.setOnClickListener {
            dialog.confirmationDialog("Hint : " + getString(R.string.normal_mode), getString(R.string.description_of_normal_mode))
        }
        val quickHelp: ImageButton = root.findViewById(R.id.hintForQuickMode)
        quickHelp.setOnClickListener {
            dialog.confirmationDialog("Hint : " + getString(R.string.quick_mode), getString(R.string.description_of_quick_mode))
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.help_icon_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> dialog.confirmationDialog("Hint : " + getString(R.string.kumiwake), getString(R.string.how_to_kumiwake))
        }
        return true
    }
}