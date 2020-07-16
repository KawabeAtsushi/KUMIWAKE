package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Slide
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.ChoiceMemberMain
import com.pandatone.kumiwake.member.function.Member
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
    private lateinit var listView: ListView
    private lateinit var mDetector: GestureDetectorCompat

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = Slide()
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.normal_mode)
        findViews()
        setStatus()
        add_group_listView.member_add_btn.setOnClickListener { moveMemberMain() }
        add_group_listView.member_register_and_add_btn.setOnClickListener { moveAddMember() }
        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        add_group_listView.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
        findViewById<Button>(R.id.normal_kumiwake_btn).setOnClickListener { onNextClick() }

        Toast.makeText(this, getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm, gpNoEditText))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, gpNoEditText))
    }

    //Viewの宣言
    private fun findViews() {
        listView = findViewById<View>(R.id.add_group_listView).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listView).findViewById(R.id.emptyMemberList)
        gpNoEditText = findViewById<View>(R.id.group_no_form) as AppCompatEditText
        errorGroup = findViewById<View>(R.id.error_group_no_txt) as TextView
    }

    private fun setStatus(){
        PublicMethods.setStatusBarColor(this, Theme.Kumiwake.primaryColor)
    }

    //MemberMainに遷移
    private fun moveMemberMain() {
        val intent = Intent(this, ChoiceMemberMain::class.java)
        intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, memberArray)
        startActivityForResult(intent, 0)
    }

    //AddMemberに遷移
    private fun moveAddMember() {
        val intent = Intent(this, AddMember::class.java)
        intent.putExtra(AddMemberKeys.FROM_MODE.key, "normal")
        startActivityForResult(intent, 0) //これで呼ぶとActivityが終わった時にonActivityResultが呼ばれる。
    }

    //次に進むボタン
    private fun onNextClick() {
        val inputGroupNo = gpNoEditText.text!!.toString()

        errorGroup.visibility = View.GONE
        errorGroup.text = ""

        if (inputGroupNo != "" && Integer.parseInt(inputGroupNo) > adapter?.count!!) {
            errorGroup.visibility = View.VISIBLE
            errorGroup.setText(R.string.number_of_groups_is_much_too)
        } else if (TextUtils.isEmpty(inputGroupNo)) {
            errorGroup.visibility = View.VISIBLE
            errorGroup.setText(R.string.error_empty_group_no)
        } else if (inputGroupNo == "0") {
            errorGroup.visibility = View.VISIBLE
            errorGroup.setText(R.string.require_correct_No)
        } else {
            val groupNo = Integer.parseInt(inputGroupNo)
            val groupArray = PublicMethods.initialGroupArray(this, groupNo, memberArray.size)
            val intent = Intent(this, KumiwakeCustom::class.java)
            intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
            intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
            //Add Firebase
            if (StatusHolder.sekigime) {
                FirebaseAnalyticsEvents.countEvent(memberArray.size, groupNo, FirebaseAnalyticsEvents.FunctionKeys.SekigimeNormal.key)
            } else {
                FirebaseAnalyticsEvents.countEvent(memberArray.size, groupNo, FirebaseAnalyticsEvents.FunctionKeys.KumiwakeNormal.key)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra(AddGroupKeys.MEMBER_ARRAY.key) as ArrayList<Member>
        }

        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        val selectedTxt = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
        add_group_listView.numberOfSelectedMember.text = selectedTxt
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    companion object {
        internal var memberArray: ArrayList<Member> = ArrayList()
    }

}
