package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.EditGroupListAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.kumiwake_custom.*
import kotlinx.android.synthetic.main.part_review_listview.*
import java.util.*


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class KumiwakeCustom : AppCompatActivity() {
    private var mbAdapter: MBListViewAdapter? = null
    private var gpAdapter: EditGroupListAdapter? = null
    private lateinit var memberArray: ArrayList<Name>
    private lateinit var groupArray: ArrayList<GroupListAdapter.Group>
    private lateinit var newGroupArray: ArrayList<GroupListAdapter.Group>
    private var nextSet = 0
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.kumiwake_custom)
        ButterKnife.bind(this)

        if (intent.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) != null) {
            memberArray = intent.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) as ArrayList<Name>
        }
        if (intent.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) != null) {
            groupArray = intent.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) as ArrayList<GroupListAdapter.Group>
        }
        mbAdapter = MBListViewAdapter(this, memberArray, true)
        gpAdapter = EditGroupListAdapter(this, groupArray, custom_scroll)
        findViews()
        setViews()
        memberList.adapter = mbAdapter
        groupList.adapter = gpAdapter

        setKeyboardListener()

        memberList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            memberList.requestFocus()
            changeLeader(position)
        }

        custom_scroll.post { custom_scroll.fullScroll(ScrollView.FOCUS_UP) }

        leaderNoList = arrayOfNulls(groupArray.size) //n番目にグループnのリーダーのidを格納
    }


    private fun findViews() {
        memberList = findViewById(R.id.memberListView)
        groupList = findViewById(R.id.groupListView)
        memberList.emptyView = findViewById(R.id.emptyMemberList)
    }

    @SuppressLint("SetTextI18n")
    fun setViews() {
        member_add_btn.visibility = View.GONE
        mbAdapter?.let { MBListViewAdapter.setRowHeight(memberList, it) }
        gpAdapter?.let { EditGroupListAdapter.setRowHeight(groupList, it) }
        numberOfSelectedMember.text = memberArray.size.toString() + getString(R.string.people)
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
    }

    @OnClick(R.id.normal_kumiwake_button)
    internal fun onClicked() {
        var memberSum = 0
        var allowToNext: Boolean? = true
        for (i in 0 until groupList.count) {
            val memberNo = EditGroupListAdapter.getMemberNo(i)
            memberSum += memberNo
            if (memberNo <= 0 || memberSum > memberArray.size) {
                allowToNext = false
            }
        }

        if (allowToNext!!) {
            recreateGrouplist()
            val intent = Intent(this, NormalKumiwakeConfirmation::class.java)
            intent.putExtra(NormalMode.NORMAL_MEMBER_ARRAY, memberArray)
            intent.putExtra(NormalMode.NORMAL_GROUP_ARRAY, newGroupArray)
            intent.putExtra(EVEN_FM_RATIO, even_fm_ratio_check.isChecked)
            intent.putExtra(EVEN_AGE_RATIO, even_age_ratio_check.isChecked)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            error_member_no_txt.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.back_to_initial_mbNo)
    internal fun onbcClicked() {
        var et: EditText
        for (i in 0 until groupList.count) {
            et = groupList.getChildAt(i).findViewById<View>(R.id.editTheNumberOfMember) as EditText
            et.isFocusable = false
            et.setText(groupArray[i].belongNo.toString())
            et.isFocusableInTouchMode = true
            et.setTextColor(Color.BLACK)
        }
    }

    @SuppressLint("SetTextI18n")
    fun changeLeader(position: Int) {
        val id = memberArray[position].id
        val name = memberArray[position].name

        //リーダー解除
        if (leaderNoList.contains(id)) {

            nextSet = leaderNoList.indexOf(id)
            leaderNoList[nextSet] = null
            val leader = groupList.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + getText(R.string.nothing)

        } else if (nextSet != -1) {//最大数登録したら終わり(indexが-1返される)
            //リーダー登録
            leaderNoList[nextSet] = id
            val leader = groupList.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + name
            nextSet = leaderNoList.indexOfFirst { it == null }
        }

        mbAdapter?.notifyDataSetChanged()
    }

    private fun recreateGrouplist() {
        newGroupArray = ArrayList()
        for (i in 0 until groupList.count) {
            val groupName = EditGroupListAdapter.getGroupName(i)
            val memberNo = EditGroupListAdapter.getMemberNo(i)
            newGroupArray.add(GroupListAdapter.Group(i, groupName, "", memberNo))
        }
    }

    fun changeBelongNo(position: Int, addNo: Int) {
        val et: EditText = if (position == groupList.count - 1) {
            groupList.getChildAt(0).findViewById<View>(R.id.editTheNumberOfMember) as EditText
        } else {
            groupList.getChildAt(position + 1).findViewById<View>(R.id.editTheNumberOfMember) as EditText
        }
        var nowNo = 0
        val newNo: Int

        if (et.text.toString().isNotEmpty()) {
            nowNo = Integer.parseInt(et.text.toString())
        }
        newNo = nowNo + addNo
        et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
        et.setText(newNo.toString())
        if (newNo < 0) {
            et.setTextColor(Color.RED)
        } else {
            et.setTextColor(Color.BLACK)
        }
    }

    private fun setKeyboardListener() {
        val activityRootView = findViewById<View>(R.id.custom_root_layout)
        val view = findViewById<View>(R.id.normal_kumiwake_button)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val r = Rect()

            override fun onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r)
                val heightDiff = activityRootView.rootView.height - r.height()
                if (heightDiff > screenHeight * 0.2) {
                    view.visibility = View.GONE
                } else {
                    view.visibility = View.VISIBLE
                }
            }
        })
    }

    companion object {

        const val EVEN_FM_RATIO = "even_fm_ratio"
        const val EVEN_AGE_RATIO = "even_age_ratio"

        internal var leaderNoList: Array<Int?> = emptyArray()
        internal lateinit var memberList: ListView
        internal lateinit var groupList: ListView

    }

}