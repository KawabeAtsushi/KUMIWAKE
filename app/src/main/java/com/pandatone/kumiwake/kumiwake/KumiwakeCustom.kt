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
import com.pandatone.kumiwake.ArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.EditGroupViewAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.Group
import com.pandatone.kumiwake.member.Member
import kotlinx.android.synthetic.main.kumiwake_custom.*
import kotlinx.android.synthetic.main.part_review_listview.*
import java.util.*


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class KumiwakeCustom : AppCompatActivity() {
    private lateinit var memberListView: ListView
    private lateinit var groupListView: ListView
    private var mbAdapter: SmallMBListAdapter? = null
    private var editGPAdapter: EditGroupViewAdapter? = null
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var newGroupArray: ArrayList<Group>
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.kumiwake_custom)
        ButterKnife.bind(this)

        if (intent.getSerializableExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key) != null) {
            memberArray = intent.getSerializableExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key) as ArrayList<Member>
        }
        if (intent.getSerializableExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key) != null) {
            groupArray = intent.getSerializableExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key) as ArrayList<Group>
        }
        mbAdapter = SmallMBListAdapter(this, memberArray, true, showLeaderNo = true)
        editGPAdapter = EditGroupViewAdapter(this, groupArray, custom_scroll)
        findViews()
        setViews()
        memberListView.adapter = mbAdapter
        groupListView.adapter = editGPAdapter

        setKeyboardListener()
        memberListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            memberListView.requestFocus()
            changeLeader(position)
        }
        custom_scroll.post { custom_scroll.fullScroll(ScrollView.FOCUS_UP) }
        leaderNoList = arrayOfNulls(groupArray.size) //n番目にグループnのリーダーのidを格納
    }

    //View宣言
    private fun findViews() {
        memberListView = findViewById(R.id.memberListView)
        groupListView = findViewById(R.id.groupListView)
        memberListView.emptyView = findViewById(R.id.emptyMemberList)
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        member_add_btn.visibility = View.GONE
        member_register_and_add_btn.visibility = View.GONE
        mbAdapter?.setRowHeight(memberListView)
        editGPAdapter?.setRowHeight(groupListView)
        numberOfSelectedMember.text = memberArray.size.toString() + getString(R.string.people)
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
    }

    //組み分け確認画面に遷移ボタン
    @OnClick(R.id.normal_kumiwake_button)
    internal fun onClicked() {
        var memberSum = 0
        var allowToNext: Boolean? = true
        for (i in 0 until groupListView.count) {
            val memberNo = editGPAdapter!!.getMemberNo(i)
            memberSum += memberNo
            if (memberNo <= 0 || memberSum > memberArray.size) {
                allowToNext = false
            }
        }

        if (allowToNext!!) {
            createGroupArray()
            val intent = Intent(this, NormalKumiwakeConfirmation::class.java)
            intent.putExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key, memberArray)
            intent.putExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key, newGroupArray)
            intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, even_fm_ratio_check.isChecked)
            intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, even_age_ratio_check.isChecked)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            error_member_no_txt.visibility = View.VISIBLE
        }
    }

    //初期状態に戻すボタン
    @OnClick(R.id.back_to_initial_mbNo)
    internal fun onBCClicked() {
        var et: EditText
        for (i in 0 until groupListView.count) {
            et = groupListView.getChildAt(i).findViewById<View>(R.id.editTheNumberOfMember) as EditText
            et.isFocusable = false
            et.setText(groupArray[i].belongNo.toString())
            et.isFocusableInTouchMode = true
            et.setTextColor(Color.BLACK)
        }
    }

    //リーダーの選択・解除処理
    @SuppressLint("SetTextI18n")
    fun changeLeader(position: Int) {
        val id = memberArray[position].id
        val name = memberArray[position].name
        var nextSet = 0

        //リーダー解除
        if (leaderNoList.contains(id)) {

            nextSet = leaderNoList.indexOf(id)
            leaderNoList[nextSet] = null
            val leader = groupListView.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + getText(R.string.nothing)

        } else if (nextSet != -1) {//最大数登録したら終わり(indexが-1返される)
            //リーダー登録
            leaderNoList[nextSet] = id
            val leader = groupListView.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + name
            nextSet = leaderNoList.indexOfFirst { it == null }
        }

        mbAdapter?.notifyDataSetChanged()
    }

    //GroupArrayの作成
    private fun createGroupArray() {
        newGroupArray = ArrayList()
        for (i in 0 until groupListView.count) {
            val groupName = editGPAdapter!!.getGroupName(i)
            val memberNo = editGPAdapter!!.getMemberNo(i)
            newGroupArray.add(Group(i, groupName, "", memberNo))
        }
    }

    //人数配分の自動調整
    fun changeBelongNo(position: Int, addNo: Int) {
        val et: EditText = if (position == groupListView.count - 1) {
            groupListView.getChildAt(0).findViewById<View>(R.id.editTheNumberOfMember) as EditText
        } else {
            groupListView.getChildAt(position + 1).findViewById<View>(R.id.editTheNumberOfMember) as EditText
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

    //キーボードによるレイアウト崩れを防ぐ
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
        internal var leaderNoList: Array<Int?> = emptyArray()
    }

}