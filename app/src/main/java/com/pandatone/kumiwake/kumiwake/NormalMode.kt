package com.pandatone.kumiwake.kumiwake

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.transition.Slide
import android.view.View
import android.view.Window
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.normal_mode.*
import kotlinx.android.synthetic.main.part_review_listview.view.*
import kotlinx.android.synthetic.main.quick_mode.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class NormalMode : AppCompatActivity() {
    private lateinit var adapter: MBListViewAdapter

    private val clicked = View.OnClickListener { moveMemberMain() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Slide()
        }
        setContentView(R.layout.normal_mode)
        ButterKnife.bind(this)
        findViews()
        memberArray = ArrayList()
        add_group_listview.member_add_btn.setOnClickListener(clicked)
        add_group_listview.numberOfSelectedMember.text = "0${getString(R.string.person)}${getString(R.string.selected)}"
    }

    fun findViews() {
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.reviewListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
    }

    fun moveMemberMain() {
        val intent = Intent(this, MemberMain::class.java)
        intent.putExtra("visible", true)
        intent.putExtra("delete_icon_visible", false)
        intent.putExtra("START_ACTIONMODE", true)
        intent.putExtra("kumiwake_select", true)
        intent.putExtra("memberArray", memberArray)
        startActivityForResult(intent, 1000)
    }


    @OnClick(R.id.normal_kumiwake_button)
    internal fun onClicked() {
        val group_no = `@+id/group_no_form`.text!!.toString()

        if (TextUtils.isEmpty(group_no)) {
            error_group_no_txt.setText(R.string.error_empty_group_no)
            scrollToTop()
        }
        if (group_no != "" && Integer.parseInt(group_no) > adapter.count) {
            error_group_no_txt.setText(R.string.number_of_groups_is_much_too)
            error_group_no_txt.text = ""
            scrollToTop()
        } else if (TextUtils.isEmpty(group_no)) {
            error_group_no_txt.setText(R.string.error_empty_group_no)
            error_group_no_txt.text = ""
            scrollToTop()
        } else {
            val groupArray = ArrayList<GroupListAdapter.Group>()
            val groupNo = Integer.parseInt(group_no)
            val eachMemberNo = memberArray.size / groupNo
            val remainder = memberArray.size % groupNo

            for (i in 0 until remainder) {
                groupArray.add(GroupListAdapter.Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), eachMemberNo + 1, null.toString()))
            }

            for (i in remainder until groupNo) {
                groupArray.add(GroupListAdapter.Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), eachMemberNo, null.toString()))
            }

            val intent = Intent(this, KumiwakeCustom::class.java)
            intent.putExtra("NormalModeMemberArray", memberArray)
            intent.putExtra("NormalModeGroupArray", groupArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra("memberArray") as ArrayList<Name>
            adapter = MBListViewAdapter(this, memberArray, 1000)
            listView.adapter = adapter
            MBListViewAdapter.setRowHeight(listView, adapter)

            add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.person)}${getString(R.string.selected)}"
        }
    }

    fun scrollToTop() {
        normal_mode_scrollView.post { normal_mode_scrollView.scrollTo(0, 0) }
    }

    companion object {
        internal lateinit var listView: ListView
        internal lateinit var memberArray: ArrayList<Name>
    }

}
