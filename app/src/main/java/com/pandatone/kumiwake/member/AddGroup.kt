package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.NameListAdapter
import com.pandatone.kumiwake.member.FragmentMemberChoiceMode.Companion.nameList
import com.pandatone.kumiwake.ui.DialogWarehouse
import com.pandatone.kumiwake.ui.members.FragmentGroupMain
import com.pandatone.kumiwake.ui.members.FragmentMemberMain
import kotlinx.android.synthetic.main.part_review_listview.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddGroup : AppCompatActivity() {
    private var textInputLayout: TextInputLayout? = null
    private var nextId = FragmentGroupMain.dbAdapter.maxId + 1 //FragmentGroupMainなしだとX
    private lateinit var adapter: MBListViewAdapter
    private lateinit var listView: ListView
    private var editId: Int = 0
    private var members: ArrayList<Name> = ArrayList()
    private lateinit var groupEditText: AppCompatEditText
    private lateinit var dbAdapter: GroupListAdapter

    private val groupId: Int
        get() {
            return if (editId == nextId) {
                nextId
            } else {
                editId
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_group)
        ButterKnife.bind(this)
        dbAdapter = GroupListAdapter(this)
        findViews()
        val i = intent
        editId = i.getIntExtra(GROUP_ID, nextId)
        if (editId != nextId) {
            setItem(editId)
        }

        members = if (editId == nextId) {
            FragmentMemberMain().searchBelong(nextId.toString())
        } else {
            FragmentMemberMain().searchBelong(editId.toString())
        }
    }

    private fun findViews() {
        groupEditText = findViewById<View>(R.id.input_group) as AppCompatEditText
        textInputLayout = findViewById<View>(R.id.group_form_input_layout) as TextInputLayout
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
        val addMember = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.member_add_btn) as Button
        member_register_and_add_btn.visibility = View.GONE
        addMember.setOnClickListener { moveMemberMain() }
    }

    @SuppressLint("SetTextI18n")
    public override fun onStart() {
        super.onStart()
        adapter = MBListViewAdapter(this@AddGroup, members, false, showLeaderNo = false)
        listView.adapter = adapter
        numberOfSelectedMember.text = adapter.count.toString() + getString(R.string.people) + getString(R.string.selected)
        FragmentMemberMain().duplicateBelong()
    }


    @OnClick(R.id.group_registration_btn)
    internal fun onRegistrationGroupClicked() {
        val group = groupEditText.text!!.toString()
        if (TextUtils.isEmpty(group)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_group)
        } else {
            saveItem()
            Toast.makeText(this, getString(R.string.group) + " \"" + group + "\" " + getString(R.string.registered), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @OnClick(R.id.group_cancel_btn)
    internal fun cancel() {
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogWarehouse(supportFragmentManager).decisionDialog("KUMIWAKE",getString(R.string.app_exit_confirmation)){finish()}
            return true
        }
        return false
    }

    private fun saveItem() {
        val name = groupEditText.text!!.toString()
        updateBelong()
        dbAdapter.saveGroup(name, name, adapter.count)
    }

    private fun updateItem(listId: Int) {
        val name = groupEditText.text!!.toString()
        updateBelong()
        dbAdapter.open()
        dbAdapter.updateGroup(listId, name, name, adapter.count)
        dbAdapter.close()
    }


    private fun setItem(id: Int) {
        for (group in FragmentGroupMain.groupList) {
            if (group.id == id) {
                groupEditText.setText(group.group)
                break
            }
        }
        update(id)
    }

    private fun update(listId: Int) {
        val updateBt = findViewById<View>(R.id.group_registration_btn) as Button
        updateBt.setText(R.string.update)
        updateBt.setOnClickListener {
            val group = groupEditText.text!!.toString()
            if (TextUtils.isEmpty(group)) {
                textInputLayout!!.isErrorEnabled = true
                textInputLayout!!.error = getText(R.string.error_empty_name)
            } else {
                updateItem(listId)
                Toast.makeText(applicationContext, getText(R.string.group).toString() + " \"" + group + "\" " + getText(R.string.updated), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun moveMemberMain() {
        val intent = Intent(this, MemberMain::class.java)
        intent.putExtra(MemberMain.GROUP_ID, groupId)
        startActivity(intent)
    }

    private fun updateBelong() {
        val mbAdapter = MemberListAdapter(this)
        mbAdapter.open()     // DBの読み込み(読み書きの方)
        var i = 1
        val nameList = mbAdapter.getAllMembers()
        nameList.forEach { member ->
            val listId = member.id
            val newId = MemberMain.groupId
            if (members.contains(member)) {
                val newBelong = StringBuilder()
                newBelong.append(member.belong)
                newBelong.append("$newId,")
                mbAdapter.addBelong(listId.toString(), newBelong.toString())
            } else {
                deleteBelongInfo(member, newId, listId)
            }
        }

        mbAdapter.close()    // DBを閉じる
        loadName()
    }

    private fun deleteBelongInfo(listItem: Name, groupId: Int, listId: Int) {
        val belongText = listItem.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list = java.util.ArrayList(Arrays.asList<String>(*belongArray))
        val hs = HashSet<String>()
        hs.addAll(list)
        list.clear()
        list.addAll(hs)
        if (list.contains(groupId.toString())) {
            list.remove(groupId.toString())
            val newBelong = StringBuilder()

            for (item in list) {
                newBelong.append("$item,")
            }
            dbAdapter.addBelong(listId.toString(), newBelong.toString())
        }
    }

    companion object {
        const val GROUP_ID = "group_id"
    }

}