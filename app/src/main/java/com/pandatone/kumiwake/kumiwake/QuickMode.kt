package com.pandatone.kumiwake.kumiwake

import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.Slide
import android.view.Window
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import kotlinx.android.synthetic.main.quick_mode.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/02.
 */
class QuickMode : AppCompatActivity(), TextWatcher {

    private var memberNo: Int = 0
    private var manNo: Int = 0
    private var womanNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Slide()
        }
        setContentView(R.layout.quick_mode)
        ButterKnife.bind(this)

        sex_seekbar.isEnabled = false
        member_no_form.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.toString() != "") {
            memberNo = Integer.parseInt(s.toString())
            sex_seekbar.isEnabled = true
        } else {
            memberNo = 0
            sex_seekbar.isEnabled = false
        }
        sex_seekbar.max = memberNo
        sex_seekbar.progress = memberNo

        sex_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sex_seekbar: SeekBar) {}

            override fun onProgressChanged(sex_seekbar: SeekBar, progress: Int, fromTouch: Boolean) {
                manNo = progress
                womanNo = memberNo - progress
                man_number_txt.text = manNo.toString()
                woman_number_txt.text = womanNo.toString()
            }

            override fun onStopTrackingTouch(sex_seekbar: SeekBar) {}
        })
    }

    @OnClick(R.id.quick_kumiwake_btn)
    internal fun onClicked() {
        val group_no = group_no_form.text!!.toString()
        val member_no = member_no_form.text!!.toString()

        error_member_no_txt.text = ""
        error_group_no_txt.text = ""

        when {
            TextUtils.isEmpty(member_no) -> error_member_no_txt.setText(R.string.error_empty_member_no)
            TextUtils.isEmpty(group_no) -> error_group_no_txt.setText(R.string.error_empty_group_no)
            group_no == "0" -> error_group_no_txt.setText(R.string.require_correct_No)
            Integer.parseInt(group_no) > memberNo -> {
                error_group_no_txt.setText(R.string.number_of_groups_is_much_too)
            }
            else -> {
                val womanList = ArrayList<String>()
                val manList = createManList(manNo, womanNo)
                val groupList = ArrayList<String>()
                for (i in 1..womanNo) {
                    womanList.add(getText(R.string.member).toString() + "♡" + i.toString())
                }
                for (i in 1..Integer.parseInt(group_no)) {
                    groupList.add(getText(R.string.group).toString() + " " + i.toString())
                }
                val intent = Intent(this, QuickKumiwakeConfirmation::class.java)
                intent.putStringArrayListExtra(MAN_LIST, manList)
                intent.putStringArrayListExtra(WOMAN_LIST, womanList)
                intent.putStringArrayListExtra(GROUP_LIST, groupList)
                intent.putExtra(EVEN_FM_RATIO, even_fm_ratio_check.isChecked)
                startActivity(intent)
                overridePendingTransition(R.anim.in_right, R.anim.out_left)
            }
        }
    }

    private fun createManList(manNo: Int, womanNo: Int): ArrayList<String> {
        val manList = ArrayList<String>()
        if (womanNo == 0) {
            for (i in 1..manNo) {
                manList.add(getText(R.string.member).toString() + " " + i.toString())
            }
        } else {
            for (i in 1..manNo) {
                manList.add(getText(R.string.member).toString() + "♠" + i.toString())
            }
        }
        return manList
    }

    companion object {

        const val EVEN_FM_RATIO = "even_fm_ratio"
        const val MAN_LIST = "QuickModeManList"
        const val WOMAN_LIST = "QuickModeWomanList"
        const val GROUP_LIST = "QuickModeGroupList"
        const val MEMBER_LIST = "QuickModeMemberList"

    }
}
