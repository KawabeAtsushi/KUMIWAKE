package com.pandatone.kumiwake.order

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.ChoiceMemberMain
import com.pandatone.kumiwake.member.function.Member
import kotlinx.android.synthetic.main.normal_mode.*
import kotlinx.android.synthetic.main.part_review_listview.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class OrderSelectMember : AppCompatActivity() {
    private var adapter: SmallMBListAdapter? = null
    private lateinit var errorMember: TextView
    private lateinit var listView: ListView
    private var memberArray = ArrayList<Member>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Slide()
        }
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.normal_mode)
        val layout = findViewById<ConstraintLayout>(R.id.normal_select_layout)
        layout.background = getDrawable(R.drawable.img_others_background)
        findViews()
        findViewById<LinearLayout>(R.id.layout_group_no).visibility = View.GONE
        add_group_listview.member_add_btn.setOnClickListener { moveMemberMain() }
        add_group_listview.member_register_and_add_btn.setOnClickListener { moveAddMember() }
        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
        val nextBtn = findViewById<Button>(R.id.normal_kumiwake_btn)
        nextBtn.text = "${getText(R.string.order)}!!"
        nextBtn.setTextColor(PublicMethods.getColor(this,R.color.gold))
        nextBtn.textSize = 26F
        nextBtn.setTypeface(Typeface.DEFAULT_BOLD)
        nextBtn.setOnClickListener { onNextClick() }
    }

    //Viewの宣言
    private fun findViews() {
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
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
        errorMember.visibility = View.GONE
        errorMember.text = ""

        if (adapter == null) {
            errorMember.visibility = View.VISIBLE
            errorMember.setText(R.string.error_empty_member_list)
        } else {

            val intent = Intent(this, OrderResult::class.java)
            intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            memberArray = i!!.getSerializableExtra(AddGroupKeys.MEMBER_ARRAY.key) as ArrayList<Member>
        }

        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        add_group_listview.numberOfSelectedMember.text = "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
    }

}
