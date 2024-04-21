package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.AddGroupKeys
import com.pandatone.kumiwake.FirebaseAnalyticsEvents
import com.pandatone.kumiwake.MyGestureListener
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.databinding.AddGroupBinding
import com.pandatone.kumiwake.member.function.GroupMethods
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.members.FragmentGroupMain
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse

/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddGroup : AppCompatActivity() {
    private lateinit var binding: AddGroupBinding
    private var textInputLayout: TextInputLayout? = null
    private var nextId = FragmentGroupMain.gpAdapter.maxId + 1 //FragmentGroupMainなしだとX
    private lateinit var adapter: SmallMBListAdapter
    private lateinit var listView: ListView
    private var editId: Int = 0
    private var members: ArrayList<Member> = ArrayList()
    private lateinit var groupEditText: AppCompatEditText
    private lateinit var gpAdapter: GroupAdapter
    private lateinit var mbAdapter: MemberAdapter
    private lateinit var mDetector: GestureDetectorCompat

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
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setTheme(StatusHolder.nowTheme)
        binding = AddGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gpAdapter = GroupAdapter(this)
        mbAdapter = MemberAdapter(this)
        findViews()
        editId = intent.getIntExtra(AddGroupKeys.EDIT_ID.key, nextId)
        if (editId != nextId) {
            setItem(editId)
        }
        members = GroupMethods.searchBelong(this, editId.toString())
        Toast.makeText(this, getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm, groupEditText))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, groupEditText))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    //各Viewの初期化処理
    private fun findViews() {
        groupEditText = findViewById<View>(R.id.input_group) as AppCompatEditText
        textInputLayout = findViewById<View>(R.id.group_form_input_layout) as TextInputLayout
        listView =
            findViewById<View>(R.id.add_group_listView).findViewById<View>(R.id.memberListView) as ListView
        listView.emptyView =
            findViewById<View>(R.id.add_group_listView).findViewById(R.id.emptyMemberList)
        binding.addGroupListView.memberRegisterAndAddBtn.visibility = View.GONE
        findViewById<View>(R.id.add_group_listView).findViewById<View>(R.id.member_add_btn)
            .setOnClickListener { moveMemberMain() }
        findViewById<View>(R.id.group_registration_btn).setOnClickListener { register() } //登録ボタンの処理
        findViewById<View>(R.id.group_cancel_btn).setOnClickListener { finish() } //cancelボタンの処理
    }

    @SuppressLint("SetTextI18n")
    public override fun onStart() {
        super.onStart()
        adapter = SmallMBListAdapter(this@AddGroup, members)
        listView.adapter = adapter
        binding.addGroupListView.numberOfSelectedMember.text =
            adapter.count.toString() + getString(R.string.people) + getString(R.string.selected)
    }

    //バックキーの処理
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogWarehouse(supportFragmentManager).decisionDialog(
                "KUMIWAKE",
                getString(R.string.edit_exit_confirmation)
            ) { finish() }
            return true
        }
        return false
    }

    //グループ更新編集時の初期情報表示処理
    private fun setItem(id: Int) {
        val groupList = GroupAdapter(this).getAllGroups()
        for (group in groupList) {
            if (group.id == id) {
                groupEditText.setText(group.name)
                break
            }
        }
        //registerボタンをupdateボタンに
        update(id)
    }

    //registerボタンの処理
    private fun register() {
        val group = groupEditText.text!!.toString()
        if (TextUtils.isEmpty(group)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_group)
        } else {
            saveItem()
            cleanUpBelong()
            Toast.makeText(
                this,
                getString(R.string.group) + " \"" + group + "\" " + getString(R.string.registered),
                Toast.LENGTH_SHORT
            ).show()
            FirebaseAnalyticsEvents.groupRegisterEvent(group, adapter.count)
            finish()
        }
    }

    //Group_DBに新規グループの登録
    private fun saveItem() {
        val name = groupEditText.text!!.toString()
        updateBelong()
        gpAdapter.saveGroup(name, name, adapter.count)
    }

    //updateボタンの処理
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
                cleanUpBelong()
                Toast.makeText(
                    applicationContext,
                    getText(R.string.group).toString() + " \"" + group + "\" " + getText(R.string.updated),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    //Group_DBの既存グループの更新
    private fun updateItem(listId: Int) {
        val name = groupEditText.text!!.toString()
        updateBelong()
        gpAdapter.updateGroup(listId, name, name, adapter.count)
    }

    //メンバー選択(ChoiceMemberMain)へ移動
    private fun moveMemberMain() {
        val intent = Intent(this, ChoiceMemberMain::class.java)
        intent.putExtra(AddGroupKeys.MEMBER_ARRAY.key, members)
        startActivityForResult(intent, 0)
    }

    //memberのbelongを更新
    private fun updateBelong() {
        val nameList = mbAdapter.getAllMembers()
        nameList.forEach { member ->
            val listId = member.id
            if (members.contains(member)) {
                val newBelong = StringBuilder()
                newBelong.append(member.belong)
                newBelong.append(",$groupId")
                mbAdapter.updateBelong(listId.toString(), newBelong.toString())
            } else {
                deleteBelongInfo(member, groupId, listId)
            }
        }
    }

    //指定したremoveIdのBelongを削除
    private fun deleteBelongInfo(member: Member, removeId: Int, listId: Int) {
        val belongText = member.belong
        val belongArray =
            belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list = java.util.ArrayList(listOf(*belongArray))
        val hs = HashSet<String>()
        hs.addAll(list)
        list.clear()
        list.addAll(hs)
        if (list.contains(removeId.toString())) {
            list.remove(removeId.toString())
            val newBelong = list.joinToString(separator = ",")
            mbAdapter.updateBelong(listId.toString(), newBelong)
        }
    }

    //重複するBelongの削除
    private fun cleanUpBelong() {
        val nameList = mbAdapter.getAllMembers()
        nameList.forEach { member ->
            val listId = member.id
            val belongText = member.belong
            val belongArray =
                belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList(listOf(*belongArray))
            //HashSetによって重複を削除
            val hs = HashSet<String>()
            hs.addAll(list)
            list.clear()
            list.addAll(hs)
            val newBelong = list.joinToString(separator = ",")
            mbAdapter.updateBelong(listId.toString(), newBelong)
        }
    }

    //メンバー選択(ChoiceMemberMain)からのコールバック
    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)

        if (resultCode == Activity.RESULT_OK) {
            members = i!!.getSerializableExtra(AddGroupKeys.MEMBER_ARRAY.key) as ArrayList<Member>
        }
        adapter = SmallMBListAdapter(this@AddGroup, members)
        listView.adapter = adapter
        val selectedTxt =
            adapter.count.toString() + getString(R.string.people) + getString(R.string.selected)
        binding.addGroupListView.numberOfSelectedMember.text = selectedTxt
    }

}