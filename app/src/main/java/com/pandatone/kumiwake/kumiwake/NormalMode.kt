package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Slide
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.normal_mode.*
import kotlinx.android.synthetic.main.part_review_listview.view.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class NormalMode : AppCompatActivity() {
    private var adapter: MBListViewAdapter? = null
    private lateinit var gpNoEditText: AppCompatEditText
    private lateinit var errorGroup: TextView
    private lateinit var errorMember: TextView

    private val clickAdd = View.OnClickListener { moveMemberMain() }
    private val clickResister = View.OnClickListener { moveAddMember() }

    @SuppressLint("SetTextI18n")
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
        add_group_listview.member_add_btn.setOnClickListener(clickAdd)
        add_group_listview.member_register_and_add_btn.setOnClickListener(clickResister)
        add_group_listview.numberOfSelectedMember.text = "0${getString(R.string.people)}${getString(R.string.selected)}"
    }

    private fun findViews() {
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
        gpNoEditText = findViewById<View>(R.id.group_no_form) as AppCompatEditText
        errorGroup = findViewById<View>(R.id.error_group_no_txt) as TextView
        errorMember = findViewById<View>(R.id.error_member_no_txt) as TextView
    }

    private fun moveMemberMain() {
        val intent = Intent(this, MemberMain::class.java)
        intent.putExtra(MemberMain.MEMBER_ARRAY, memberArray)
        startActivityForResult(intent, 1000)
    }

    private fun moveAddMember() {
        val intent = Intent(this, AddMember::class.java)
        intent.putExtra(AddMember.FROM_NORMAL_MODE, true)
        startActivityForResult(intent, 100) //これで呼ぶとActivityが終わった時にonActivityResultが呼ばれる。
    }

    @OnClick(R.id.normal_kumiwake_btn)
    fun onClicked() {
        val group_no = gpNoEditText.text!!.toString()

        errorGroup.text = ""
        errorMember.text = ""

        if (adapter == null) {
            errorMember.setText(R.string.error_empty_member_list)
        } else if (group_no != "" && Integer.parseInt(group_no) > adapter?.count!!) {
            errorGroup.setText(R.string.number_of_groups_is_much_too)
        } else if (TextUtils.isEmpty(group_no)) {
            errorGroup.setText(R.string.error_empty_group_no)
        } else if (group_no == "0") {
            errorGroup.setText(R.string.require_correct_No)
        } else {
            val groupArray = ArrayList<GroupListAdapter.Group>()
            val groupNo = Integer.parseInt(group_no)
            val eachMemberNo = memberArray.size / groupNo
            val remainder = memberArray.size % groupNo

            for (i in 0 until remainder) {
                groupArray.add(GroupListAdapter.Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo + 1))
            }

            for (i in remainder until groupNo) {
                groupArray.add(GroupListAdapter.Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo))
            }

            val intent = Intent(this, KumiwakeCustom::class.java)
            intent.putExtra(NORMAL_MEMBER_ARRAY, memberArray)
            intent.putExtra(NORMAL_GROUP_ARRAY, groupArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra(MemberMain.MEMBER_ARRAY) as ArrayList<Name>
        }

        adapter = MBListViewAdapter(this, memberArray, false, showLeaderNo = false)
        listView.adapter = adapter
        MBListViewAdapter.setRowHeight(listView, adapter!!)
        add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        internal lateinit var listView: ListView
        internal lateinit var memberArray: ArrayList<Name>
        //intent keys
        const val NORMAL_MEMBER_ARRAY = "normal_memberArray"
        const val NORMAL_GROUP_ARRAY = "normal_groupArray"
    }

}
