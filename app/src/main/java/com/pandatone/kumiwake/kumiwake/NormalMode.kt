package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.ModeKeys
import com.pandatone.kumiwake.MyGestureListener
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.databinding.NormalModeBinding
import com.pandatone.kumiwake.extension.getSerializable
import com.pandatone.kumiwake.member.AddMember
import com.pandatone.kumiwake.member.ChoiceMemberMain
import com.pandatone.kumiwake.member.function.Member

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class NormalMode : AppCompatActivity() {
    private lateinit var binding: NormalModeBinding
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
        //テーマを設定
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            PublicMethods.setStatus(this, Theme.Sekigime.primaryColor)
        } else {
            PublicMethods.setStatus(this, Theme.Kumiwake.primaryColor)
        }
        binding = NormalModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            val layout = findViewById<ConstraintLayout>(R.id.normal_select_layout)
            layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_background)
        }
        findViews()
        binding.addGroupListView.memberAddBtn.setOnClickListener { moveMemberMain() }
        binding.addGroupListView.memberRegisterAndAddBtn.setOnClickListener { moveAddMember() }
        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        binding.addGroupListView.numberOfSelectedMember.text =
            "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
        findViewById<Button>(R.id.normal_kumiwake_btn).setOnClickListener { onNextClick() }

        Toast.makeText(this, getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm, gpNoEditText))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, gpNoEditText))
    }

    //Viewの宣言
    private fun findViews() {
        listView = findViewById<View>(R.id.add_group_listView).findViewById(R.id.memberListView)
        listView.emptyView =
            findViewById<View>(R.id.add_group_listView).findViewById(R.id.emptyMemberList)
        gpNoEditText = findViewById(R.id.group_no_form)
        errorGroup = findViewById(R.id.error_group_no_txt)
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
            val groupArray = PublicMethods.initialGroupArray(this, groupNo, memberArray.size, true)
            val intent = Intent(this, KumiwakeCustom::class.java)
            intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
            intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
            //Add Firebase
            if (StatusHolder.mode == ModeKeys.Sekigime.key) {
                FirebaseAnalyticsEvents.countEvent(
                    memberArray.size,
                    groupNo,
                    FirebaseAnalyticsEvents.FunctionKeys.SekigimeNormal.key
                )
            } else {
                FirebaseAnalyticsEvents.countEvent(
                    memberArray.size,
                    groupNo,
                    FirebaseAnalyticsEvents.FunctionKeys.KumiwakeNormal.key
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            i?.getSerializable<ArrayList<Member>>(AddGroupKeys.MEMBER_ARRAY.key)
                ?.let { memberArray = it }
        }

        adapter = SmallMBListAdapter(this, memberArray)
        listView.adapter = adapter
        val selectedTxt =
            "${memberArray.size}${getString(R.string.people)}${getString(R.string.selected)}"
        binding.addGroupListView.numberOfSelectedMember.text = selectedTxt
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    companion object {
        internal var memberArray: ArrayList<Member> = ArrayList()
    }

}
