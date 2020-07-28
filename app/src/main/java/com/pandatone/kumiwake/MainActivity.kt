package com.pandatone.kumiwake

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updatePadding
import com.airbnb.lottie.LottieAnimationView
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.getbase.floatingactionbutton.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.history.HistoryMain
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.kumiwake.QuickMode
import com.pandatone.kumiwake.member.AddGroup
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.members.FragmentGroupMain
import com.pandatone.kumiwake.member.members.MembersMain
import com.pandatone.kumiwake.others.SelectMember
import com.pandatone.kumiwake.others.drawing.TicketDefine
import com.pandatone.kumiwake.setting.Help
import com.pandatone.kumiwake.setting.PurchaseFreeAdOption
import com.pandatone.kumiwake.setting.Settings
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


class MainActivity : AppCompatActivity(), RewardedVideoAdListener {

    private lateinit var dimmer: View
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var loadingAnim: LottieAnimationView
    private var rewarded = false

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
        memberFabSetting()

        mAdView = findViewById<View>(R.id.adView) as AdView

        if (StatusHolder.adCheck) {
            startActivity(Intent(this, PurchaseFreeAdOption::class.java))
        }

        if (StatusHolder.adDeleated) {
            mAdView.visibility = View.GONE
        } else {
            MobileAds.initialize(this)
            val adRequest = AdRequest.Builder()
                    .addTestDevice(getString(R.string.device_id)).build()
            mAdView.loadAd(adRequest)
        }
        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()
        PublicMethods.initialize(this)
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

    //メンバーのFABの処理
    private fun memberFabSetting() {
        val menuMultipleActions = findViewById<View>(R.id.multiple_member_fab) as FloatingActionsMenu
        val actionMemberList = findViewById<View>(R.id.member_list_button)
        actionMemberList.setOnClickListener {
            startActivity(Intent(this, MembersMain::class.java))
            menuMultipleActions.collapse()
        }
        val actionAddMember = findViewById<View>(R.id.member_add_button)
        actionAddMember.setOnClickListener {
            startActivity(Intent(this, AddMember::class.java))
            menuMultipleActions.collapse()
        }
        val actionAddGroup = findViewById<View>(R.id.group_add_button)
        actionAddGroup.setOnClickListener {
            FragmentGroupMain.gpAdapter = GroupAdapter(this)
            startActivity(Intent(this, AddGroup::class.java))
            menuMultipleActions.collapse()
        }
        dimmer = findViewById<View>(R.id.dimmer_layout)
        dimmer.setOnClickListener {
            menuMultipleActions.collapse()
        }
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(object : OnFloatingActionsMenuUpdateListener {
            override fun onMenuExpanded() {
                dimmer.visibility = View.VISIBLE
            }

            override fun onMenuCollapsed() {
                dimmer.visibility = View.GONE
            }
        })
    }

    private fun setClickListeners() {
        //toHomepage
        val kumiwakeIcon: ImageView = findViewById(R.id.kumiwake_icon)
        kumiwakeIcon.setOnClickListener { PublicMethods.toWebSite(this, supportFragmentManager) }

        //Care
        val careIcon: ImageView = findViewById(R.id.care_icon)
        careIcon.setOnClickListener {
            dialog.decisionDialog(getString(R.string.support_title), getString(R.string.support_description), getString(R.string.support), getString(R.string.close), R.drawable.ic_care, R.drawable.ic_close_black_24dp) {
                dimmer.visibility = View.VISIBLE
                loadingAnim = findViewById(R.id.loading_anim)
                loadingAnim.visibility = View.VISIBLE
                loadingAnim.playAnimation()
                mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
                mRewardedVideoAd.rewardedVideoAdListener = this
                mRewardedVideoAd.loadAd(getString(R.string.adVideoUnit_id),
                        AdRequest.Builder().addTestDevice(getString(R.string.device_id)).build())
            }
            FirebaseAnalyticsEvents.support("CLICKED")
        }

        //組分けボタン
        val kNormalUnit: View = findViewById(R.id.kumiwake_normal_unit)
        val kNormalButton: ImageButton = kNormalUnit.findViewById(R.id.icon_button)
        (kNormalUnit.findViewById(R.id.button_text) as TextView).setText(R.string.normal_mode)
        kNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.theme_red))
        kNormalButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kNormalButton.updatePadding(top = PublicMethods.setByDp(17.0f, this), bottom = PublicMethods.setByDp(22.0f, this))
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
        kQuickButton.updatePadding(top = PublicMethods.setByDp(17.0f, this), bottom = PublicMethods.setByDp(22.0f, this))
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
        sNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.theme_red))
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

        //設定・ヘルプ
        val helpUnit: View = findViewById(R.id.help_unit)
        val helpButton: ImageButton = helpUnit.findViewById(R.id.icon_button)
        (helpUnit.findViewById(R.id.button_text) as TextView).setText(R.string.help)
        helpButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.yellow_title))
        helpButton.setImageResource(R.drawable.ic_help_outline_dark_24dp)
        helpButton.setOnClickListener {
            startActivity(Intent(this, Help::class.java))
        }

        val settingsUnit: View = findViewById(R.id.settings_unit)
        val settingsButton: ImageButton = settingsUnit.findViewById(R.id.icon_button)
        (settingsUnit.findViewById(R.id.button_text) as TextView).setText(R.string.settings)
        settingsButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.yellow_title))
        settingsButton.setImageResource(R.drawable.ic_settings_24dp)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

    }

    //ツールバー初期表示
    private fun setUpToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)
    }

    //リワード広告のオーバーライド

    // 広告の準備が完了したとき
    override fun onRewardedVideoAdLoaded() {
        mRewardedVideoAd.show()
        loadingAnim.visibility = View.GONE
        loadingAnim.cancelAnimation()
    }

    //報酬対象になったとき
    override fun onRewarded(p0: com.google.android.gms.ads.reward.RewardItem?) {
        rewarded = true
    }

    //広告が閉じられたとき
    override fun onRewardedVideoAdClosed() {
        if (rewarded) {
            val thanks = findViewById<LottieAnimationView>(R.id.thanks_anim)
            Handler().postDelayed({
                thanks.visibility = View.VISIBLE
                thanks.playAnimation()
            }, 500)
            thanks.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    thanks.visibility = View.GONE
                    dimmer.visibility = View.GONE
                }
            })
            rewarded = false
            FirebaseAnalyticsEvents.support("REWARDED")
        } else {
            dimmer.visibility = View.GONE
        }
    }

    override fun onRewardedVideoAdOpened() {}
    override fun onRewardedVideoStarted() {}
    override fun onRewardedVideoAdLeftApplication() {}
    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {}
    override fun onRewardedVideoCompleted() {}

    companion object {
        lateinit var mAdView: AdView
    }
}
