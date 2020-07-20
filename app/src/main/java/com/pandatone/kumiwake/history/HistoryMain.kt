package com.pandatone.kumiwake.history

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.*


/**
 * Created by atsushi_2 on 2016/02/19.
 */
class HistoryMain : AppCompatActivity() {

    private val manager = supportFragmentManager
    private var page: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_main)
        setStatus()
        setViews()
        setPageChangeListener()
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    //Viewの宣言・初期化
    private fun setViews() {
        viewPager = findViewById<View>(R.id.history_view_pager) as ViewPager
        val adapter = HistoryPagerAdapter(this, manager)
        viewPager.adapter = adapter
    }

    private fun setStatus(){
        toolbar = findViewById<View>(R.id.history_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.history)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        PublicMethods.setStatus(this, Theme.Others.primaryColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
                if (page == 0) {
                    FragmentHistory.toolbarTitle = getString(R.string.history) + " " + FragmentHistory.historyList.count().toString() + "times"
                    supportActionBar!!.title = FragmentHistory.toolbarTitle
                } else {
                    FragmentKeeps.toolbarTitle = getString(R.string.favorite) + " " + FragmentKeeps.historyList.count().toString() + "♥s"
                    supportActionBar!!.title = FragmentKeeps.toolbarTitle
                }
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewPager = findViewById<View>(R.id.history_view_pager) as ViewPager
        val adapter = HistoryPagerAdapter(this, manager)
        adapter.findFragmentByPosition(viewPager, page).onOptionsItemSelected(item)

        return false
    }

    private fun setPageChangeListener() {
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem

            }
        })
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        // 戻るボタンが押されたとき
        when (e.keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
        }
        return super.dispatchKeyEvent(e)
    }

    companion object {
        lateinit var viewPager: ViewPager
        lateinit var toolbar: Toolbar
    }

}