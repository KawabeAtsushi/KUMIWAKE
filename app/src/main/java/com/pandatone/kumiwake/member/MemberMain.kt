package com.pandatone.kumiwake.member

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.CustomPagerAdapter
import java.io.IOException


/**
 * Created by atsushi_2 on 2016/02/19.
 */
class MemberMain : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val manager = supportFragmentManager
    private var page: Int = 0
    val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_main)
        val toolbar = findViewById<View>(R.id.tool_bar_2) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.member_main)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val i = intent
        startAction = i.getBooleanExtra(ACTION_MODE, false)
        groupId = i.getIntExtra(GROUP_ID, -1)
        kumiwake_select = i.getBooleanExtra(NORMAL_SELECT, false)
        if (i.getSerializableExtra(MEMBER_ARRAY) != null) {
            memberArray = i.getSerializableExtra(MEMBER_ARRAY) as ArrayList<Name>
        }

        setViews()
    }

    private fun setViews() {
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(manager)
        viewPager.adapter = adapter
        decision = findViewById<View>(R.id.decisionBt) as Button

        if (startAction) {
            viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                //スクロール中（page切り替え中）に呼ばれる
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //position:遷移中pageのindex
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
    }

    private fun visibleViews() {
        if (startAction) {
            decision.visibility = View.VISIBLE
            FragmentGroup.adviceInFG.visibility = View.VISIBLE
            FragmentMember.fab.hide()
            FragmentGroup.fab.hide()
            FragmentMember.fab.isEnabled = false
            FragmentGroup.fab.isEnabled = false
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.member_main_menu, menu)
        val delete = menu.findItem(R.id.item_delete)
        delete.isVisible = false
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
                val itemFilter = menu.findItem(R.id.item_filter)
                itemFilter.isVisible = page != 1
                val allSelect = menu.findItem(R.id.item_all_select)
                allSelect.isVisible = !(startAction && page == 1)
            }
        })

        searchView = menu.findItem(R.id.search_view).actionView as SearchView
        searchView.setOnQueryTextListener(this)
        val searchAutoComplete = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        // ActionBarの検索アイコン
        val searchIcon = searchView.findViewById<View>(androidx.appcompat.R.id.search_button) as ImageView
        searchIcon.setImageResource(android.R.drawable.ic_menu_search)
        val ssb = SpannableStringBuilder("　")
        // ヒントテキスト
        ssb.append(getText(R.string.search_view))
        // ヒントアイコン
        val searchHintIcon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_menu_search, null)
        val textSize = (searchAutoComplete.textSize * 1.25).toInt()
        searchHintIcon?.setBounds(0, 0, textSize, textSize)
        ssb.setSpan(searchHintIcon?.let { ImageSpan(it) }, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        searchAutoComplete.hint = ssb
        // テキストカラー
        searchAutoComplete.setTextColor(Color.WHITE)
        // ヒントテキストカラー
        searchAutoComplete.setHintTextColor(Color.parseColor("#40000000"))
        // Remove button icon
        val removeIcon = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        removeIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        removeIcon.setOnClickListener {
            if (searchAutoComplete.text.toString() != "") {
                searchAutoComplete.setText("")
            } else {
                searchView.onActionViewCollapsed()
                FragmentMember().loadName()
                FragmentGroup().loadName()
            }
        }

        visibleViews()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(manager)
        adapter.findFragmentByPosition(viewPager, page).onOptionsItemSelected(item)

        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        try {
            if (page == 0) {
                FragmentMember().selectName(newText)
            } else {
                FragmentGroup().selectGroup(newText)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    companion object {
        lateinit var searchView: SearchView
        lateinit var decision: Button
        lateinit var viewPager: ViewPager

        //intent key
        const val ACTION_MODE = "action_mode"
        const val GROUP_ID = "group_id"
        const val NORMAL_SELECT = "normal_select"
        const val MEMBER_ARRAY = "memberArray"

        var startAction: Boolean = false
        var kumiwake_select: Boolean = false
        var groupId: Int = 0

        var memberArray: ArrayList<Name> = ArrayList()
    }

}