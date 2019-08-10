package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GPListViewAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.kumiwake_confirmation.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/05/08.
 */
class NormalKumiwakeConfirmation : AppCompatActivity() {
    private lateinit var memberArray: ArrayList<Name>
    private lateinit var newMemberArray: ArrayList<Name>
    private lateinit var leaderArray: ArrayList<Name>
    private lateinit var groupArray: ArrayList<GroupListAdapter.Group>
    private lateinit var memberAdapter: MBListViewAdapter
    private lateinit var groupAdapter: GPListViewAdapter
    private var even_fm_ratio: Boolean = false
    private var even_age_ratio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_confirmation)
        ButterKnife.bind(this)
        val i = intent
        if (i.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) != null) {
            memberArray = i.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) as ArrayList<Name>
        }
        if (i.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) != null) {
            groupArray = i.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) as ArrayList<GroupListAdapter.Group>
        }

        even_fm_ratio = i.getBooleanExtra(KumiwakeCustom.EVEN_FM_RATIO, false)
        even_age_ratio = i.getBooleanExtra(KumiwakeCustom.EVEN_AGE_RATIO, false)
        createLeaderArray()
        findViews()
        setAdapter()
        setViews()
        scrollView.post { scrollView.scrollTo(0, 0) }

        val animation = AnimationUtils.loadAnimation(this, R.anim.arrow_move)
        arrow1.startAnimation(animation)
        arrow2.startAnimation(animation)
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        if (KumiwakeSelectMode.sekigime) {
            val button = findViewById<Button>(R.id.kumiwake_btn)
            confirmation_title_txt.setText(R.string.sekigime_confirm)
            between_arrows_txt.text = MyApplication.context?.getText(R.string.sekigime)
            button.setText(R.string.go_select_seats_type)
            button.typeface = Typeface.DEFAULT
            button.setTextColor(ContextCompat.getColor(MyApplication.context!!, android.R.color.white))
            button.textSize = 20F
        }
        member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.people)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(R.string.people) + ")")
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
    }

    private fun countManNo(): Int {
        var manNo = 0
        for (member in memberArray) {
            if (member.sex == getText(R.string.man)) {
                manNo++
            }
        }
        return manNo
    }

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

    private fun setViews() {
        val custom_text = StringBuilder()

        if (even_fm_ratio) {
            custom_text.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n")
        }
        if (even_age_ratio) {
            custom_text.append("☆" + getText(R.string.even_out_age_ratio) + "\n")
        }

        custom_review_txt.text = custom_text.toString()

        MBListViewAdapter.setRowHeight(kumiwake_member_listView, memberAdapter)
        GPListViewAdapter.setRowHeight(groupListView, groupAdapter)
    }

    @OnClick(R.id.kumiwake_btn)
    internal fun onClicked() {
        val intent = Intent(this, NormalKumiwakeResult::class.java)
        intent.putExtra(NormalMode.NORMAL_MEMBER_ARRAY, newMemberArray)
        intent.putExtra(NormalMode.NORMAL_GROUP_ARRAY, groupArray)
        intent.putExtra(LEADER_ARRAY, leaderArray)
        intent.putExtra(KumiwakeCustom.EVEN_FM_RATIO, even_fm_ratio)
        intent.putExtra(KumiwakeCustom.EVEN_AGE_RATIO, even_age_ratio)
        startActivity(intent)
    }

    private fun setAdapter() {
        Collections.sort(newMemberArray, KumiwakeViewComparator())
        memberArray.clear()
        memberArray.addAll(leaderArray)
        memberArray.addAll(newMemberArray)
        memberAdapter = MBListViewAdapter(this, memberArray, true, showLeaderNo = true)
        groupAdapter = GPListViewAdapter(this, groupArray)
        kumiwake_member_listView.adapter = memberAdapter
        groupListView.adapter = groupAdapter
    }

    companion object {
        const val LEADER_ARRAY = "leader_array"
    }

}

