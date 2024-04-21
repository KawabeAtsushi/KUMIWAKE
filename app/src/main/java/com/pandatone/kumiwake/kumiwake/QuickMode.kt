package com.pandatone.kumiwake.kumiwake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.Slide
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.ModeKeys
import com.pandatone.kumiwake.MyGestureListener
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.Theme
import com.pandatone.kumiwake.databinding.QuickModeBinding
import com.pandatone.kumiwake.member.function.Member


/**
 * Created by atsushi_2 on 2016/05/02.
 */
class QuickMode : AppCompatActivity(), TextWatcher {
    private lateinit var binding: QuickModeBinding

    private var memberNo: Int = 0
    private var manNo: Int = 0
    private var womanNo: Int = 0

    private lateinit var mDetector: GestureDetectorCompat

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
        binding = QuickModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            val layout = findViewById<ConstraintLayout>(R.id.quick_layout)
            layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_background)
        }
        binding.sexSeekBar.isEnabled = false
        binding.memberNoForm.addTextChangedListener(this)
        findViewById<Button>(R.id.quick_kumiwake_btn).setOnClickListener { onNextClicked() }

        Toast.makeText(this, getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val groupNoInput = findViewById<EditText>(R.id.group_no_form)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm, groupNoInput))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, groupNoInput))
        setupSeekBar()
    }

    //スクロールビューの場合こっち呼ぶ
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.toString() != "") {
            memberNo = Integer.parseInt(s.toString())
            binding.sexSeekBar.isEnabled = true
        } else {
            memberNo = 0
            binding.sexSeekBar.isEnabled = false
        }
        binding.sexSeekBar.max = memberNo
        binding.sexSeekBar.progress = memberNo
        manNo = memberNo
        womanNo = 0
        binding.manNumberTxt.text = manNo.toString()
        binding.womanNumberTxt.text = womanNo.toString()
    }

    private fun setupSeekBar() {
        binding.sexSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sexSeekbar: SeekBar) {}

            override fun onProgressChanged(
                sexSeekbar: SeekBar,
                progress: Int,
                fromTouch: Boolean
            ) {
                manNo = progress
                womanNo = memberNo - manNo
                binding.manNumberTxt.text = manNo.toString()
                binding.womanNumberTxt.text = womanNo.toString()
            }

            override fun onStopTrackingTouch(sexSeekbar: SeekBar) {}
        })
    }

    private fun onNextClicked() {
        val groupNo = binding.groupNoForm.text.toString()
        val memNo = binding.memberNoForm.text.toString()
        val errorMemberNo = findViewById<TextView>(R.id.error_member_no_txt)
        val errorGroupNo = findViewById<TextView>(R.id.error_group_no_txt)
        errorMemberNo.visibility = View.GONE
        errorGroupNo.visibility = View.GONE
        errorMemberNo.text = ""
        errorGroupNo.text = ""

        when {
            TextUtils.isEmpty(memNo) -> {
                errorMemberNo.visibility = View.VISIBLE
                errorMemberNo.setText(R.string.error_empty_member_no)
            }

            TextUtils.isEmpty(groupNo) -> {
                errorGroupNo.visibility = View.VISIBLE
                errorGroupNo.setText(R.string.error_empty_group_no)
            }

            groupNo == "0" -> {
                errorGroupNo.visibility = View.VISIBLE
                errorGroupNo.setText(R.string.require_correct_No)
            }

            Integer.parseInt(groupNo) > memberNo -> {
                errorGroupNo.visibility = View.VISIBLE
                errorGroupNo.setText(R.string.number_of_groups_is_much_too)
            }

            else -> {
                val memberList = createMemberList(manNo, memberNo - manNo)
                val groupList = PublicMethods.initialGroupArray(
                    this,
                    Integer.parseInt(groupNo),
                    memberList.size,
                    false
                )
                val intent = Intent(this, KumiwakeConfirmation::class.java)
                intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberList)
                intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupList)
                intent.putExtra(
                    KumiwakeCustomKeys.EVEN_FM_RATIO.key,
                    binding.evenFmRatioCheck.isChecked
                )
                startActivity(intent)
                overridePendingTransition(R.anim.in_right, R.anim.out_left)
                //Add Firebase
                if (StatusHolder.mode == ModeKeys.Sekigime.key) {
                    FirebaseAnalyticsEvents.countEvent(
                        memberNo,
                        Integer.parseInt(groupNo),
                        FirebaseAnalyticsEvents.FunctionKeys.SekigimeQuick.key
                    )
                } else {
                    FirebaseAnalyticsEvents.countEvent(
                        memberNo,
                        Integer.parseInt(groupNo),
                        FirebaseAnalyticsEvents.FunctionKeys.KumiwakeQuick.key
                    )
                }
            }
        }
    }

    private fun createMemberList(manNo: Int, womanNo: Int): ArrayList<Member> {
        val memberList = ArrayList<Member>()
        if (manNo == 0 || womanNo == 0) {
            for (i in 1..manNo + womanNo) {
                val planeMember = Member(
                    i,
                    getText(R.string.member).toString() + " " + i.toString(),
                    StatusHolder.none,
                    0,
                    "",
                    "",
                    -1
                )
                memberList.add(planeMember)
            }
        } else {
            for (i in 1..manNo) {
                val man = Member(
                    i,
                    getText(R.string.member).toString() + "♠" + i.toString(),
                    getString(R.string.man),
                    0,
                    "",
                    "",
                    -1
                )
                memberList.add(man)
            }
            for (i in 1..womanNo) {
                val woman = Member(
                    i + 1000,
                    getText(R.string.member).toString() + "♡" + i.toString(),
                    getString(R.string.woman),
                    0,
                    "",
                    "",
                    -1
                )
                memberList.add(woman)
            }
        }
        return memberList
    }
}
