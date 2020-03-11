package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallGPListAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import kotlinx.android.synthetic.main.kumiwake_confirmation.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/05/08.
 */
class KumiwakeConfirmation : AppCompatActivity() {
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var newMemberArray: ArrayList<Member>
    private lateinit var leaderArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.kumiwake_confirmation)
        if (!StatusHolder.normalMode) {
            val layout = findViewById<ConstraintLayout>(R.id.confirmation_view)
            layout.background = getDrawable(R.drawable.quick_img)
        }
        findViewById<Button>(R.id.kumiwake_btn).setOnClickListener { doKumiwake() }

        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray = i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }

        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)
        createLeaderArray()
        findViews()
        setViews()
        scrollView.post { scrollView.scrollTo(0, 0) }

        val animation = AnimationUtils.loadAnimation(this, R.anim.arrow_move)
        arrow1.startAnimation(animation)
        arrow2.startAnimation(animation)
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        if (StatusHolder.sekigime) {
            val button = findViewById<Button>(R.id.kumiwake_btn)
            confirmation_title_txt.setText(R.string.sekigime_confirm)
            between_arrows_txt.text = getText(R.string.sekigime)
            button.setText(R.string.go_select_seats_type)
            button.typeface = Typeface.DEFAULT
            button.setTextColor(PublicMethods.getColor(this, android.R.color.white))
            button.textSize = 20F
        }
        member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.people)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(R.string.people) + ")")
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
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

    //リーダーArray作成
    private fun createLeaderArray() {
        leaderArray = ArrayList()
        newMemberArray = ArrayList()

        var id = 0
        val leaderNoList = KumiwakeCustom.leaderNoList

        for (member in memberArray) {
            id = member.id

            if (leaderNoList.contains(id)) {
                member.role = leaderNoList.indexOf(id).toString()
                leaderArray.add(member)
            } else {
                newMemberArray.add(member) //リーダーを除いたmemberArray
            }
        }

        Collections.sort(leaderArray, KumiwakeLeaderComparator())
    }

    //Viewの初期化処理
    private fun setViews() {
        Collections.sort(newMemberArray, KumiwakeViewComparator())
        memberArray.clear()
        memberArray.addAll(leaderArray)
        memberArray.addAll(newMemberArray)
        val mbAdapter = SmallMBListAdapter(this, memberArray, true, showLeaderNo = true)
        val gpAdapter = SmallGPListAdapter(this, groupArray)
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
        mbAdapter.setRowHeight(kumiwake_member_listView)
        gpAdapter.setRowHeight(groupListView)
    }

    //組み分け実行
    private fun doKumiwake() {
        val intent = Intent(this, KumiwakeResult::class.java)
        intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, newMemberArray)
        intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
        intent.putExtra(KumiwakeArrayKeys.LEADER_LIST.key, leaderArray)
        intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, evenFmRatio)
        intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, evenAgeRatio)
        startActivity(intent)
    }

}

