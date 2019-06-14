package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GPListViewAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.kumiwake_confirmation.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/08.
 */
class NormalKumiwakeConfirmation : AppCompatActivity() {
    private lateinit var memberArray: ArrayList<Name>
    private lateinit var groupArray: ArrayList<GroupListAdapter.Group>
    private lateinit var memberAdapter: MBListViewAdapter
    private lateinit var groupAdapter: GPListViewAdapter
    internal var even_fm_ratio: Boolean = false
    internal var even_leader_ratio: Boolean = false
    internal var even_age_ratio: Boolean = false
    internal var even_grade_ratio: Boolean = false
    internal lateinit var even_role: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_confirmation)
        ButterKnife.bind(this)
        val i = intent
        memberArray = i.getSerializableExtra("NormalModeMemberArray") as ArrayList<Name>
        groupArray = i.getSerializableExtra("NormalModeGroupArray") as ArrayList<GroupListAdapter.Group>
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false)
        even_leader_ratio = i.getBooleanExtra("EvenLeaderRatio", false)
        even_age_ratio = i.getBooleanExtra("EvenAgeRatio", false)
        even_grade_ratio = i.getBooleanExtra("EvenGradeRatio", false)
        even_role = i.getStringExtra("EvenRole")
        findViews()
        setAdapter()
        setViews()
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        if (KumiwakeSelectMode.sekigime) {
            confirmation_title_txt.setText(R.string.sekigime_confirm)
            between_arrows_txt.text = "SEKIGIME"
            kumiwake_btn.setText(R.string.go_select_seats_type)
        }
        member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.person)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.person)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(R.string.person) + ")")
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
    }

    private fun countManNo(): Int {
        var manNo = 0
        for (i in memberArray.indices) {
            if (memberArray[i].sex == getText(R.string.man)) {
                manNo++
            }
        }
        return manNo
    }

    private fun setViews() {
        val custom_text = StringBuilder()
        val array_str = resources.getStringArray(R.array.role)

        if (even_fm_ratio) {
            custom_text.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n")
        }
        if (even_age_ratio) {
            custom_text.append("☆" + getText(R.string.even_out_age_ratio) + "\n")
        }
        if (even_grade_ratio) {
            custom_text.append("☆" + getText(R.string.even_out_grade_ratio) + "\n")
        }
        if (even_role != array_str[0]) {
            custom_text.append("☆" + even_role + getText(R.string.even_out) + "\n")
        }
        custom_review_txt.text = custom_text.toString()

        MBListViewAdapter.setRowHeight(kumiwake_member_listView, memberAdapter)
        GPListViewAdapter.setRowHeight(kumiwake_group_listView, groupAdapter)
    }

    @OnClick(R.id.kumiwake_btn)
    internal fun onClicked() {
        val intent = Intent(this, NormalKumiwakeResult::class.java)
        intent.putExtra("NormalModeMemberArray", memberArray)
        intent.putExtra("NormalModeGroupArray", groupArray)
        intent.putExtra("EvenFMRatio", even_fm_ratio)
        intent.putExtra("EvenLeaderRatio", even_leader_ratio)
        intent.putExtra("EvenAgeRatio", even_age_ratio)
        intent.putExtra("EvenGradeRatio", even_grade_ratio)
        intent.putExtra("EvenRole", even_role)
        startActivity(intent)
    }

    private fun setAdapter() {
        Collections.sort(memberArray, KumiwakeLeaderComparator())
        memberAdapter = MBListViewAdapter(this, memberArray, 0)
        groupAdapter = GPListViewAdapter(this, groupArray)
        kumiwake_member_listView.adapter = memberAdapter
        kumiwake_group_listView.adapter = groupAdapter
    }

}

