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
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.EditOthersViewAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import kotlinx.android.synthetic.main.kumiwake_custom.even_age_ratio_check
import kotlinx.android.synthetic.main.kumiwake_custom.even_fm_ratio_check
import kotlinx.android.synthetic.main.ticket_difinition.error_incorrect_number


/**
 * Created by atsushi_2 on 2016/05/27.
 */
class RoleDefine : AppCompatActivity() {
    private lateinit var roleListView: ListView
    private lateinit var totalAssignedTextView: TextView
    private var editRoleAdapter: EditOthersViewAdapter? = null
    private lateinit var memberArray: ArrayList<Member>
    private var roleArray: ArrayList<Group> = ArrayList()
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.role_difinition)

        if (intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray =
                intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        findViews()
        editRoleAdapter = EditOthersViewAdapter(this, roleArray, totalAssignedTextView, false)
        onAddRole()
        setViews()
        roleListView.adapter = editRoleAdapter

        setKeyboardListener()
    }

    //View宣言
    private fun findViews() {
        val title = getString(R.string.member) + " " + memberArray.size + getString(R.string.people)
        findViewById<TextView>(R.id.member_no_txt).text = title
        roleListView = findViewById(R.id.ticketListView)
        totalAssignedTextView = findViewById(R.id.total_ticket_no)
        val totalStr = getString(R.string.assigned) + "0" + getString(R.string.people)
        totalAssignedTextView.text = totalStr
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<Button>(R.id.normal_kumiwake_button).setOnClickListener { onNextClicked() }
        findViewById<ImageButton>(R.id.add_role).setOnClickListener { onAddRole() }
    }


    //組み分け確認画面に遷移ボタン
    private fun onNextClicked() {
        var memberSum = 0
        var allowToNext = true
        for (i in 0 until roleListView.count) {
            val memberNo = editRoleAdapter!!.getNumber(i, false)
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
            error_incorrect_number.visibility = View.VISIBLE
        }
    }

    //役職追加
    private fun onAddRole() {
        val roleNo = roleArray.size
        updateRoleArray()
        roleArray.add(Group(roleNo, "", "", -1))
        editRoleAdapter?.notifyDataSetChanged()
        editRoleAdapter?.setRowHeight(roleListView)
        editRoleAdapter?.setTotalCount()
    }

    //roleArrayの内容更新
    private fun updateRoleArray() {
        for (i in 0 until roleListView.count) {
            val roleName = editRoleAdapter!!.getName(i, true)
            val memberNo = editRoleAdapter!!.getNumber(i, true)
            val role = roleArray[i]
            role.name = roleName
            role.belongNo = memberNo
        }
    }

    private fun createNextRoleArray(): ArrayList<Group> {
        val nextRoleArray: ArrayList<Group> = ArrayList()
        var total = 0
        for (i in 0 until roleListView.count) {
            val roleName = editRoleAdapter!!.getName(i, false)
            val memberNo = editRoleAdapter!!.getNumber(i, false)
            total += memberNo
            if (memberNo != 0) {
                nextRoleArray.add(Group(0, roleName, "", memberNo))
            }
            FirebaseAnalyticsEvents.roleNames(roleName)
        }
        if (total < memberArray.size) {
            nextRoleArray.add(
                Group(
                    1,
                    getString(R.string.no_assigned),
                    "",
                    memberArray.size - total
                )
            )
        }
        return nextRoleArray
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