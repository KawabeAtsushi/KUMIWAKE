package com.pandatone.kumiwake

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.getbase.floatingactionbutton.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
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


class MainActivity : AppCompatActivity() {

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

        val dimmer = findViewById<View>(R.id.dimmer_layout)
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

        //組分けボタン
        val kNormalUnit: View = findViewById(R.id.kumiwake_normal_unit)
        val kNormalButton: ImageButton = kNormalUnit.findViewById(R.id.icon_button)
        (kNormalUnit.findViewById(R.id.button_text) as TextView).setText(R.string.normal_mode)
        kNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.theme_red))
        kNormalButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kNormalButton.updatePadding(top = PublicMethods.setByDp(17.0f, this), bottom = PublicMethods.setByDp(22.0f, this))
        kNormalButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Kumiwake.key
            StatusHolder.normalMode = true
            NormalMode.memberArray = ArrayList()
            startActivity(Intent(this, NormalMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeNormal.key)
        }

        val kQuickUnit: View = findViewById(R.id.kumiwake_quick_unit)
        val kQuickButton: ImageButton = kQuickUnit.findViewById(R.id.icon_button)
        (kQuickUnit.findViewById(R.id.button_text) as TextView).setText(R.string.quick_mode)
        kQuickButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.theme_red))
        kQuickButton.setImageResource(R.drawable.ic_kumiwake_24px)
        kQuickButton.updatePadding(top = PublicMethods.setByDp(17.0f, this), bottom = PublicMethods.setByDp(22.0f, this))
        kQuickButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Kumiwake.key
            StatusHolder.normalMode = false
            startActivity(Intent(this, QuickMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.KumiwakeQuick.key)
        }

        //席決めボタン
        val sNormalUnit: View = findViewById(R.id.sekigime_normal_unit)
        val sNormalButton: ImageButton = sNormalUnit.findViewById(R.id.icon_button)
        (sNormalUnit.findViewById(R.id.button_text) as TextView).setText(R.string.normal_mode)
        sNormalButton.backgroundTintList = ColorStateList.valueOf(PublicMethods.getColor(this, R.color.green_title))
        sNormalButton.setImageResource(R.drawable.ic_sekigime_24px)
        sNormalButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Sekigime.key
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
            StatusHolder.mode = ModeKeys.Sekigime.key
            StatusHolder.normalMode = false
            startActivity(Intent(this, QuickMode::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.SekigimeQuick.key)
        }

        //その他機能

        //履歴
        val historyUnit: View = findViewById(R.id.history_unit)
        val historyButton: ImageButton = historyUnit.findViewById(R.id.icon_button)
        (historyUnit.findViewById(R.id.button_text) as TextView).setText(R.string.history)
        historyButton.setImageResource(R.drawable.ic_history_black_24dp)
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryMain::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.History.key)
        }

        //順番決め
        val orderUnit: View = findViewById(R.id.order_unit)
        val orderButton: ImageButton = orderUnit.findViewById(R.id.icon_button)
        (orderUnit.findViewById(R.id.button_text) as TextView).setText(R.string.order)
        orderButton.setImageResource(R.drawable.ic_order)
        orderButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Order.key
            startActivity(Intent(this, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Order.key)
        }

        //役割決め
        val roleUnit: View = findViewById(R.id.role_unit)
        val roleButton: ImageButton = roleUnit.findViewById(R.id.icon_button)
        (roleUnit.findViewById(R.id.button_text) as TextView).setText(R.string.role)
        roleButton.setImageResource(R.drawable.ic_role)
        roleButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Role.key
            startActivity(Intent(this, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Role.key)
        }

        //くじ引き
        val drawingUnit: View = findViewById(R.id.drawing_unit)
        val drawingButton: ImageButton = drawingUnit.findViewById(R.id.icon_button)
        (drawingUnit.findViewById(R.id.button_text) as TextView).setText(R.string.drawing)
        drawingButton.setImageResource(R.drawable.ic_drawing)
        drawingButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Drawing.key
            startActivity(Intent(this, TicketDefine()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Drawing.key)
        }

        //クラスルーム
        val classroomUnit: View = findViewById(R.id.classroom_unit)
        val classroomButton: ImageButton = classroomUnit.findViewById(R.id.icon_button)
        (classroomUnit.findViewById(R.id.button_text) as TextView).setText(R.string.classroom)
        classroomButton.setImageResource(R.drawable.ic_school)
        classroomButton.setOnClickListener {
            StatusHolder.mode = ModeKeys.Classroom.key
            startActivity(Intent(this, SelectMember()::class.java))
            FirebaseAnalyticsEvents.functionSelectEvent(FirebaseAnalyticsEvents.FunctionKeys.Classroom.key)
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

    companion object {
        lateinit var mAdView: AdView
    }
}
