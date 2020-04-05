package com.pandatone.kumiwake.history

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
        val toolbar = findViewById<View>(R.id.history_toolbar) as Toolbar
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
        decision = findViewById<View>(R.id.decisionBt) as Button

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            //スクロール中（page切り替え中）に呼ばれる
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //tableNo:遷移中pageのindex
                //positionOffset:遷移前→遷移後のoffset割合を0~1で返す
                //positionOffsetPixels:positionOffsetをpixelで返す
                if (position == 0) {
                    decision.visibility = View.VISIBLE
                } else {
                    decision.visibility = View.GONE
                }
                decision.translationY = 1000 * positionOffset * (1 - 2 * position) //0to1->up,1to0->down
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
                val allSelect = menu.findItem(R.id.item_all_select)
                allSelect.isVisible = (page == 0)
            }
        })

        decision.visibility = View.VISIBLE

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
        lateinit var decision: Button
        lateinit var viewPager: ViewPager
    }

}