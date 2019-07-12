package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import kotlinx.android.synthetic.main.add_member.*
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
        if (position != -1) {
            setItem(position)
        }
        val animDrawable = addmember_layout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(5000)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()
    }

    private fun findViews() {
        nameEditText = findViewById<View>(R.id.input_name) as AppCompatEditText
        readEditText = findViewById<View>(R.id.input_name_read) as AppCompatEditText
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString().matches("^[a-zA-Z0-9ぁ-ん]+$".toRegex())) {
                source
            } else ""
        }
        val filters = arrayOf(inputFilter)
        readEditText!!.filters = filters
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
        if (sex == "女") {
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
        for (j in 0 until FragmentGroup.ListCount) {
            val listItem = FragmentGroup.nameList[j]
            val groupName = listItem.group

            list.add(groupName)
        }
        val belongArray = list.toTypedArray<String>()
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
        dialog.setMultiChoiceItems(belongArray, checkArray, DialogInterface.OnMultiChoiceClickListener { _, value, isChecked ->
            val text = belongSpinner!!.text.toString()
            // 選択された場合
            if (isChecked) {
                // ボタンの表示に追加
                belongSpinner!!.text = text + (if ("" == text) "" else ",") + belongArray[value]
            } else {
                // ボタンの表示から削除
                if (text.indexOf(belongArray[value] + ",") >= 0) {
                    belongSpinner!!.text = text.replace(belongArray[value] + ",", "")
                } else if (text.indexOf("," + belongArray[value]) >= 0) {
                    belongSpinner!!.text = text.replace("," + belongArray[value], "")
                } else {
                    belongSpinner!!.text = text.replace(belongArray[value], "")
                }
            }
        })
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

    @OnClick(R.id.member_registration_button)
    internal fun onRegistrationMemberClicked() {
        val name = nameEditText!!.text!!.toString()
        if (TextUtils.isEmpty(name)) {
            textInputLayout!!.isErrorEnabled = true
            textInputLayout!!.error = getText(R.string.error_empty_name)
        } else {
            saveItem()
            afterBelong = belongSpinner!!.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            changeBelongNo()
            Toast.makeText(this, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateItem(listId: Int) {
        sexButton = findViewById<View>(sexGroup!!.checkedRadioButtonId) as RadioButton
        val name = nameEditText!!.text!!.toString()
        val read = readEditText!!.text!!.toString()
        val sex = sexButton!!.text as String
        val age = getValue(ageEditText!!)
        val belong = belongConvertToNo()

        dbAdapter!!.updateMember(listId, name, sex, age, 0, belong, "", read)
        FragmentMember().loadName()
    }

    private fun update(listId: Int) {
        val updateBt = findViewById<View>(R.id.member_registration_button) as Button
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

        dbAdapter!!.open()
        dbAdapter!!.saveName(name, sex, age, 0, belong, "", read)
        dbAdapter!!.close()
    }

    private fun changeBelongNo() {
        var i: Int = 0
        var j: Int
        var k: Int
        var id: Int
        var belongNo: Int
        val beforeBelongNo: Int
        val afterBelongNo: Int
        var name: String
        val read = "ￚ no data ￚ"
        var change: Boolean? = true
        val GPdbAdapter = GroupListAdapter(this)

        if (beforeBelong != null) {
            beforeBelongNo = beforeBelong!!.size
        } else {
            beforeBelongNo = 0
        }

        if (afterBelong != null) {
            afterBelongNo = afterBelong!!.size
        } else {
            afterBelongNo = 0
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
                GPdbAdapter.open()
                k = 0
                while (k < FragmentGroup.ListCount) {
                    val listItem = FragmentGroup.nameList[k]
                    val groupName = listItem.group
                    if (beforeBelong!![i] == groupName) {
                        id = listItem.id
                        name = listItem.group
                        belongNo = listItem.belongNo - 1
                        GPdbAdapter.updateGroup(id, name, read, belongNo)
                    }
                    k++
                }
                GPdbAdapter.close()
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
                GPdbAdapter.open()
                k = 0
                while (k < FragmentGroup.ListCount) {
                    val listItem = FragmentGroup.nameList[k]
                    val groupName = listItem.group
                    if (afterBelong!![i] == groupName) {
                        id = listItem.id
                        name = listItem.group
                        belongNo = listItem.belongNo + 1
                        GPdbAdapter.updateGroup(id, name, read, belongNo)
                    }
                    k++
                }
                GPdbAdapter.close()
            }
            i++
        }
    }

    fun getValue(totext: EditText): Int {
        val text = totext.text.toString()
        var a = 0
        if (text.isNotEmpty()) {
            a = Integer.parseInt(text)
        }
        return a
    }

    private fun belongConvertToNo(): String {
        dbAdapter!!.open()
        val belongText = belongSpinner!!.text.toString()
        val belongTextArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val BelongNo = StringBuilder()

        for (i in belongTextArray.indices) {
            val belongGroup = belongTextArray[i]
            for (j in 0 until FragmentGroup.ListCount) {
                val listItem = FragmentGroup.nameList[j]
                val groupName = listItem.group
                if (belongGroup == groupName) {
                    val listName = listItem.id.toString()
                    BelongNo.append("$listName,")
                }
            }
        }
        dbAdapter!!.close()

        return BelongNo.toString()
    }

    companion object {
        const val POSITION = "position"
    }
}







