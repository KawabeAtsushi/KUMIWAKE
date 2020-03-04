package com.pandatone.kumiwake

import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import com.pandatone.kumiwake.ui.kumiwake.KumiwakeFragment
import com.pandatone.kumiwake.ui.members.MembersFragment
import com.pandatone.kumiwake.ui.sekigime.SekigimeFragment
import com.pandatone.kumiwake.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import com.pandatone.kumiwake.PublicMethods.setStatusBarColor


class MainActivity : AppCompatActivity() {

    lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusHolder.nowTheme = R.style.AppTheme
        setContentView(R.layout.activity_main)

        val navView: AHBottomNavigation = findViewById(R.id.nav_view)

        navView.addItem(AHBottomNavigationItem(R.string.kumiwake, R.drawable.ic_kumiwake_24px, Theme.Kumiwake.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.sekigime, R.drawable.ic_sekigime_24px, Theme.Sekigime.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.member, R.drawable.ic_members_24dp, Theme.Member.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.setting_help, R.drawable.ic_settings_24dp, Theme.Setting.primaryColor))

        navView.isColored = true

        setUpToolbar()
        navView.setOnTabSelectedListener(mOnNavigationItemSelectedListener)

        // To open the first tab as default
        openFragment(KumiwakeFragment())

        MobileAds.initialize(this, "ca-app-pub-2315101868638564~1560987130")
        //MobileAds.initialize(getApplicationContext(), "ca-app-pub-2315101868638564/8665451539");
        mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice("8124DDB5C185E5CA87E826BAB5D4AA10").build()
        mAdView.loadAd(adRequest)

        setKeyboardListener(navView)
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        // 戻るボタンが押されたとき
        when (e.keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                DialogWarehouse(supportFragmentManager).decisionDialog("KUMIWAKE",getString(R.string.app_exit_confirmation)){finish()}
                return true
            }
        }
        return super.dispatchKeyEvent(e)
    }

    //NavigationBarのクリックリスナー
    private val mOnNavigationItemSelectedListener = AHBottomNavigation.OnTabSelectedListener { position, _ ->
        val menuItem: MenuItem = AHBottomNavigationAdapter(this, R.menu.bottom_nav_menu).getMenuItem(position)
        supportActionBar!!.title = menuItem.title
        when (menuItem.itemId) {

            R.id.navigation_kumiwake -> {
                openFragment(KumiwakeFragment())
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Kumiwake.primaryColor))
                supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.kumiwake) + "</font>")
                container.background = getDrawable(Theme.Kumiwake.backgroundColor)
                setStatusBarColor(this,Theme.Kumiwake.primaryColor)
                mAdView.visibility = View.VISIBLE
                true
            }

            R.id.navigation_sekigime -> {
                openFragment(SekigimeFragment())
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Sekigime.primaryColor))
                supportActionBar!!.title = Html.fromHtml("<font color='#616161'>" + getString(R.string.sekigime) + "</font>")
                container.background = getDrawable(Theme.Sekigime.backgroundColor)
                setStatusBarColor(this,Theme.Sekigime.primaryColor)
                mAdView.visibility = View.VISIBLE
                true
            }

            R.id.navigation_members -> {
                openFragment(MembersFragment())
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Member.primaryColor))
                supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.member) + "</font>")
                container.background = ColorDrawable(Theme.Member.backgroundColor)
                setStatusBarColor(this,Theme.Member.primaryColor)
                mAdView.visibility = View.GONE
                true
            }

            R.id.navigation_settings -> {
                openFragment(SettingsFragment())
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Setting.primaryColor))
                supportActionBar!!.title = Html.fromHtml("<font color='#616161'>" + getString(R.string.setting_help) + "</font>")
                container.background = getDrawable(Theme.Setting.backgroundColor)
                setStatusBarColor(this,Theme.Setting.primaryColor)
                mAdView.visibility = View.GONE
                true
            }
            else -> false
        }
    }

    //Fragment初期表示（引数のfragmentが最初に表示される）
    private fun openFragment(fragment: Fragment) {
        // アニメーション無しでバックスタックを消去
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //ツールバー初期表示
    private fun setUpToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.kumiwake) + "</font>")
    }

    //キーボードによるレイアウト崩れを防ぐ
    private fun setKeyboardListener(navView: View) {
        val activityRootView = findViewById<View>(R.id.fragment_layout)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val screenHeight = size.y
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val r = Rect()

            override fun onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r)
                val heightDiff = activityRootView.rootView.height - r.height()
                if (heightDiff > screenHeight * 0.2) {
                    navView.visibility = View.GONE
                } else {
                    navView.visibility = View.VISIBLE
                }
            }
        })
    }
}
