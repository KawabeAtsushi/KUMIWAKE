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
import butterknife.OnClick
import com.pandatone.kumiwake.ArrayKeys
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.Group
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.member.Member
import kotlinx.android.synthetic.main.normal_mode.*
import kotlinx.android.synthetic.main.part_review_listview.view.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class NormalMode : AppCompatActivity() {
    private var adapter: SmallMBListAdapter? = null
    private lateinit var gpNoEditText: AppCompatEditText
    private lateinit var errorGroup: TextView
    private lateinit var errorMember: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Slide()
        }
        setContentView(R.layout.normal_mode)
        findViews()
        memberArray = ArrayList()
        add_group_listview.member_add_btn.setOnClickListener(){ moveMemberMain() }
        add_group_listview.member_register_and_add_btn.setOnClickListener(){ moveAddMember() }
        add_group_listview.numberOfSelectedMember.text = "0${getString(R.string.people)}${getString(R.string.selected)}"
    }

    //Viewの宣言
    private fun findViews() {
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
        gpNoEditText = findViewById<View>(R.id.group_no_form) as AppCompatEditText
        errorGroup = findViewById<View>(R.id.error_group_no_txt) as TextView
        errorMember = findViewById<View>(R.id.error_member_no_txt) as TextView
    }

    //MemberMainに遷移
    private fun moveMemberMain() {
        val intent = Intent(this, MemberMain::class.java)
        intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, memberArray)
        startActivityForResult(intent, 0)
    }

    //AddMemberに遷移
    private fun moveAddMember() {
        val intent = Intent(this, AddMember::class.java)
        intent.putExtra(AddMemberKeys.FROM_NORMAL_MODE.key, true)
        startActivityForResult(intent, 0) //これで呼ぶとActivityが終わった時にonActivityResultが呼ばれる。
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
            val groupArray = ArrayList<Group>()
            val groupNo = Integer.parseInt(group_no)
            val eachMemberNo = memberArray.size / groupNo
            val remainder = memberArray.size % groupNo

            for (i in 0 until remainder) {
                groupArray.add(Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo + 1))
            }

            for (i in remainder until groupNo) {
                groupArray.add(Group(i, getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo))
            }

            val intent = Intent(this, KumiwakeCustom::class.java)
            intent.putExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key, memberArray)
            intent.putExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key, groupArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra(AddGroupKeys.MEMBER_ARRAY.key) as ArrayList<Member>
        }

        adapter = SmallMBListAdapter(this, memberArray, false, showLeaderNo = false)
        listView.adapter = adapter
        SmallMBListAdapter.setRowHeight(listView, adapter!!)
        add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        internal lateinit var listView: ListView
        internal lateinit var memberArray: ArrayList<Member>
    }

}
