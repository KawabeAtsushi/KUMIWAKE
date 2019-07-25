package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter


/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddGroup : AppCompatActivity() {
    private var textInputLayout: TextInputLayout? = null
    private var nextId = FragmentGroup.dbAdapter.maxId + 1 //FragmentGroupなしだとX
    private lateinit var adapter: MBListViewAdapter
    private lateinit var listView: ListView
    private var editId: Int = 0
    private var firstMembers: ArrayList<Name> = ArrayList()

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

        val nameByBelong: ArrayList<Name> = if (editId == nextId) {
            FragmentMember().searchBelong(nextId.toString())
        } else {
            FragmentMember().searchBelong(editId.toString())
        }
        firstMembers = nameByBelong
    }

    private fun findViews() {
        groupEditText = findViewById<View>(R.id.input_group) as AppCompatEditText
        textInputLayout = findViewById<View>(R.id.group_form_input_layout) as TextInputLayout
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.memberListView) as ListView
        numberOfSelectedMember = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.numberOfSelectedMember) as TextView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
        val addMember = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.member_add_btn) as Button
        addMember.setOnClickListener { moveMemberMain() }
    }

    @SuppressLint("SetTextI18n")
    public override fun onStart() {
        super.onStart()
        val nameByBelong: ArrayList<Name> = if (editId == nextId) {
            FragmentMember().searchBelong(nextId.toString())
        } else {
            FragmentMember().searchBelong(editId.toString())
        }

        adapter = MBListViewAdapter(this@AddGroup, nameByBelong, false)
        listView.adapter = adapter
        numberOfSelectedMember.text = adapter.count.toString() + getString(R.string.people) + getString(R.string.selected)
        FragmentMember().duplicateBelong()
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
            MemberMain.startAction = false
            finish()
        }
    }

    @OnClick(R.id.group_cancel_btn)
    internal fun cancel() {
        FragmentMember().deleteBelongInfoAll(editId)
        if (editId != nextId)
            restoreBelong()
        MemberMain.startAction = false
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            FragmentMember().deleteBelongInfoAll(editId)
            if (editId != nextId)
                restoreBelong()
            MemberMain.startAction = false
            finish()
            return true
        }
        return false
    }

    private fun saveItem() {
        val name = groupEditText.text!!.toString()
        dbAdapter.open()
        dbAdapter.saveGroup(name, name, adapter.count)
        dbAdapter.close()
    }

    private fun updateItem(listId: Int) {
        val name = groupEditText.text!!.toString()
        dbAdapter.open()
        dbAdapter.updateGroup(listId, name, name, adapter.count)
        dbAdapter.close()
    }


    private fun setItem(id: Int) {
        for (group in FragmentGroup.groupList) {
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
        intent.putExtra(MemberMain.DELETE_ICON_VISIBLE, false)
        intent.putExtra(MemberMain.ACTION_MODE, true)
        intent.putExtra(MemberMain.NORMAL_SELECT, false)
        intent.putExtra(MemberMain.GROUP_ID, groupId)
        startActivity(intent)
    }

    private fun restoreBelong() {
        val firstMemberIds: ArrayList<Int> = ArrayList()

        for (member in firstMembers) {
            firstMemberIds.add(member.id)
        }

        FragmentMember.dbAdapter.open()     // DBの読み込み(読み書きの方)
        var i = 1
        while (i < FragmentMember.listAdp.count) {
            FragmentMember.listItem = FragmentMember.nameList[i]
            val listId = FragmentMember.listItem.id

            if (firstMemberIds.contains(listId)) {
                val newBelong = StringBuilder()
                newBelong.append(FragmentMember.listItem.belong)
                newBelong.append("$editId,")
                FragmentMember.dbAdapter.addBelong(listId.toString(), newBelong.toString())
            }
            i += 2
        }
        FragmentMember.dbAdapter.close()    // DBを閉じる
    }

    companion object {
        internal lateinit var groupEditText: AppCompatEditText
        internal lateinit var numberOfSelectedMember: TextView
        internal lateinit var dbAdapter: GroupListAdapter
        const val GROUP_ID = "group_id"
    }

}