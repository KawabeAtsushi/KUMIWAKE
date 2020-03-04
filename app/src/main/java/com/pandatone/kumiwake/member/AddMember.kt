package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.AddMemberKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.function.MemberClick
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import com.pandatone.kumiwake.ui.members.FragmentMemberMain
import kotlinx.android.synthetic.main.add_member.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/02/24.
 */
class AddMember : AppCompatActivity() {
    private var beforeBelong: Array<String>? = null
    private var afterBelong: Array<String>? = null
    private var nameEditText: AppCompatEditText? = null
    private var readEditText: AppCompatEditText? = null
    private var textInputLayout: TextInputLayout? = null
    private var sexGroup: RadioGroup? = null
    private var sexButton: RadioButton? = null
    private var ageEditText: EditText? = null
    private var belongDropdown: Button? = null
    private var mbAdapter: MemberAdapter? = null
    private var fromNormalMode = false
    private lateinit var groupList: ArrayList<Group>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.add_member)
        ButterKnife.bind(this)
        mbAdapter = MemberAdapter(this)
        beforeBelong = null
        afterBelong = null
        findViews()
        ageEditText!!.setText("")
        val i = intent
        val member = i.getSerializableExtra(AddMemberKeys.MEMBER.key) as Member?
        fromNormalMode = i.getBooleanExtra(AddMemberKeys.FROM_NORMAL_MODE.key, false)
        if (member != null) {
            setItem(member)
            member_registration_continue_btn.visibility = View.GONE
        }

        var yomigana = ""

        nameEditText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (s.matches("^[a-zA-Z0-9ぁ-ん]+$".toRegex()) || s.toString() == "") {
                    yomigana = s.toString()
                }
                readEditText?.setText(yomigana)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })
    }

    //Viewの宣言
    private fun findViews() {
        nameEditText = findViewById<View>(R.id.input_name) as TextInputEditText
        readEditText = findViewById<View>(R.id.input_name_read) as TextInputEditText
        textInputLayout = findViewById<View>(R.id.member_form_input_layout) as TextInputLayout
        sexGroup = findViewById<View>(R.id.sexGroup) as RadioGroup
        ageEditText = findViewById<View>(R.id.input_age) as EditText
        belongDropdown = findViewById<View>(R.id.select_group_choicer) as Button
    }

    //Updateの場合の初期アイテム表示
    private fun setItem(member: Member) {
        val listId = member.id
        val name = member.name
        val read = member.read
        val sex = member.sex
        val age = member.age.toString()
        val belong = MemberClick.viewBelong(member, mbAdapter!!)

        nameEditText!!.setText(name)
        readEditText!!.setText(read)
        if (sex == getString(R.string.woman)) {
            sexGroup!!.check(R.id.womanBtn)
        } else {
            sexGroup!!.check(R.id.manBtn)
        }
        ageEditText!!.setText(age)
        belongDropdown!!.text = belong

        update(listId)

    }


    @SuppressLint("SetTextI18n")
    @OnClick(R.id.select_group_choicer)
    internal fun onSelectGroupDropdownClicked(view: View) {
        // 選択中の候補を取得
        val buttonText = belongDropdown!!.text.toString()
        val textArray = buttonText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        groupList = GroupAdapter(this).getAllGroups()
        // 候補リスト
        val list = ArrayList<String>()
        for (group in groupList) {
            list.add(group.name)
        }
        val belongArray = list.toTypedArray()
        // 選択リスト
        val checkArray = BooleanArray(belongArray.size)
        for (i in belongArray.indices) {
            checkArray[i] = false
            for (data in textArray) {
                if (belongArray[i] == data) {
                    checkArray[i] = true
                    break
                }
            }
        }
        // ダイアログを生成
        val dialog = AlertDialog.Builder(this)
        // 選択イベント
        dialog.setMultiChoiceItems(belongArray, checkArray) { _, value, isChecked ->
            val text = belongDropdown!!.text.toString()
            // 選択された場合
            if (isChecked) {
                // ボタンの表示に追加
                belongDropdown!!.text = text + (if ("" == text) "" else ",") + belongArray[value]
            } else {
                // ボタンの表示から削除
                when {
                    text.indexOf(belongArray[value] + ",") >= 0 -> belongDropdown!!.text = text.replace(belongArray[value] + ",", "")
                    text.indexOf("," + belongArray[value]) >= 0 -> belongDropdown!!.text = text.replace("," + belongArray[value], "")
                    else -> belongDropdown!!.text = text.replace(belongArray[value], "")
                }
            }
        }
        dialog.setPositiveButton(getText(R.string.decide), null)
        dialog.setNeutralButton(getText(R.string.clear)) { _, _ ->
            belongDropdown!!.text = ""
            // 再表示
            onSelectGroupDropdownClicked(view)
        }
        dialog.setNegativeButton(getText(R.string.cancel)) { _, _ ->
            // 選択前の状態に戻す
            belongDropdown!!.text = buttonText
        }
        dialog.show()

        beforeBelong = textArray
    }

    @OnClick(R.id.member_registration_finish_btn)
    internal fun onRegistrationMemberClicked() {
        register(true)
    }

    @OnClick(R.id.member_registration_continue_btn)
    internal fun onRegistrationContinueMemberClicked() {
        register(false)
    }

    @OnClick(R.id.member_cancel_btn)
    internal fun cancel() {
        finish()
    }

    //バックキーの処理
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogWarehouse(supportFragmentManager).decisionDialog("KUMIWAKE", getString(R.string.edit_exit_confirmation)) { finish() }
            return true
        }
        return false
    }

    //メンバー登録　finish true:終了, false:続けて登録
    private fun register(finish: Boolean) {
        val name = nameEditText!!.text!!.toString()
        if (TextUtils.isEmpty(name)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_name)
        } else {
            saveItem()
            afterBelong = belongDropdown!!.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            changeBelongNo()

            if (fromNormalMode) {
                NormalMode.memberArray.add(MemberAdapter(this).newMember)
            }

            Toast.makeText(this, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show()

            if (finish) {
                finish()
            } else {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    //メンバー登録処理
    private fun saveItem() {
        sexButton = findViewById<View>(sexGroup!!.checkedRadioButtonId) as RadioButton

        val name = nameEditText!!.text!!.toString()
        val read = readEditText!!.text!!.toString()
        val sex = sexButton!!.text as String
        val age = getValue(ageEditText!!)
        val belong = belongConvertToNo()

        mbAdapter!!.saveName(name, sex, age, belong, read)
    }

    //メンバー情報更新ボタンリスナー
    private fun update(listId: Int) {
        val updateBt = findViewById<View>(R.id.member_registration_finish_btn) as Button
        updateBt.setText(R.string.update)
        updateBt.setOnClickListener {
            val name = nameEditText!!.text!!.toString()
            if (TextUtils.isEmpty(name)) {
                textInputLayout!!.isErrorEnabled = true
                textInputLayout!!.error = getText(R.string.error_empty_name)
            } else {
                updateItem(listId)
                afterBelong = belongDropdown!!.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                changeBelongNo()
                Toast.makeText(applicationContext, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.updated), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //メンバー情報更新処理
    private fun updateItem(listId: Int) {
        sexButton = findViewById<View>(sexGroup!!.checkedRadioButtonId) as RadioButton
        val name = nameEditText!!.text!!.toString()
        val read = readEditText!!.text!!.toString()
        val sex = sexButton!!.text as String
        val age = getValue(ageEditText!!)
        val belong = belongConvertToNo()

        mbAdapter!!.updateMember(listId, name, sex, age, belong, read)
        FragmentMemberMain().loadName()
    }

    //メンバーのbelong更新に伴う、グループの所属人数データの更新
    private fun changeBelongNo() {
        var i = 0
        var j: Int
        var k: Int
        var id: Int
        var belongNo: Int
        var change: Boolean? = true
        val groupListAdapter = GroupAdapter(this)

        val beforeBelongNo: Int = if (beforeBelong != null) {
            beforeBelong!!.size
        } else {
            0
        }
        val afterBelongNo: Int = if (afterBelong != null) {
            afterBelong!!.size
        } else {
            0
        }

        while (i < beforeBelongNo) {
            j = 0
            while (j < afterBelongNo) {

                if (beforeBelong!![i] == afterBelong!![j]) {
                    change = false
                }
                j++
            }
            if (change!!) {
                k = 0
                while (k < groupList.size) {
                    val group = groupList[k]
                    val groupName = group.name
                    if (beforeBelong!![i] == groupName) {
                        id = group.id
                        belongNo = group.belongNo - 1
                        groupListAdapter.updateBelongNo(id.toString(), belongNo)
                    }
                    k++
                }
            }
            i++
        }

        change = true

        i = 0
        while (i < afterBelongNo) {
            j = 0
            while (j < beforeBelongNo) {
                if (afterBelong!![i] == beforeBelong!![j]) {
                    change = false
                }
                j++
            }
            if (change!!) {
                k = 0
                while (k < groupList.size) {
                    val group = groupList[k]
                    val groupName = group.name
                    if (afterBelong!![i] == groupName) {
                        id = group.id
                        belongNo = group.belongNo + 1
                        groupListAdapter.updateBelongNo(id.toString(), belongNo)
                    }
                    k++
                }
            }
            i++
        }
    }

    //String to Int
    private fun getValue(toText: EditText): Int {
        val text = toText.text.toString()
        var a = 0
        if (text.isNotEmpty()) {
            a = Integer.parseInt(text)
        }
        return a
    }

    //グループ名をグループIDに変換
    private fun belongConvertToNo(): String {
        val belongText = belongDropdown!!.text.toString()
        val belongTextArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val belongNo = StringBuilder()

        for (belongGroup in belongTextArray) {
            for (group in groupList) {
                if (belongGroup == group.name) {
                    val groupId = group.id.toString()
                    belongNo.append("$groupId,")
                }
            }
        }
        return belongNo.toString()
    }
}







