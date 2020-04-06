package com.pandatone.kumiwake.history

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.CustomPagerAdapter
import com.pandatone.kumiwake.member.function.Member


/**
 * Created by atsushi_2 on 2016/02/19.
 */
class HistoryMain : AppCompatActivity() {

    private val manager = supportFragmentManager
    private var page: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_main)
        toolbar = findViewById<View>(R.id.history_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.history)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        setViews()
    }

    //Viewの宣言・初期化
    private fun setViews() {
        viewPager = findViewById<View>(R.id.history_view_pager) as ViewPager
        val adapter = HistoryPagerAdapter(this, manager)
        viewPager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
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
        lateinit var toolbar:Toolbar
    }

}