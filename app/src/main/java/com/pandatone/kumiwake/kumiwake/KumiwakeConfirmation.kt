package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallGPListAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.kumiwake.function.KumiwakeComparator
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
    private lateinit var groupArray: ArrayList<Group>
    private var leaderNoList: Array<Int?> = emptyArray()
    private var newMemberArray: ArrayList<Member> = ArrayList()
    private var leaderArray: ArrayList<Member> = ArrayList()
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.kumiwake_confirmation)
        if (StatusHolder.sekigime) {
            val layout = findViewById<ConstraintLayout>(R.id.confirmation_view)
            layout.background = getDrawable(R.drawable.sekigime_background)
        }
        findViewById<Button>(R.id.kumiwake_btn).setOnClickListener { doKumiwake() }

        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray = i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.LEADER_NO_LIST.key) != null) {
            leaderNoList = i.getSerializableExtra(KumiwakeArrayKeys.LEADER_NO_LIST.key) as Array<Int?>
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
        leaderArray.clear()
        newMemberArray.clear()

        if (StatusHolder.normalMode) {
            var id: Int
            for (member in memberArray) {
                id = member.id
                if (leaderNoList.contains(id)) {
                    member.leader = leaderNoList.indexOf(id)
                    leaderArray.add(member)
                } else {
                    newMemberArray.add(member) //リーダーを除いたmemberArray
                }
            }
            Collections.sort(leaderArray, KumiwakeComparator.LeaderComparator())
        } else {
            newMemberArray.addAll(memberArray)
        }
    }

    //Viewの初期化処理
    private fun setViews() {
        Collections.sort(newMemberArray, KumiwakeComparator.ViewComparator())
        memberArray.clear()
        memberArray.addAll(leaderArray)
        memberArray.addAll(newMemberArray)
        val mbAdapter = SmallMBListAdapter(this, memberArray, leaderNoList = leaderNoList, showLeaderNo = true)
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
        //Add Firebase Analytics
        if (StatusHolder.sekigime) {
            groupArray.forEach { it ->
                FirebaseAnalyticsEvents.groupCreateEvent("SEKIGIME", it.name)
            }
        } else {
            groupArray.forEach { it ->
                FirebaseAnalyticsEvents.groupCreateEvent(it.name, "KUMIWAKE")
            }
        }
        val intent = Intent(this, KumiwakeResult::class.java)
        intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, newMemberArray)
        intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
        intent.putExtra(KumiwakeArrayKeys.LEADER_LIST.key, leaderArray)
        intent.putExtra(KumiwakeArrayKeys.LEADER_NO_LIST.key, leaderNoList)
        intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, evenFmRatio)
        intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, evenAgeRatio)
        startActivity(intent)
    }

}

