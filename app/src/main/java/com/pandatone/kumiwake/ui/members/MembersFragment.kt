package com.pandatone.kumiwake.ui.members

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.CustomPagerAdapter
import java.io.IOException

class MembersFragment : Fragment(), SearchView.OnQueryTextListener {

    var page = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_members, container, false)
        setViews(root)
        return root
    }

    //Viewの宣言・初期化
    private fun setViews(root: View) {
        viewPager = root.findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = CustomPagerAdapter(context!!, childFragmentManager, true)
        viewPager.adapter = adapter
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
            }
        })

        searchView = menu.findItem(R.id.search_view).actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setOnClickListener { menuItemVisible(menu, false) }
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
                FragmentMemberMain().loadName()
                FragmentGroupMain().loadName()
            }
            menuItemVisible(menu, true)
        }
    }

    private fun menuItemVisible(menu: Menu, visibility: Boolean) {
        menu.findItem(R.id.item_all_select).isVisible = visibility
        menu.findItem(R.id.item_sort).isVisible = visibility
        menu.findItem(R.id.item_filter).isVisible = visibility && page != 1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val adapter = CustomPagerAdapter(context!!, childFragmentManager, true)
        adapter.findFragmentByPosition(viewPager, page).onOptionsItemSelected(item)
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    //検索ボックスの文字入力のたびに呼ばれる
    override fun onQueryTextChange(newText: String): Boolean {
        try {
            if (page == 0) {
                FragmentMemberMain().searchMember(newText)
            } else {
                FragmentGroupMain().searchGroup(newText)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    companion object {
        lateinit var searchView: SearchView
        lateinit var viewPager: ViewPager
    }
}