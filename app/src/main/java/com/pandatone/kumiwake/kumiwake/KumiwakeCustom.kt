package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import android.view.WindowManager
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.EditGroupListAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.member.Name
import kotlinx.android.synthetic.main.kumiwake_custom.*
import kotlinx.android.synthetic.main.part_review_listview.*
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/27.
 */
class KumiwakeCustom : AppCompatActivity() {
    private var mbAdapter: MBListViewAdapter? = null
    private var gpAdapter: EditGroupListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.kumiwake_custom)
        ButterKnife.bind(this)

        if(intent.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) != null) {
            memberArray = intent.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) as ArrayList<Name>
        }
        if(intent.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) != null) {
            groupArray = intent.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) as ArrayList<GroupListAdapter.Group>
        }
        mbAdapter = MBListViewAdapter(this, memberArray, groupArray.size)
        gpAdapter = EditGroupListAdapter(this, groupArray)
        findViews()
        setViews()
        memberList.adapter = mbAdapter
        groupList.adapter = gpAdapter
        setLeader()

        memberList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //行をクリックした時の処理
            memberList.requestFocus()
            changeLeader(position)
        }

        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        origMemberArray = memberArray
    }

    private fun findViews() {

        even_age_ratio_check.setOnCheckedChangeListener { _, _ ->
            if (even_age_ratio_check.isChecked) {
                even_grade_ratio_check.isEnabled = false
                even_grade_ratio_check.isChecked = false
            } else {
                even_grade_ratio_check.isEnabled = true
            }
        }
        even_grade_ratio_check.setOnCheckedChangeListener { _, _ ->
            if (even_grade_ratio_check.isChecked) {
                even_age_ratio_check.isEnabled = false
                even_age_ratio_check.isChecked = false
            } else {
                even_age_ratio_check.isEnabled = true
            }
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        )
        val strs = resources.getStringArray(R.array.role)
        val list = ArrayList(Arrays.asList(*strs)) // 新インスタンスを生成
        list.removeAt(1)
        adapter.addAll(list)
        custom_spinner.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    fun setViews() {
        member_add_btn.visibility = View.GONE
        mbAdapter?.let { MBListViewAdapter.setRowHeight(memberList, it) }
        gpAdapter?.let { EditGroupListAdapter.setRowHeight(groupList, it) }
        numberOfSelectedMember.text = memberArray.size.toString() + getString(R.string.person)
        groupNo.text = groupArray.size.toString() + " " + getText(R.string.group)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val screenHeight = size.y
        background_img.layoutParams.height = screenHeight
    }

    @SuppressLint("SetTextI18n")
    fun setLeader() {
        for (i in memberArray.indices) {
            if (memberArray[i].role.matches((".*" + getText(R.string.leader) + ".*").toRegex())) {

                val roleArray = memberArray[i].role.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*roleArray))
                val hs = HashSet<String>()
                hs.addAll(list)
                list.clear()
                list.addAll(hs)
                list.remove("")
                list.sort()
                val nowldNo = Integer.parseInt(list[0].substring(2))
                groupList.post {
                    val leader = groupList.getChildAt(nowldNo - 1).findViewById<View>(R.id.leader) as TextView
                    leader.text = getText(R.string.leader).toString() + ":" + memberArray[i].name
                }
            }
        }
    }

    @OnClick(R.id.normal_kumiwake_button)
    internal fun onClicked() {
        var memberSum = 0
        var allowToNext: Boolean? = true
        for (i in 0 until groupList.count) {
            val memberNo = EditGroupListAdapter.getMemberNo(i)
            memberSum += memberNo
            if (memberNo <= 0 || memberSum > memberArray.size) {
                allowToNext = false
            }
        }

        if (allowToNext!!) {
            recreateGrouplist()
            val intent = Intent(this, NormalKumiwakeConfirmation::class.java)
            intent.putExtra(NormalMode.NORMAL_MEMBER_ARRAY, memberArray)
            intent.putExtra(NormalMode.NORMAL_GROUP_ARRAY, newGroupArray)
            intent.putExtra(EVEN_FM_RATIO, even_fm_ratio_check.isChecked)
            intent.putExtra(EVEN_AGE_RATIO, even_age_ratio_check.isChecked)
            intent.putExtra(KumiwakeCustom.EVEN_GRADE_RATIO, even_grade_ratio_check.isChecked)
            intent.putExtra(KumiwakeCustom.EVEN_ROLE, custom_spinner.selectedItem as String)
            startActivity(intent)
            overridePendingTransition(R.anim.in_right, R.anim.out_left)
        } else {
            error_member_no_txt.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.back_to_initial_mbNo)
    internal fun onbcClicked() {
        var et: EditText
        for (i in 0 until groupList.count) {
            et = groupList.getChildAt(i).findViewById<View>(R.id.editTheNumberOfMember) as EditText
            et.isFocusable = false
            et.setText(groupArray[i].belongNo.toString())
            et.isFocusableInTouchMode = true
            et.setTextColor(Color.BLACK)
        }
    }

    @SuppressLint("SetTextI18n")
    fun changeLeader(position: Int) {
        val newRole = StringBuilder()
        val roleItem = memberArray[position].role
        if (roleItem.matches((".*" + getText(R.string.leader) + ".*").toRegex())) {
            val list = deleteLeaderList(roleItem)
            val nowldNo = Integer.parseInt(list[0].substring(2))
            MBListViewAdapter.leaderNoArray?.removeAt(MBListViewAdapter.leaderNoArray!!.indexOf(nowldNo))
            list.remove(list[0])
            val leader = groupList.getChildAt(nowldNo - 1).findViewById<View>(R.id.leader) as TextView
            leader.text = getText(R.string.leader).toString() + ":" + getText(R.string.nothing)

            for (j in list.indices) {
                newRole.append(list[j])
                if (j != list.size - 1) {
                    newRole.append(",")
                }
            }
        } else {
            MBListViewAdapter.ldNo = 1
            while (MBListViewAdapter.leaderNoArray?.contains(MBListViewAdapter.ldNo)!!) {
                MBListViewAdapter.ldNo++
            }
            if (MBListViewAdapter.ldNo != groupArray.size + 1) {
                newRole.append(roleItem)
                newRole.append("," + getText(R.string.leader))
                val leader = groupList.getChildAt(MBListViewAdapter.ldNo - 1).findViewById<View>(R.id.leader) as TextView
                leader.text = getText(R.string.leader).toString() + ":" + memberArray[position].name
            }
        }
        memberArray[position] = Name(memberArray[position].id, memberArray[position].name,
                memberArray[position].sex, memberArray[position].age,
                memberArray[position].grade, memberArray[position].belong, newRole.toString(),
                memberArray[position].read)
        mbAdapter?.notifyDataSetChanged()
    }

    private fun recreateGrouplist() {
        newGroupArray = ArrayList()
        for (i in 0 until groupList.count) {
            val groupName = EditGroupListAdapter.getGroupName(i)
            val memberNo = EditGroupListAdapter.getMemberNo(i)
            newGroupArray.add(GroupListAdapter.Group(i, groupName, memberNo, null.toString()))
        }
    }

    companion object {

        const val EVEN_FM_RATIO = "even_fm_ratio"
        const val EVEN_AGE_RATIO = "even_age_ratio"
        const val EVEN_GRADE_RATIO = "even_grade_ratio"
        const val EVEN_ROLE = "even_role"

        lateinit var memberList: ListView
        lateinit var groupList: ListView
        lateinit var groupNo: TextView
        lateinit var scrollView: ScrollView

        lateinit var memberArray: ArrayList<Name>
        lateinit var origMemberArray: ArrayList<Name>
        lateinit var groupArray: ArrayList<GroupListAdapter.Group>
        lateinit var newGroupArray: ArrayList<GroupListAdapter.Group>

        fun deleteLeaderList(roleItem: String): MutableList<String> {
            val roleArray = roleItem.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val list = ArrayList(Arrays.asList<String>(*roleArray))
            val hs = HashSet<String>()
            hs.addAll(list)
            list.clear()
            list.addAll(hs)
            list.remove(R.string.leader.toString())
            list.remove("")
            list.sort()
            return list
        }

        fun changeBelongNo(position: Int, addNo: Int) {
            val et: EditText = if (position == groupList.count - 1) {
                groupList.getChildAt(0).findViewById<View>(R.id.editTheNumberOfMember) as EditText
            } else {
                groupList.getChildAt(position + 1).findViewById<View>(R.id.editTheNumberOfMember) as EditText
            }
            var nowNo = 0
            val newNo: Int

            if (et.text.toString().isNotEmpty()) {
                nowNo = Integer.parseInt(et.text.toString())
            }
            newNo = nowNo + addNo
            et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
            et.setText(newNo.toString())
            if (newNo < 0) {
                et.setTextColor(Color.RED)
            } else {
                et.setTextColor(Color.BLACK)
            }
        }
    }

}