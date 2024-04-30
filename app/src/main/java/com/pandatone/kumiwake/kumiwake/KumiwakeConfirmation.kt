package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.ModeKeys
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.SmallGPListAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.databinding.KumiwakeConfirmationBinding
import com.pandatone.kumiwake.extension.getSerializable
import com.pandatone.kumiwake.kumiwake.function.KumiwakeComparator
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import java.util.Collections

/**
 * Created by atsushi_2 on 2016/05/08.
 */
class KumiwakeConfirmation : AppCompatActivity() {
    private lateinit var binding: KumiwakeConfirmationBinding
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var notLeadersArray: ArrayList<Member>
    private var leaderArray: ArrayList<Member?> = ArrayList()
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setTheme(StatusHolder.nowTheme)
        binding = KumiwakeConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            val layout = findViewById<ConstraintLayout>(R.id.confirmation_view)
            layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_background)
        }
        findViewById<Button>(R.id.kumiwake_btn).setOnClickListener { doKumiwake() }

        val i = intent
        i.getSerializable<ArrayList<Member>>(KumiwakeArrayKeys.MEMBER_LIST.key)
            ?.let { memberArray = it }
        i.getSerializable<ArrayList<Group>>(KumiwakeArrayKeys.GROUP_LIST.key)
            ?.let { groupArray = it }
        i.getSerializable<ArrayList<Member?>>(KumiwakeArrayKeys.LEADER_LIST.key)
            ?.let { leaderArray = it }

        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)

        createExcludeLeaderArray()
        findViews()
        setViews()
        binding.scrollView.post { binding.scrollView.scrollTo(0, 0) }

        val animation = AnimationUtils.loadAnimation(this, R.anim.arrow_move)
        binding.arrow1.startAnimation(animation)
        binding.arrow2.startAnimation(animation)
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            val button = findViewById<Button>(R.id.kumiwake_btn)
            binding.confirmationTitleTxt.setText(R.string.sekigime_confirm)
            binding.betweenArrowsTxt.text = getText(R.string.sekigime)
            button.setText(R.string.go_select_seats_type)
            button.typeface = Typeface.DEFAULT
            button.setTextColor(PublicMethods.getColor(this, android.R.color.white))
            button.textSize = 20F
        }
        binding.memberNoTxt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.people)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(
            R.string.people
        ) + ")")
        binding.groupNoTxt.text = groupArray.size.toString() + " " + getText(R.string.group)
    }

    //Manの数
    private fun countManNo(): Int {
        var manNo = 0
        for (member in memberArray) {
            if (PublicMethods.isMan(member.sex)) {
                manNo++
            }
        }
        return manNo
    }

    //リーダーArray作成
    private fun createExcludeLeaderArray() {
        notLeadersArray = ArrayList()
        notLeadersArray.addAll(memberArray)

        if (StatusHolder.normalMode) {
            notLeadersArray.minusAssign(leaderArray.filterNotNull()) //リーダーを除いたmemberArray
            for (leader in leaderArray) {
                leader?.leader = leaderArray.indexOf(leader)
            }
        }

    }

    //Viewの初期化処理
    private fun setViews() {
        Collections.sort(
            notLeadersArray,
            KumiwakeComparator.ViewComparator(KumiwakeComparator.SortType.DEFAULT)
        )
        memberArray.clear()
        memberArray.addAll(leaderArray.filterNotNull())
        memberArray.addAll(notLeadersArray)
        val mbAdapter =
            SmallMBListAdapter(this, memberArray, leaderArray = leaderArray, showLeaderNo = true)
        val gpAdapter = SmallGPListAdapter(this, groupArray)
        binding.kumiwakeMemberListView.adapter = mbAdapter
        binding.groupListView.adapter = gpAdapter

        val customText = StringBuilder()
        if (evenFmRatio) {
            customText.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n")
        }
        if (evenAgeRatio) {
            customText.append("☆" + getText(R.string.even_out_age_ratio) + "\n")
        }
        binding.customReviewTxt.text = customText.toString()
        mbAdapter.setRowHeight(binding.kumiwakeMemberListView)
        gpAdapter.setRowHeight(binding.groupListView)
    }

    //組み分け実行
    private fun doKumiwake() {
        //Add Firebase Analytics
        if (StatusHolder.mode == ModeKeys.Sekigime.key) {
            groupArray.forEach {
                FirebaseAnalyticsEvents.groupCreateEvent("SEKIGIME", it.name)
            }
        } else {
            groupArray.forEach {
                FirebaseAnalyticsEvents.groupCreateEvent(it.name, "KUMIWAKE")
            }
        }
        val intent = Intent(this, KumiwakeResult::class.java)
        intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, notLeadersArray)
        intent.putExtra(KumiwakeArrayKeys.GROUP_LIST.key, groupArray)
        intent.putExtra(KumiwakeArrayKeys.LEADER_LIST.key, leaderArray)
        intent.putExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, evenFmRatio)
        intent.putExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, evenAgeRatio)
        startActivity(intent)
    }

}

