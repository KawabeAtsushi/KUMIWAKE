package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import java.util.*

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
    private var belongSpinner: Button? = null
    private var dbAdapter: MemberListAdapter? = null
    private var fromNormalMode = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_member)
        ButterKnife.bind(this)
        dbAdapter = MemberListAdapter(this)
        beforeBelong = null
        afterBelong = null
        findViews()
        ageEditText!!.setText("")
        val i = intent
        val position = i.getIntExtra(POSITION, -1)
        fromNormalMode = i.getBooleanExtra(FROM_NORMAL_MODE, false)
        if (position != -1) {
            setItem(position)
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

    private fun findViews() {
        nameEditText = findViewById<View>(R.id.input_name) as TextInputEditText
        readEditText = findViewById<View>(R.id.input_name_read) as TextInputEditText
        textInputLayout = findViewById<View>(R.id.member_form_input_layout) as TextInputLayout
        sexGroup = findViewById<View>(R.id.sexGroup) as RadioGroup
        ageEditText = findViewById<View>(R.id.input_age) as EditText
        belongSpinner = findViewById<View>(R.id.select_group_spinner) as Button
    }

    private fun setItem(position: Int) {
        val listItem = FragmentMember.nameList[position]
        val listId = listItem.id
        val name = listItem.name
        val read = listItem.read
        val sex = listItem.sex
        val age = listItem.age.toString()
        val belong = MemberClick.viewBelong(position)

        nameEditText!!.setText(name)
        readEditText!!.setText(read)
        if (sex == getString(R.string.woman)) {
            sexGroup!!.check(R.id.womanBtn)
        } else {
            sexGroup!!.check(R.id.manBtn)
        }
        ageEditText!!.setText(age)
        belongSpinner!!.text = belong

        update(listId)

    }


    @SuppressLint("SetTextI18n")
    @OnClick(R.id.select_group_spinner)
    internal fun onSelectGroupSpinnerClicked(view: View) {
        // 選択中の候補を取得
        val buttonText = belongSpinner!!.text.toString()
        val textArray = buttonText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        // 候補リスト
        val list = ArrayList<String>()
        for (listItem in FragmentGroup.groupList) {
            list.add(listItem.group)
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
            val text = belongSpinner!!.text.toString()
            // 選択された場合
            if (isChecked) {
                // ボタンの表示に追加
                belongSpinner!!.text = text + (if ("" == text) "" else ",") + belongArray[value]
            } else {
                // ボタンの表示から削除
                when {
                    text.indexOf(belongArray[value] + ",") >= 0 -> belongSpinner!!.text = text.replace(belongArray[value] + ",", "")
                    text.indexOf("," + belongArray[value]) >= 0 -> belongSpinner!!.text = text.replace("," + belongArray[value], "")
                    else -> belongSpinner!!.text = text.replace(belongArray[value], "")
                }
            }
        }
        dialog.setPositiveButton(getText(R.string.decide), null)
        dialog.setNeutralButton(getText(R.string.clear)) { _, _ ->
            belongSpinner!!.text = ""
            // 再表示
            onSelectGroupSpinnerClicked(view)
        }
        dialog.setNegativeButton(getText(R.string.cancel)) { _, _ ->
            // 選択前の状態に戻す
            belongSpinner!!.text = buttonText
        }
        dialog.show()

        beforeBelong = textArray
    }

    @OnClick(R.id.member_registration_btn)
    internal fun onRegistrationMemberClicked() {
        val name = nameEditText!!.text!!.toString()
        if (TextUtils.isEmpty(name)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_name)
        } else {
            saveItem()
            afterBelong = belongSpinner!!.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            changeBelongNo()
            if(fromNormalMode){
                val intent = Intent(this, NormalMode::class.java)
                intent.putExtra(NormalMode.NEW_MEMBER, MemberListAdapter(this).newMember)
                setResult(NormalMode.ADD_MEMBER_OK, intent)
            }else {
                Toast.makeText(this, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    @OnClick(R.id.member_cancel_btn)
    internal fun cancel() {
        finish()
    }

    private fun updateItem(listId: Int) {
        sexButton = findViewById<View>(sexGroup!!.checkedRadioButtonId) as RadioButton
        val name = nameEditText!!.text!!.toString()
        val read = readEditText!!.text!!.toString()
        val sex = sexButton!!.text as String
        val age = getValue(ageEditText!!)
        val belong = belongConvertToNo()

        dbAdapter!!.updateMember(listId, name, sex, age, belong, read)
        FragmentMember().loadName()
    }

    private fun update(listId: Int) {
        val updateBt = findViewById<View>(R.id.member_registration_btn) as Button
        updateBt.setText(R.string.update)
        updateBt.setOnClickListener {
            val name = nameEditText!!.text!!.toString()
            if (TextUtils.isEmpty(name)) {
                textInputLayout!!.isErrorEnabled = true
                textInputLayout!!.error = getText(R.string.error_empty_name)
            } else {
                updateItem(listId)
                afterBelong = belongSpinner!!.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                changeBelongNo()
                Toast.makeText(applicationContext, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.updated), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveItem() {
        sexButton = findViewById<View>(sexGroup!!.checkedRadioButtonId) as RadioButton

        val name = nameEditText!!.text!!.toString()
        val read = readEditText!!.text!!.toString()
        val sex = sexButton!!.text as String
        val age = getValue(ageEditText!!)
        val belong = belongConvertToNo()

        dbAdapter!!.saveName(name, sex, age, belong, read)
    }

    private fun changeBelongNo() {
        var i = 0
        var j: Int
        var k: Int
        var id: Int
        var belongNo: Int
        var change: Boolean? = true
        val groupListAdapter = GroupListAdapter(this)

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
                while (k < FragmentGroup.listAdp.count) {
                    val listItem = FragmentGroup.groupList[k]
                    val groupName = listItem.group
                    if (beforeBelong!![i] == groupName) {
                        id = listItem.id
                        belongNo = listItem.belongNo - 1
                        groupListAdapter.updateBelongNo(id.toString(),belongNo)
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
                groupListAdapter.open()
                k = 0
                while (k < FragmentGroup.listAdp.count) {
                    val listItem = FragmentGroup.groupList[k]
                    val groupName = listItem.group
                    if (afterBelong!![i] == groupName) {
                        id = listItem.id
                        belongNo = listItem.belongNo + 1
                        groupListAdapter.updateBelongNo(id.toString(),belongNo)
                    }
                    k++
                }
                groupListAdapter.close()
            }
            i++
        }
    }

    fun getValue(toText: EditText): Int {
        val text = toText.text.toString()
        var a = 0
        if (text.isNotEmpty()) {
            a = Integer.parseInt(text)
        }
        return a
    }

    private fun belongConvertToNo(): String {
        val belongText = belongSpinner!!.text.toString()
        val belongTextArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val belongNo = StringBuilder()

        for (belongGroup in belongTextArray) {
            for (listItem in FragmentGroup.groupList) {
                if (belongGroup == listItem.group) {
                    val listId = listItem.id.toString()
                    belongNo.append("$listId,")
                }
            }
        }
        return belongNo.toString()
    }

    companion object {
        const val POSITION = "position"
        const val FROM_NORMAL_MODE = "fromNormalMode"
    }
}







