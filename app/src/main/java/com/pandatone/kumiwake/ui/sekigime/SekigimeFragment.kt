package com.pandatone.kumiwake.ui.sekigime

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.kumiwake.QuickMode
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse

class SekigimeFragment : Fragment() {

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

        val root = inflater.inflate(R.layout.fragment_sekigime, container, false)

        val normalButton: Button = root.findViewById(R.id.normal_mode_button)
        normalButton.setOnClickListener {
            StatusHolder.normalMode = true
            NormalMode.memberArray = ArrayList()
            startActivity(Intent(activity, NormalMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeNormal.key)
        }
        val quickButton: Button = root.findViewById(R.id.quick_mode_button)
        quickButton.setOnClickListener {
            StatusHolder.normalMode = false
            startActivity(Intent(activity, QuickMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeQuick.key)
        }

        //ヘルプボタンのクリックリスナ
        val homepageLink = PublicMethods.getLinkChar(getString(R.string.url_homepage), getString(R.string.more_details))
        val sekigimeHelp: ImageButton = root.findViewById(R.id.hintForSekigime)
        sekigimeHelp.setOnClickListener {
            dialog.confirmationDialog(getString(R.string.sekigime), getString(R.string.how_to_sekigime), homepageLink)
        }
        val normalHelp: ImageButton = root.findViewById(R.id.hintForNormalMode)
        normalHelp.setOnClickListener {
            dialog.confirmationDialog(getString(R.string.normal_mode), getString(R.string.description_of_normal_sekigime), homepageLink)
        }
        val quickHelp: ImageButton = root.findViewById(R.id.hintForQuickMode)
        quickHelp.setOnClickListener {
            dialog.confirmationDialog(getString(R.string.quick_mode), getString(R.string.description_of_quick_sekigime), homepageLink)
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