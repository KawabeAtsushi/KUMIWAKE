package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import java.util.*

/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddGroup : AppCompatActivity() {
    private var textInputLayout: TextInputLayout? = null
    private var nextId = FragmentGroup.dbAdapter.maxId + 1
    internal lateinit var adapter: MBListViewAdapter
    private lateinit var listView: ListView
    internal var position: Int = 0

    private val clicked = View.OnClickListener { moveMemberMain() }

    private val groupId: Int
        get() {
            val groupId: Int
            if (position == nextId) {
                groupId = nextId
            } else {
                groupId = FragmentGroup.nameList[position].id
            }
            return groupId
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_group)
        ButterKnife.bind(this)
        dbAdapter = GroupListAdapter(this)
        findViews()
        val i = intent
        position = i.getIntExtra("POSITION", nextId)
        if (position != nextId) {
            setItem(position)
        }
        FragmentMember().deleteBelongInfoAll(nextId)
    }

    protected fun findViews() {
        groupEditText = findViewById<View>(R.id.input_group) as AppCompatEditText
        textInputLayout = findViewById<View>(R.id.group_form_input_layout) as TextInputLayout
        listView = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.reviewListView) as ListView
        numberOfSelectedMember = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.numberOfSelectedMember) as TextView
        listView.emptyView = findViewById<View>(R.id.add_group_listview).findViewById(R.id.emptyMemberList)
        val addMember = findViewById<View>(R.id.add_group_listview).findViewById<View>(R.id.member_add_btn) as Button
        addMember.setOnClickListener(clicked)
    }

    @SuppressLint("SetTextI18n")
    public override fun onStart() {
        super.onStart()
        val nameByBelong: ArrayList<Name> = if (position == nextId) {
            FragmentMember().searchBelong(nextId.toString())
        } else {
            FragmentMember().searchBelong(FragmentGroup.nameList[position].id.toString())
        }
        adapter = MBListViewAdapter(this@AddGroup, nameByBelong, 0)
        listView.adapter = adapter
        numberOfSelectedMember.text = adapter.count.toString() + getString(R.string.person) + getString(R.string.selected)
        FragmentMember().duplicateBelong()
    }


    @OnClick(R.id.group_registration_button)
    internal fun onRegistrationGroupClicked() {
        val group = groupEditText.text!!.toString()
        if (TextUtils.isEmpty(group)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_group)
        } else {
            saveItem()
            Toast.makeText(this, getText(R.string.group).toString() + " \"" + group + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun saveItem() {
        val name = groupEditText.text!!.toString()
        dbAdapter.open()
        dbAdapter.saveGroup(name, name, adapter.count)
        dbAdapter.close()
        FragmentMember().loadName()
    }

    private fun updateItem(listId: Int) {
        val name = groupEditText.text!!.toString()
        dbAdapter.open()
        dbAdapter.updateGroup(listId, name, name, adapter.count)
        dbAdapter.close()
        FragmentMember().loadName()
    }


    private fun setItem(position: Int) {
        val listItem = FragmentGroup.nameList[position]
        val listId = listItem.id
        val group = listItem.group
        groupEditText.setText(group)

        update(listId)
    }

    private fun update(listId: Int) {
        val updateBt = findViewById<View>(R.id.group_registration_button) as Button
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
        intent.putExtra(MemberMain.VISIBLE, true)
        intent.putExtra(MemberMain.DELETE_ICON_VISIBLE, false)
        intent.putExtra(MemberMain.START_ACTION_MODE, true)
        intent.putExtra(MemberMain.GROUP_ID, groupId)
        startActivity(intent)
    }

    companion object {

        internal lateinit var groupEditText: AppCompatEditText
        internal lateinit var numberOfSelectedMember: TextView
        internal lateinit var dbAdapter: GroupListAdapter
    }

}