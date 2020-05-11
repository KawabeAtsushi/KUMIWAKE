package com.pandatone.kumiwake.others.role

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.EditGroupViewAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import kotlinx.android.synthetic.main.kumiwake_custom.*


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class RoleDefine : AppCompatActivity() {
    private lateinit var roleListView: ListView
    private lateinit var totalAssinedTextView: TextView
    private var editRoleAdapter: EditGroupViewAdapter? = null
    private lateinit var memberArray: ArrayList<Member>
    private var roleArray: ArrayList<Group> = ArrayList()
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.role_difinition)

        if (intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        findViews()
        onAddRole()
        editRoleAdapter = EditGroupViewAdapter(this, roleArray, custom_scroll, roleListView, totalAssinedTextView)
        setViews()
        roleListView.adapter = editRoleAdapter

        setKeyboardListener()
    }

    //View宣言
    private fun findViews() {
        val title = getString(R.string.member) + " " + memberArray.size + getString(R.string.people)
        findViewById<TextView>(R.id.member_no_txt).text = title
        roleListView = findViewById(R.id.ticketListView)
        totalAssinedTextView = findViewById<TextView>(R.id.total_ticket_no)
        val totalStr = getString(R.string.assigned) + "0" + getString(R.string.people)
        totalAssinedTextView.text = totalStr
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<Button>(R.id.normal_kumiwake_button).setOnClickListener { onNextClicked() }
        findViewById<Button>(R.id.add_ticket).setOnClickListener { onAddRole() }
    }


    //組み分け確認画面に遷移ボタン
    private fun onNextClicked() {
        var memberSum = 0
        var allowToNext: Boolean = true
        for (i in 0 until roleListView.count) {
            val memberNo = editRoleAdapter!!.getMemberNo(i)
            memberSum += memberNo
            if (memberNo < 0 || memberSum > memberArray.size) {
                allowToNext = false
            }
        }

        if (allowToNext) {
            val intent = Intent(this, RoleConfirmation::class.java)
            intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
            intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, createNextRoleArray())
            intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, even_fm_ratio_check.isChecked)
            intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, even_age_ratio_check.isChecked)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            error_member_no_txt.visibility = View.VISIBLE
        }
    }

    //役職追加
    private fun onAddRole() {
        val roleNo = roleArray.size
        val roleHint = getString(R.string.role) + " " + (roleNo + 1).toString()
        updateRoleArray()
        roleArray.add(Group(roleNo, roleHint, "", 1))
        editRoleAdapter?.notifyDataSetChanged()
        editRoleAdapter?.setRowHeight(roleListView)
    }

    //roleArrayの内容更新
    private fun updateRoleArray() {
        for (i in 0 until roleListView.count) {
            val roleName = editRoleAdapter!!.getGroupName(i)
            val memberNo = editRoleAdapter!!.getMemberNo(i)
            val role = roleArray[i]
            role.name = roleName
            role.belongNo = memberNo
        }
    }

    private fun createNextRoleArray(): ArrayList<Group> {
        val nextRoleArray: ArrayList<Group> = ArrayList()
        var total = 0
        for (i in 0 until roleListView.count) {
            val roleName = editRoleAdapter!!.getGroupName(i)
            val memberNo = editRoleAdapter!!.getMemberNo(i)
            total += memberNo
            if (memberNo != 0) {
                nextRoleArray.add(Group(0, roleName, "", memberNo))
            }
        }
        if (total < memberArray.size) {
            nextRoleArray.add(Group(1, getString(R.string.no_assigned), "", memberArray.size - total))
        }
        return nextRoleArray
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
}