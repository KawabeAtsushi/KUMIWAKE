package com.pandatone.kumiwake.member

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
import com.pandatone.kumiwake.extension.getSerializable
import com.pandatone.kumiwake.member.function.Member


/**
 * Created by atsushi_2 on 2016/02/19.
 */
class ChoiceMemberMain : AppCompatActivity() {

    private val manager = supportFragmentManager
    private var page: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_main)
        val toolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.title = "      0" + getString(R.string.selected)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        intent.getSerializable<ArrayList<Member>>(AddGroupKeys.MEMBER_ARRAY.key)
            ?.let { memberArray = it }

        setViews()
    }

    //Viewの宣言・初期化
    private fun setViews() {
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(this, manager, false)
        viewPager.adapter = adapter
        decision = findViewById<View>(R.id.decisionBt) as Button

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            //スクロール中（page切り替え中）に呼ばれる
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                //tableNo:遷移中pageのindex
                //positionOffset:遷移前→遷移後のoffset割合を0~1で返す
                //positionOffsetPixels:positionOffsetをpixelで返す
                if (position == 0) {
                    decision.visibility = View.VISIBLE
                } else {
                    decision.visibility = View.GONE
                }
                decision.translationY =
                    1000 * positionOffset * (1 - 2 * position) //0to1->up,1to0->down
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.member_menu, menu)
        val searchIcon = menu.findItem(R.id.search_view)
        val deleteIcon = menu.findItem(R.id.item_delete)
        searchIcon.isVisible = false
        deleteIcon.isVisible = false
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
                val itemFilter = menu.findItem(R.id.item_filter)
                itemFilter.isVisible = (page == 0)
                val allSelect = menu.findItem(R.id.item_all_select)
                allSelect.isVisible = (page == 0)
            }
        })

        decision.visibility = View.VISIBLE

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(this, manager, false)
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
        var memberArray: ArrayList<Member> = ArrayList()
    }

}