package com.pandatone.kumiwake

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.PublicMethods.setStatusBarColor
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.kumiwake.QuickMode
import com.pandatone.kumiwake.others.SelectMember
import com.pandatone.kumiwake.others.drawing.TicketDefine
import com.pandatone.kumiwake.setting.PurchaseFreeAdOption
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import com.pandatone.kumiwake.ui.members.MembersMain
import com.pandatone.kumiwake.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val dialog: DialogWarehouse
        get() {
            return DialogWarehouse(supportFragmentManager)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContentView(R.layout.activity_main)
        setClickListeners()

        if (StatusHolder.adCheck) {
            startActivity(Intent(this, PurchaseFreeAdOption::class.java))
        }

        val mAdView = findViewById<View>(R.id.adView) as AdView
        if (StatusHolder.adDeleated) {
            mAdView.visibility = View.GONE
        } else {
            MobileAds.initialize(this, getString(R.string.adApp_id))
            val adRequest = AdRequest.Builder()
                    .addTestDevice(getString(R.string.device_id)).build()
            mAdView.loadAd(adRequest)
        }
        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()
        PublicMethods.initialize()
    }

    // 戻るボタンが押されたとき
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogWarehouse(supportFragmentManager).decisionDialog("KUMIWAKE", getString(R.string.app_exit_confirmation)) {
                finishAndRemoveTask()
            }
            return true
        }
        return false
    }

    private fun setClickListeners() {
        //toHomepage
        val kumiwakeIcon: ImageView = findViewById(R.id.kumiwake_icon)
        kumiwakeIcon.setOnClickListener { PublicMethods.toWebSite(this, supportFragmentManager) }

        //settings/help
        val settingsIcon: ImageButton = findViewById(R.id.settings_button)
        settingsIcon.setOnClickListener {
            openFragment(SettingsFragment())
            supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Setting.primaryColor))
            supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#616161'>" + getString(R.string.setting_help) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
            container.background = getDrawable(Theme.Setting.backgroundColor)
            setStatusBarColor(this, Theme.Setting.primaryColor)
        }

        //組分けボタン
        val kNormalUnit: View = findViewById(R.id.kumiwake_normal_unit)
        val kNormalButton: ImageButton = kNormalUnit.findViewById(R.id.icon_button)
        (kNormalUnit.findViewById(R.id.button_text) as TextView).setText(R.string.normal_mode)
        kNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.red_unpressed))
        kNormalButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kNormalButton.updatePadding(top = 23, bottom = 32)
        kNormalButton.setOnClickListener {
            StatusHolder.sekigime = false
            StatusHolder.normalMode = true
            NormalMode.memberArray = ArrayList()
            startActivity(Intent(this, NormalMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeNormal.key)
        }

        val kQuickUnit: View = findViewById(R.id.kumiwake_quick_unit)
        val kQuickButton: ImageButton = kQuickUnit.findViewById(R.id.icon_button)
        (kQuickUnit.findViewById(R.id.button_text) as TextView).setText(R.string.quick_mode)
        kQuickButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.green_title))
        kQuickButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kQuickButton.updatePadding(top = 23, bottom = 32)
        kQuickButton.setOnClickListener {
            StatusHolder.sekigime = false
            StatusHolder.normalMode = false
            startActivity(Intent(this, QuickMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeQuick.key)
        }

        //席決めボタン
        val sNormalUnit: View = findViewById(R.id.sekigime_normal_unit)
        val sNormalButton: ImageButton = sNormalUnit.findViewById(R.id.icon_button)
        (sNormalUnit.findViewById(R.id.button_text) as TextView).setText(R.string.normal_mode)
        sNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.red_unpressed))
        sNormalButton.setImageResource(R.drawable.ic_sekigime_24px)
        sNormalButton.setOnClickListener {
            StatusHolder.sekigime = true
            StatusHolder.normalMode = true
            NormalMode.memberArray = ArrayList()
            startActivity(Intent(this, NormalMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeNormal.key)
        }

        val sQuickUnit: View = findViewById(R.id.sekigime_quick_unit)
        val sQuickButton: ImageButton = sQuickUnit.findViewById(R.id.icon_button)
        (sQuickUnit.findViewById(R.id.button_text) as TextView).setText(R.string.quick_mode)
        sQuickButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.green_title))
        sQuickButton.setImageResource(R.drawable.ic_sekigime_24px)
        sQuickButton.setOnClickListener {
            StatusHolder.sekigime = true
            StatusHolder.normalMode = false
            startActivity(Intent(this, QuickMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeQuick.key)
        }

        //その他機能
        val historyUnit: View = findViewById(R.id.history_unit)
        val historyButton: ImageButton = historyUnit.findViewById(R.id.icon_button)
        (historyUnit.findViewById(R.id.button_text) as TextView).setText(R.string.history)
        historyButton.setImageResource(R.drawable.ic_history_black_24dp)
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryMain::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.History.key)
        }

        val orderUnit: View = findViewById(R.id.order_unit)
        val orderButton: ImageButton = orderUnit.findViewById(R.id.icon_button)
        (orderUnit.findViewById(R.id.button_text) as TextView).setText(R.string.order)
        orderButton.setImageResource(R.drawable.ic_order)
        orderButton.setOnClickListener {
            StatusHolder.order = true
            startActivity(Intent(this, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Order.key)
        }

        val roleUnit: View = findViewById(R.id.role_unit)
        val roleButton: ImageButton = roleUnit.findViewById(R.id.icon_button)
        (roleUnit.findViewById(R.id.button_text) as TextView).setText(R.string.role)
        roleButton.setImageResource(R.drawable.ic_role)
        roleButton.setOnClickListener {
            StatusHolder.order = false
            startActivity(Intent(this, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Role.key)
        }

        val drawingUnit: View = findViewById(R.id.drawing_unit)
        val drawingButton: ImageButton = drawingUnit.findViewById(R.id.icon_button)
        (drawingUnit.findViewById(R.id.button_text) as TextView).setText(R.string.drawing)
        drawingButton.setImageResource(R.drawable.ic_drawing)
        drawingButton.setOnClickListener {
            startActivity(Intent(this, TicketDefine()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Drawing.key)
        }

        //メンバー
        val memberButton: Button = findViewById(R.id.member_button)
        memberButton.setOnClickListener {
            startActivity(Intent(this, MembersMain()::class.java))
        }

    }

    //Fragment初期表示（引数のfragmentが最初に表示される）
    private fun openFragment(fragment: Fragment) {
        // アニメーション無しでバックスタックを消去
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //ツールバー初期表示
    private fun setUpToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)
    }

    companion object {
        lateinit var mAdView: AdView
    }
}
