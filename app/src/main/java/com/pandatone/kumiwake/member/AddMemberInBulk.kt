package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.AddInBulkViewAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.others.SelectMember
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddMemberInBulk : AppCompatActivity() {
    private var memberArray: ArrayList<Member> = ArrayList()
    private lateinit var memberListView: ListView
    private var mbAdapter: MemberAdapter? = null
    private var fromMode: String? = "member"
    private var editViewAdapter: AddInBulkViewAdapter? = null
    private var screenHeight = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.add_member_in_bulk)
        mbAdapter = MemberAdapter(this)
        memberListView = findViewById(R.id.addMemberListView)
        val i = intent
        fromMode = i.getStringExtra(AddMemberKeys.FROM_MODE.key)
        onAddMember()
        editViewAdapter = AddInBulkViewAdapter(this, memberArray)
        setViews()
        memberListView.adapter = editViewAdapter
        setKeyboardListener()
    }

    //View初期化
    @SuppressLint("SetTextI18n")
    fun setViews() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        screenHeight = size.y
        findViewById<ImageButton>(R.id.add_member).setOnClickListener { onAddMember() }
        findViewById<Button>(R.id.member_registration_finish_btn).setOnClickListener { registerMembers(true) }
        findViewById<Button>(R.id.member_registration_continue_btn).setOnClickListener { registerMembers(false) }
        findViewById<Button>(R.id.member_cancel_btn).setOnClickListener { finish() }
        findViewById<Button>(R.id.switch_single_mode).setOnClickListener {
            val intent = Intent(this, AddMember::class.java)
            intent.putExtra(AddMemberKeys.FROM_MODE.key, fromMode)
            finish()
            startActivity(intent)
        }
    }

    //メンバー追加
    private fun onAddMember() {
        val memberNo = memberArray.size + 1
        updateMemberArray()
        memberArray.add(Member(memberNo, "", getString(R.string.man), -1, "", getString(R.string.hira_member) + " " + memberNo, -1))
        editViewAdapter?.notifyDataSetChanged()
        editViewAdapter?.setRowHeight(memberListView)
    }

    //memberArrayの内容更新
    private fun updateMemberArray() {
        memberArray.clear()
        for (i in 0 until memberListView.count) {
            val name = editViewAdapter!!.getName(i, true)
            val sex = editViewAdapter!!.getSex(i)
            val age = editViewAdapter!!.getAge(i, true)
            val read = editViewAdapter!!.getRead(i)
            memberArray.add(Member(i, name, sex, age, "", read, -1))
        }
    }

    private fun registerMembers(finish: Boolean) {
        for (i in 0 until memberListView.count) {
            val name = editViewAdapter!!.getName(i, false)
            val sex = editViewAdapter!!.getSex(i)
            val age = editViewAdapter!!.getAge(i, false)
            val read = editViewAdapter!!.getRead(i)

            mbAdapter!!.saveName(name, sex, age, "", read)

            val newMember = MemberAdapter(this).newMember

            when (fromMode) {
                "normal" -> NormalMode.memberArray.add(newMember)
                "others" -> SelectMember.memberArray.add(newMember)
            }

            FirebaseAnalyticsEvents.memberRegisterEvent(newMember)
        }

        Toast.makeText(this, getText(R.string.member).toString() + memberListView.count + getText(R.string.people) + getText(R.string.registered), Toast.LENGTH_SHORT).show()

        if (finish) {
            finish()
        } else {
            val intent = Intent(this, AddMember::class.java)
            intent.putExtra(AddMemberKeys.FROM_MODE.key, fromMode)
            finish()
            startActivity(intent)
        }
    }

    //バックキーの処理
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogWarehouse(supportFragmentManager).decisionDialog("KUMIWAKE", getString(R.string.edit_exit_confirmation)) { finish() }
            return true
        }
        return false
    }


    //キーボードによるレイアウト崩れを防ぐ
    private fun setKeyboardListener() {
        val activityRootView = findViewById<View>(R.id.custom_root_layout)
        val view = findViewById<View>(R.id.button_unit)
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







