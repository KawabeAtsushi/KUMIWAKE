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
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.kumiwake.QuickMode
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


class KumiwakeFragment : Fragment() {

    private lateinit var normalButton: Button
    private lateinit var quickButton: Button
    private lateinit var historyButton: Button

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

        val root = inflater.inflate(R.layout.fragment_kumiwake, container, false)

        val kumiwakeIcon: ImageView = root.findViewById(R.id.main_icon)
        kumiwakeIcon.setOnClickListener { PublicMethods.toWebSite(requireContext(), requireFragmentManager()) }

        normalButton = root.findViewById(R.id.normal_mode_button)
        normalButton.setOnClickListener {
            StatusHolder.normalMode = true
            NormalMode.memberArray = ArrayList()
            startActivity(Intent(activity, NormalMode::class.java))
        }
        quickButton = root.findViewById(R.id.quick_mode_button)
        quickButton.setOnClickListener {
            StatusHolder.normalMode = false
            startActivity(Intent(activity, QuickMode::class.java))
        }
        historyButton = root.findViewById(R.id.history_button)
        historyButton.setOnClickListener {
            startActivity(Intent(activity, HistoryMain::class.java))
        }

        val homepageLink = PublicMethods.getLinkChar(getString(R.string.url_homepage), getString(R.string.more_details))
        val normalHelp: ImageButton = root.findViewById(R.id.hintForNormalMode)
        normalHelp.setOnClickListener {
            dialog.confirmationDialog(getString(R.string.normal_mode), getString(R.string.description_of_normal_kumiwake), homepageLink)
        }
        val quickHelp: ImageButton = root.findViewById(R.id.hintForQuickMode)
        quickHelp.setOnClickListener {
            dialog.confirmationDialog(getString(R.string.quick_mode), getString(R.string.description_of_quick_kumiwake), homepageLink)
        }

        val layout = root.findViewById<ConstraintLayout>(R.id.top_container)
        val adView = MainActivity.mAdView
        val guideLine = root.findViewById<View>(R.id.half_guideline) as Guideline
        adView.afterMeasured {
            // layoutのレイアウトが完了したら、ガイドラインの位置を変更
            val lHeight = layout.height.toFloat()
            val adHeight = adView.height.toFloat()
            val adRatio = adHeight / lHeight
            val guideLineRatio = 0.5f - adRatio
            guideLine.setGuidelinePercent(guideLineRatio)
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.kumiwake_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val homepageLink = PublicMethods.getLinkChar(getString(R.string.url_homepage), getString(R.string.more_details))
        when (item.itemId) {
            R.id.menu_help -> dialog.confirmationDialog(getString(R.string.kumiwake), getString(R.string.how_to_kumiwake), homepageLink)
        }
        return true
    }

    //Viewのレイアウトが完了したタイミングで呼ばれる拡張関数
    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (width > 0 && height > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }
}