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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GestureDetectorCompat
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
    private lateinit var errorMember: TextView
    private lateinit var listView: ListView
    private lateinit var mDetector: GestureDetectorCompat

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Slide()
        }
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.normal_mode)
        findViews()
        memberArray = ArrayList()
        add_group_listview.member_add_btn.setOnClickListener { moveMemberMain() }
        add_group_listview.member_register_and_add_btn.setOnClickListener { moveAddMember() }
        add_group_listview.numberOfSelectedMember.text = "0${getString(R.string.people)}${getString(R.string.selected)}"
        findViewById<Button>(R.id.normal_kumiwake_btn).setOnClickListener { onNextClick() }

        Toast.makeText(this,getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm,gpNoEditText))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, gpNoEditText))
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
        errorMember.text = ""
        val intent = Intent(this, ChoiceMemberMain::class.java)
        intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, memberArray)
        startActivityForResult(intent, 0)
    }

    //AddMemberに遷移
    private fun moveAddMember() {
        errorMember.text = ""
        val intent = Intent(this, AddMember::class.java)
        intent.putExtra(AddMemberKeys.FROM_NORMAL_MODE.key, true)
        startActivityForResult(intent, 0) //これで呼ぶとActivityが終わった時にonActivityResultが呼ばれる。
    }

    //次に進むボタン
    private fun onNextClick() {
        val inputGroupNo = gpNoEditText.text!!.toString()

        errorMember.visibility = View.GONE
        errorGroup.visibility = View.GONE
        errorGroup.text = ""
        errorMember.text = ""

        if (adapter == null) {
            errorMember.visibility = View.VISIBLE
            errorMember.setText(R.string.error_empty_member_list)
        } else if (inputGroupNo != "" && Integer.parseInt(inputGroupNo) > adapter?.count!!) {
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra(AddGroupKeys.MEMBER_ARRAY.key) as ArrayList<Member>
        }

        adapter = SmallMBListAdapter(this, memberArray, false, showLeaderNo = false)
        listView.adapter = adapter
        add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    companion object {
        internal lateinit var memberArray: ArrayList<Member>
    }

}
