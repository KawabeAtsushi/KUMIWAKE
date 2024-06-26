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
class RoleConfirmation : AppCompatActivity() {
    private lateinit var binding: KumiwakeConfirmationBinding

    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(StatusHolder.nowTheme)
        binding = KumiwakeConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layout = findViewById<ConstraintLayout>(R.id.confirmation_view)
        layout.background = ContextCompat.getDrawable(this, R.drawable.top_background)
        val nextButton = findViewById<Button>(R.id.kumiwake_btn)
        val buttonTxt = "${getString(R.string.role_decision)}!!"
        nextButton.text = buttonTxt
        nextButton.setOnClickListener { doAssign() }

        val i = intent
        i.getSerializable<ArrayList<Member>>(KumiwakeArrayKeys.MEMBER_LIST.key)
            ?.let { memberArray = it }
        i.getSerializable<ArrayList<Group>>(KumiwakeArrayKeys.GROUP_LIST.key)
            ?.let { groupArray = it }
        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)

        findViews()
        setViews()
        binding.scrollView.post { binding.scrollView.scrollTo(0, 0) }

        val animation = AnimationUtils.loadAnimation(this, R.anim.arrow_move)
        binding.arrow1.startAnimation(animation)
        binding.arrow2.startAnimation(animation)
    }

    @SuppressLint("SetTextI18n")
    private fun findViews() {

        binding.memberNoTxt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                + "(" + getText(R.string.man) + ":" + countManNo().toString() + getText(R.string.people)
                + "," + getText(R.string.woman) + ":" + (memberArray.size - countManNo()).toString() + getText(
            R.string.people
        ) + ")")
        if (groupArray.last().id == 1) {//割り当てなしがある場合
            binding.groupNoTxt.text =
                "${groupArray.size - 1} ${getText(R.string.kinds)} + ${getText(R.string.other)}"
        } else {
            binding.groupNoTxt.text = "${groupArray.size} ${getText(R.string.kinds)}"
        }
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


    //Viewの初期化処理
    private fun setViews() {
        Collections.sort(
            memberArray,
            KumiwakeComparator.ViewComparator(KumiwakeComparator.SortType.DEFAULT)
        )
        val mbAdapter = SmallMBListAdapter(this, memberArray)
        val gpAdapter = SmallGPListAdapter(this, groupArray, true)
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
        binding.confirmationTitleTxt.text = getString(R.string.execute_role_confirm)
        binding.betweenArrowsTxt.text = getString(R.string.assign)
        binding.groupTag.text = getString(R.string.role)
        mbAdapter.setRowHeight(binding.kumiwakeMemberListView)
        gpAdapter.setRowHeight(binding.groupListView)
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

