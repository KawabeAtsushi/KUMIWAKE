package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.QuickModeKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import kotlinx.android.synthetic.main.kumiwake_confirmation.*
import java.util.*


/**
 * Created by atsushi_2 on 2016/05/08.
 */
class QuickKumiwakeConfirmation : AppCompatActivity() {
    private lateinit var memberArray: ArrayList<String>
    private lateinit var manArray: ArrayList<String>
    private lateinit var womanArray: ArrayList<String>
    private lateinit var groupArray: ArrayList<String>
    private lateinit var mbAdapter: ArrayAdapter<String>
    private lateinit var gpAdapter: ArrayAdapter<String>
    private var evenFmRatio: Boolean = false
    private lateinit var viewGroup: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_confirmation)
        ButterKnife.bind(this)
        intent.also {
            manArray = it.getStringArrayListExtra(QuickModeKeys.MAN_LIST.key)
            womanArray = it.getStringArrayListExtra(QuickModeKeys.WOMAN_LIST.key)
            groupArray = it.getStringArrayListExtra(QuickModeKeys.GROUP_LIST.key)
            evenFmRatio = it.getBooleanExtra(QuickModeKeys.EVEN_FM_RATIO.key, false)
        }
        memberArray = ArrayList()
        memberArray.addAll(manArray)
        memberArray.addAll(womanArray)
        findViews()
        setAdapter()
        setViews()
        val scrollView = findViewById<View>(R.id.scrollView) as ScrollView
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
            button.setTextColor(resources.getColor(android.R.color.white))
            button.textSize = 20F
        }
        if (womanArray.size != 0) {
            member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.people)
                    + "(" + getText(R.string.man) + ":" + manArray.size.toString() + getText(R.string.people)
                    + "," + getText(R.string.woman) + ":" + womanArray.size.toString() + getText(R.string.people) + ")")
        } else {
            member_no_txt.text = memberArray.size.toString() + " " + getText(R.string.people)
        }
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
        viewGroup = findViewById<View>(R.id.confirmation_view) as RelativeLayout
        viewGroup.background = null
        viewGroup.background = ContextCompat.getDrawable(this, R.drawable.quick_img)
    }

    private fun setViews() {
        val customText = StringBuilder()
        if (evenFmRatio) {
            customText.append("☆" + getText(R.string.even_out_male_female_ratio))
        }

        custom_review_txt.text = customText.toString()

        for (i in 0 until mbAdapter.count) {
            val item = mbAdapter.getView(i, null, kumiwake_member_listView)
            val memberIcon: ImageView
            memberIcon = item.findViewById<View>(R.id.memberIcon) as ImageView
            if (memberArray[i].matches((".*" + "♡" + ".*").toRegex())) {
                memberIcon.setColorFilter(ContextCompat.getColor(applicationContext, R.color.woman))
            }
        }
        setRowHeight(kumiwake_member_listView, mbAdapter)
        setRowHeight(groupListView, gpAdapter)
    }

    @OnClick(R.id.kumiwake_btn)
    internal fun onClicked() {
        Intent(this, QuickKumiwakeResult::class.java).also {
            it.putStringArrayListExtra(QuickModeKeys.MEMBER_LIST.key, memberArray)
            it.putStringArrayListExtra(QuickModeKeys.MAN_LIST.key, manArray)
            it.putStringArrayListExtra(QuickModeKeys.WOMAN_LIST.key, womanArray)
            it.putStringArrayListExtra(QuickModeKeys.GROUP_LIST.key, groupArray)
            it.putExtra(QuickModeKeys.EVEN_FM_RATIO.key, evenFmRatio)
            startActivity(it)
        }
    }

    private fun setAdapter() {
        mbAdapter = MemberArrayAdapter(this, R.layout.mini_row_member, memberArray, true)
        gpAdapter = ArrayAdapter(this, R.layout.mini_row_group, R.id.groupName, groupArray)
        kumiwake_member_listView.adapter = mbAdapter
        groupListView.adapter = gpAdapter
    }

    companion object {
        fun setRowHeight(listView: ListView, listAdp: ArrayAdapter<String>) {
            var totalHeight = 33

            for (j in 0 until listAdp.count) {
                val item = listAdp.getView(j, null, listView)
                item.measure(0, 0)
                totalHeight += item.measuredHeight
            }

            listView.layoutParams.height = totalHeight + listView.dividerHeight * (listAdp.count - 1)
            listView.requestLayout()
        }
    }
}

internal class MemberArrayAdapter(context: Context, textViewResourceId: Int, private val items: ArrayList<String>, private val showBorder: Boolean?) : ArrayAdapter<String>(context, textViewResourceId, items) {
    private val inflater: LayoutInflater = context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater

    override fun isEnabled(position: Int): Boolean {
        return showBorder!!
    }

    // 1アイテム分のビューを取得
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nameTextView: TextView
        val memberIcon: ImageView
        var v = convertView
        val item = items[position]

        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_member, null)
        }

        memberIcon = v!!.findViewById<View>(R.id.memberIcon) as ImageView
        if (items[position].matches((".*" + "♡" + ".*").toRegex())) {
            memberIcon.setColorFilter(ContextCompat.getColor(context, R.color.woman))
        }

        nameTextView = v.findViewById<View>(R.id.memberName) as TextView
        nameTextView.text = item

        return v
    }
}

