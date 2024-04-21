package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.ModeKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.EditGroupViewAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.databinding.KumiwakeCustomBinding
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class KumiwakeCustom : AppCompatActivity() {
    private lateinit var binding: KumiwakeCustomBinding
    private lateinit var memberListView: ListView
    private lateinit var groupListView: ListView
    private var mbAdapter: SmallMBListAdapter? = null
    private var editGPAdapter: EditGroupViewAdapter? = null
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var newGroupArray: ArrayList<Group>
    private var leaderArray: ArrayList<Member?> = ArrayList()
    private var screenHeight = 0
    private var nextSet = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setTheme(StatusHolder.nowTheme)
        binding = KumiwakeCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            val layout = findViewById<ConstraintLayout>(R.id.custom_root_layout)
            layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_background)
        }

        if (intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray =
                intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (intent.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray =
                intent.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }
        findViews()
        mbAdapter = SmallMBListAdapter(this, memberArray, showLeaderNo = true)
        editGPAdapter = EditGroupViewAdapter(this, groupArray, groupListView)
        setViews()
        memberListView.adapter = mbAdapter
        groupListView.adapter = editGPAdapter

        setKeyboardListener()
        memberListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            memberListView.requestFocus()
            changeLeader(position)
        }
        val leaderArrayB: Array<Member?> = arrayOfNulls(groupArray.size) //n番目にグループnのリーダーを格納
        leaderArray = leaderArrayB.toCollection(ArrayList())
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
        binding.addGroupListView.memberAddBtn.visibility = View.GONE
        binding.addGroupListView.memberRegisterAndAddBtn.visibility = View.GONE
        mbAdapter?.setRowHeight(memberListView)
        editGPAdapter?.setRowHeight(groupListView)
        binding.addGroupListView.numberOfSelectedMember.text =
            memberArray.size.toString() + getString(R.string.people)
        binding.groupNoTxt.text = groupArray.size.toString() + " " + getText(R.string.group)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<Button>(R.id.normal_kumiwake_button).setOnClickListener { onNextClicked() }
        findViewById<Button>(R.id.back_to_initial_mbNo).setOnClickListener { onBCClicked() }
    }

    //組み分け確認画面に遷移ボタン
    private fun onNextClicked() {
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
            val intent = Intent(this, KumiwakeConfirmation::class.java)
            intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
            intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, newGroupArray)
            intent.putExtra(KumiwakeArrayKeys.LEADER_LIST.key, leaderArray)
            intent.putExtra(
                KumiwakeCustomKeys.EVEN_FM_RATIO.key,
                binding.evenFmRatioCheck.isChecked
            )
            intent.putExtra(
                KumiwakeCustomKeys.EVEN_AGE_RATIO.key,
                binding.evenAgeRatioCheck.isChecked
            )
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            binding.errorMemberNoTxt.visibility = View.VISIBLE
        }
    }

    //初期状態に戻すボタン
    private fun onBCClicked() {
        var et: EditText
        for (i in 0 until groupListView.count) {
            et = groupListView.getChildAt(i)
                .findViewById<View>(R.id.editTheNumberOfMember) as EditText
            et.isFocusable = false
            et.setText(groupArray[i].belongNo.toString())
            et.isFocusableInTouchMode = true
            et.setTextColor(Color.BLACK)
        }
    }

    //リーダーの選択・解除処理
    @SuppressLint("SetTextI18n")
    fun changeLeader(position: Int) {
        val selectedMember = memberArray[position]
        //リーダー解除
        if (leaderArray.contains(selectedMember)) {
            nextSet = leaderArray.indexOf(selectedMember)
            leaderArray[nextSet] = null
            val leader =
                groupListView.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + getText(R.string.nothing)
        } else if (nextSet != -1) {//最大数登録したら終わり(indexが-1返される)
            //リーダー登録
            leaderArray[nextSet] = selectedMember
            val leader =
                groupListView.getChildAt(nextSet).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + selectedMember.name
            nextSet = leaderArray.indexOfFirst { it == null }
        }

        mbAdapter =
            SmallMBListAdapter(this, memberArray, leaderArray = leaderArray, showLeaderNo = true)
        memberListView.adapter = mbAdapter
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

    //キーボードによるレイアウト崩れを防ぐ
    private fun setKeyboardListener() {
        val activityRootView = findViewById<View>(R.id.custom_root_layout)
        val view = findViewById<View>(R.id.normal_kumiwake_button)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
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
}