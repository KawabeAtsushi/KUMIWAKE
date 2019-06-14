package com.pandatone.kumiwake.member

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.CustomPagerAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import kotlinx.android.synthetic.main.member_main.*
import java.io.IOException
import java.util.*

/**
 * Created by atsushi_2 on 2016/02/19.
 */
class MemberMain : AppCompatActivity(), SearchView.OnQueryTextListener {

    lateinit var memberArray: ArrayList<Name>
    internal var manager = supportFragmentManager
    internal lateinit var viewPager: ViewPager
    internal var start_actionmode: Boolean = false
    internal var kumiwake_select: Boolean = false
    internal var groupId: Int = 0
    internal var delete_icon_visible: Boolean = false
    internal var page: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_main)
        val toolbar = findViewById<View>(R.id.tool_bar_2) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.member_main)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setViews()
        val i = intent
        start_actionmode = i.getBooleanExtra("START_ACTIONMODE", false)
        groupId = i.getIntExtra("GROUP_ID", -1)
        delete_icon_visible = i.getBooleanExtra("delete_icon_visible", true)
        kumiwake_select = i.getBooleanExtra("kumiwake_select", false)
        memberArray = i.getSerializableExtra("memberArray") as ArrayList<Name>
    }

    private fun setViews() {
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(manager)
        viewPager.adapter = adapter
        decision = findViewById<View>(R.id.decisionBt) as Button
    }

    private fun visibleViews() {
        if (intent.getBooleanExtra("visible", false)) {
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
        viewPager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                page = viewPager.currentItem
                val itemfilter = menu.findItem(R.id.item_filter)
                itemfilter.isVisible = page != 1
            }
        })

        searchView = MenuItemCompat.getActionView(menu.findItem(R.id.search_view)) as SearchView
        searchView.setOnQueryTextListener(this)
        val searchAutoComplete = searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        // ActionBarの検索アイコン
        val searchIcon = searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_button) as ImageView
        searchIcon.setImageResource(android.R.drawable.ic_menu_search)
        val ssb = SpannableStringBuilder("　")
        // ヒントテキスト
        ssb.append(getText(R.string.search_view))
        // ヒントアイコン
        val searchHintIcon = resources.getDrawable(android.R.drawable.ic_menu_search)
        val textSize = (searchAutoComplete.textSize * 1.25).toInt()
        searchHintIcon.setBounds(0, 0, textSize, textSize)
        ssb.setSpan(ImageSpan(searchHintIcon), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        searchAutoComplete.hint = ssb
        // テキストカラー
        searchAutoComplete.setTextColor(Color.WHITE)
        // ヒントテキストカラー
        searchAutoComplete.setHintTextColor(Color.parseColor("#40000000"))
        // Remove button icon
        val removeIcon = searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_close_btn) as ImageView
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

    public override fun onRestart() {
        super.onRestart()
        reload()
    }

    internal fun moveMember() {
        val intent = Intent(this, AddMember::class.java)
        startActivity(intent)
    }

    internal fun moveGroup() {
        val intent = Intent(this, AddGroup::class.java)
        startActivity(intent)
    }

    internal fun moveKumiwake() {
        val hs = HashSet<Name>()
        hs.addAll(memberArray)
        memberArray.clear()
        memberArray.addAll(hs)
        val i = Intent(this, NormalMode::class.java)
        i.putExtra("memberArray", memberArray)
        setResult(Activity.RESULT_OK, i)
        finish()
    }


    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        try {
            if (page == 0) {
                FragmentMember().selectName(newText)
            } else {
                FragmentGroup.selectGroup(newText)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    fun reload() {
        val i = Intent(this, MemberMain::class.java)
        finish()
        startActivity(i)
    }

    companion object {
        internal lateinit var searchView: SearchView
        internal lateinit var decision: Button
    }

}