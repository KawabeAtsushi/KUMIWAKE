package com.pandatone.kumiwake.ui.members

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.CustomPagerAdapter
import com.pandatone.kumiwake.member.*
import com.pandatone.kumiwake.ui.DialogWarehouse
import com.pandatone.kumiwake.ui.settings.MembersViewModel
import com.pandatone.kumiwake.ui.settings.MembersViewModel2
import java.io.IOException

class MembersFragment2 : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var membersViewModel: MembersViewModel2
    var page = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        membersViewModel =
                ViewModelProviders.of(this).get(MembersViewModel2::class.java)
        val root = inflater.inflate(R.layout.fragment_members2, container, false)

        setViews(root)

        return root
    }

    private fun setViews(root :View) {
        viewPager = root.findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(activity!!.supportFragmentManager)
        viewPager.adapter = adapter
        decision = root.findViewById<View>(R.id.decisionBt) as Button

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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.member_menu, menu)
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
        val ssb = SpannableStringBuilder("　")
        // ヒントテキスト
        ssb.append(getText(R.string.search_view))
        // ヒントアイコン
        val searchHintIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_search_black_24dp, null)
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
        removeIcon.setImageResource(R.drawable.ic_close_black_24dp)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val adapter = CustomPagerAdapter(activity!!.supportFragmentManager)
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

        var startAction: Boolean = false
        var kumiwake_select: Boolean = false
        var groupId: Int = 0

        var memberArray: ArrayList<Name> = ArrayList()
    }
}