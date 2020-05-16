package com.pandatone.kumiwake

import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.PublicMethods.setStatusBarColor
import com.pandatone.kumiwake.setting.PurchaseFreeAdOption
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import com.pandatone.kumiwake.ui.kumiwake.KumiwakeFragment
import com.pandatone.kumiwake.ui.members.MembersFragment
import com.pandatone.kumiwake.ui.others.OthersFragment
import com.pandatone.kumiwake.ui.sekigime.SekigimeFragment
import com.pandatone.kumiwake.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.kumiwake))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.sekigime))

        if (StatusHolder.adCheck) {
            startActivity(Intent(this, PurchaseFreeAdOption::class.java))
        }

        mAdView = findViewById<View>(R.id.adView) as AdView
        if (StatusHolder.adDeleated) {
            mAdView.visibility = View.GONE
        } else {
            MobileAds.initialize(this, getString(R.string.adApp_id))
            val adRequest = AdRequest.Builder()
                    .addTestDevice(getString(R.string.device_id)).build()
            mAdView.loadAd(adRequest)
        }
        val navView: AHBottomNavigation = findViewById(R.id.nav_view)

        navView.addItem(AHBottomNavigationItem(R.string.kumiwake, R.drawable.ic_kumiwake_24px, Theme.Kumiwake.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.others, R.drawable.ic_star, Theme.Others.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.member, R.drawable.ic_members_24dp, Theme.Member.primaryColor))
        navView.addItem(AHBottomNavigationItem(R.string.setting_help, R.drawable.ic_settings_24dp, Theme.Setting.primaryColor))

        navView.isColored = true

        setUpToolbar()
        navView.setOnTabSelectedListener(mOnNavigationItemSelectedListener)
        tabLayout.addOnTabSelectedListener(tabItemSelectedListener)

        // To open the first tab as default
        openFragment(KumiwakeFragment())
        setKeyboardListener(navView)
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

    //NavigationBarのクリックリスナー
    private val mOnNavigationItemSelectedListener = AHBottomNavigation.OnTabSelectedListener { position, _ ->
        val menuItem: MenuItem = AHBottomNavigationAdapter(this, R.menu.bottom_nav_menu).getMenuItem(position)
        supportActionBar!!.title = menuItem.title
        when (menuItem.itemId) {

            R.id.navigation_kumiwake -> {
                nowPage = 0
                openFragment(KumiwakeFragment())
                tabLayout.selectTab(tabLayout.getTabAt(0))
                tabLayout.visibility = View.VISIBLE
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Kumiwake.primaryColor))
                supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#FFFFFF'>" + getString(R.string.kumiwake) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                container.background = getDrawable(Theme.Kumiwake.backgroundColor)
                setStatusBarColor(this, Theme.Kumiwake.primaryColor)
                StatusHolder.sekigime = false
                if (StatusHolder.adDeleated) {
                    mAdView.visibility = View.GONE
                } else {
                    mAdView.visibility = View.VISIBLE
                }
                true
            }

            R.id.navigation_others -> {
                nowPage = 1
                openFragment(OthersFragment())
                tabLayout.visibility = View.GONE
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Others.primaryColor))
                supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#FFFFFF'>" + getString(R.string.others) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                container.background = getDrawable(Theme.Others.backgroundColor)
                setStatusBarColor(this, Theme.Others.primaryColor)
                if (StatusHolder.adDeleated) {
                    mAdView.visibility = View.GONE
                } else {
                    mAdView.visibility = View.VISIBLE
                }
                true
            }

            R.id.navigation_members -> {
                nowPage = 2
                tabLayout.visibility = View.GONE
                openFragment(MembersFragment())
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Member.primaryColor))
                supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#FFFFFF'>" + getString(R.string.member) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                container.background = ColorDrawable(Theme.Member.backgroundColor)
                setStatusBarColor(this, Theme.Member.primaryColor)
                mAdView.visibility = View.GONE
                true
            }

            R.id.navigation_settings -> {
                nowPage = 3
                openFragment(SettingsFragment())
                tabLayout.visibility = View.GONE
                supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Setting.primaryColor))
                supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#616161'>" + getString(R.string.setting_help) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                container.background = getDrawable(Theme.Setting.backgroundColor)
                setStatusBarColor(this, Theme.Setting.primaryColor)
                mAdView.visibility = View.GONE
                true
            }
            else -> false
        }
    }

    private val tabItemSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            when (tab.position) {
                0 -> {//組み分け
                    StatusHolder.sekigime = false
                    openFragment(KumiwakeFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Kumiwake.primaryColor))
                    supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#FFFFFF'>" + getString(R.string.kumiwake) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                    container.background = getDrawable(Theme.Kumiwake.backgroundColor)
                    setStatusBarColor(this@MainActivity, Theme.Kumiwake.primaryColor)
                }
                1 -> {//席決め
                    StatusHolder.sekigime = true
                    openFragment(SekigimeFragment())
                    supportActionBar!!.setBackgroundDrawable(getDrawable(Theme.Sekigime.primaryColor))
                    supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#616161'>" + getString(R.string.sekigime) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                    container.background = getDrawable(Theme.Sekigime.backgroundColor)
                    setStatusBarColor(this@MainActivity, Theme.Sekigime.primaryColor)
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
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
        supportActionBar!!.title = HtmlCompat.fromHtml("<font color='#FFFFFF'>" + getString(R.string.kumiwake) + "</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
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

    companion object {
        lateinit var mAdView: AdView
        var nowPage = 0
    }
}
