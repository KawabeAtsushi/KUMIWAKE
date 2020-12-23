package com.pandatone.kumiwake.others.role

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.SmallGPListAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.kumiwake.function.KumiwakeComparator
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import kotlinx.android.synthetic.main.kumiwake_confirmation.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/08.
 */
class RoleConfirmation : AppCompatActivity() {
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.kumiwake_confirmation)
        val layout = findViewById<ConstraintLayout>(R.id.confirmation_view)
        layout.background = ContextCompat.getDrawable(this,R.drawable.top_background)
        val nextButton = findViewById<Button>(R.id.kumiwake_btn)
        val buttonTxt = "${getString(R.string.role_decision)}!!"
        nextButton.text = buttonTxt
        nextButton.setOnClickListener { doAssign() }

        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray = i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }
        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)

        findViews()
        setViews()
        scrollView.post { scrollView.scrollTo(0, 0) }

        val animation = AnimationUtils.loadAnimation(this, R.anim.arrow_move)
        arrow1.startAnimation(animation)
        arrow2.startAnimation(animation)
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.people)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(R.string.people) + ")")
        if (groupArray.last().id == 1) {//割り当てなしがある場合
            group_no_txt.text = "${groupArray.size - 1} ${getText(R.string.kinds)} + ${getText(R.string.other)}"
        } else {
            group_no_txt.text = "${groupArray.size} ${getText(R.string.kinds)}"
        }
    }

    //Manの数
    private fun countManNo(): Int {
        var manNo = 0
        for (member in memberArray) {
            if (member.sex == getText(R.string.man)) {
                manNo++
            }
        }
        return manNo
    }


    //Viewの初期化処理
    private fun setViews() {
        Collections.sort(memberArray, KumiwakeComparator.ViewComparator())
        val mbAdapter = SmallMBListAdapter(this, memberArray)
        val gpAdapter = SmallGPListAdapter(this, groupArray, true)
        kumiwake_member_listView.adapter = mbAdapter
        groupListView.adapter = gpAdapter

        val customText = StringBuilder()
        if (evenFmRatio) {
            customText.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n")
        }
        if (evenAgeRatio) {
            customText.append("☆" + getText(R.string.even_out_age_ratio) + "\n")
        }
        custom_review_txt.text = customText.toString()
        confirmation_title_txt.text = getString(R.string.execute_role_confirm)
        between_arrows_txt.text = getString(R.string.assign)
        group_tag.text = getString(R.string.role)
        mbAdapter.setRowHeight(kumiwake_member_listView)
        gpAdapter.setRowHeight(groupListView)
    }

    //役割決め実行
    private fun doAssign() {
        val intent = Intent(this, RoleResult::class.java)
        intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
        intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
        intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, evenFmRatio)
        intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, evenAgeRatio)
        startActivity(intent)
    }

}

