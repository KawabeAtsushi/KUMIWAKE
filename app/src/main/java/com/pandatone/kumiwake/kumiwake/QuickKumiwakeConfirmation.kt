package com.pandatone.kumiwake.kumiwake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
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
    private lateinit var memberAdapter: ArrayAdapter<String>
    private lateinit var groupAdapter: ArrayAdapter<String>
    private  var even_fm_ratio: Boolean = false
    private  var even_person_ratio: Boolean = false
    private lateinit var viewGroup: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_confirmation)
        ButterKnife.bind(this)
        val i = intent
        manArray = i.getStringArrayListExtra("QuickModeManList")
        womanArray = i.getStringArrayListExtra("QuickModeWomanList")
        groupArray = i.getStringArrayListExtra("QuickModekumiwake_group_listView")
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false)
        even_person_ratio = i.getBooleanExtra("EvenPersonRatio", false)
        memberArray = ArrayList()
        memberArray.addAll(manArray)
        memberArray.addAll(womanArray)
        findViews()
        setAdapter()
        setViews()
        val scrollView = findViewById<View>(R.id.scrollView) as ScrollView
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    protected fun findViews() {
        if (KumiwakeSelectMode.sekigime) {
            confirmation_title_txt.setText(R.string.sekigime_confirm)
            between_arrows_txt.text = "SEKIGIME"
            kumiwake_btn.setText(R.string.go_select_seats_type)
        }
        if (womanArray.size != 0) {
            member_no_txt.text = (memberArray.size.toString() + " " + getText(R.string.person)
                    + "(" + getText(R.string.man) + ":" + manArray.size.toString() + getText(R.string.person)
                    + "," + getText(R.string.woman) + ":" + womanArray.size.toString() + getText(R.string.person) + ")")
        } else {
            member_no_txt.text = memberArray.size.toString() + " " + getText(R.string.person)
        }
        group_no_txt.text = groupArray.size.toString() + " " + getText(R.string.group)
        viewGroup = findViewById<View>(R.id.confirmation_view) as RelativeLayout
        viewGroup.background = null
        viewGroup.background = ContextCompat.getDrawable(this, R.drawable.quick_img)
    }

    fun setViews() {
        val custom_text = StringBuilder()
        if (even_fm_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n")
        }
        if (even_person_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_person_ratio) + "\n")
        }
        custom_review_txt.text = custom_text.toString()

        for (i in 0 until memberAdapter.count) {
            val item = memberAdapter.getView(i, null, kumiwake_member_listView)
            val memberIcon: ImageView
            memberIcon = item.findViewById<View>(R.id.memberIcon) as ImageView
            if (memberArray[i].matches((".*" + "♡" + ".*").toRegex())) {
                memberIcon.setColorFilter(resources.getColor(R.color.woman))
            }
        }
        setRowHeight(kumiwake_member_listView, memberAdapter)
        setRowHeight(kumiwake_group_listView, groupAdapter)
    }

    @OnClick(R.id.kumiwake_btn)
    internal fun onClicked() {
        val intent = Intent(this, QuickKumiwakeResult::class.java)
        intent.putStringArrayListExtra("QuickModekumiwake_member_listView", memberArray)
        intent.putStringArrayListExtra("QuickModeManList", manArray)
        intent.putStringArrayListExtra("QuickModeWomanList", womanArray)
        intent.putStringArrayListExtra("QuickModekumiwake_group_listView", groupArray)
        intent.putExtra("EvenFMRatio", even_fm_ratio)
        intent.putExtra("EvenPersonRatio", even_person_ratio)
        startActivity(intent)
    }

    protected fun setAdapter() {
        memberAdapter = MemberArrayAdapter(this, R.layout.mini_row_member, memberArray, true)
        groupAdapter = ArrayAdapter(this, R.layout.mini_row_group, R.id.groupName, groupArray)
        kumiwake_member_listView.adapter = memberAdapter
        kumiwake_group_listView.adapter = groupAdapter
    }

    protected fun setRowHeight(listView: ListView, listAdp: ArrayAdapter<String>) {
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

internal class MemberArrayAdapter(context: Context, textViewResourceId: Int, private val items: ArrayList<String>, private val showBorder: Boolean?) : ArrayAdapter<String>(context, textViewResourceId, items) {
    private val inflater: LayoutInflater

    init {

        inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    }

    override fun isEnabled(position: Int): Boolean {
        return showBorder!!
    }

    // 1アイテム分のビューを取得
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
            memberIcon.setColorFilter(context.resources.getColor(R.color.woman))
        }

        if (item != null) {
            nameTextView = v.findViewById<View>(R.id.memberName) as TextView
            nameTextView.text = item
        }

        return v
    }
}

