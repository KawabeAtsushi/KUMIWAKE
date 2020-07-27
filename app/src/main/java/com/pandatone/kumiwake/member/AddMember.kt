package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.GroupFragmentViewAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.kumiwake.NormalMode
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.function.MemberClick
import com.pandatone.kumiwake.member.function.MemberMethods
import com.pandatone.kumiwake.member.members.FragmentGroupMain
import com.pandatone.kumiwake.member.members.FragmentMemberMain
import com.pandatone.kumiwake.others.SelectMember
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import kotlinx.android.synthetic.main.add_member.*


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
    private var belongDropdown: AutoCompleteTextView? = null
    private var mbAdapter: MemberAdapter? = null
    private var fromMode: String? = "member"
    private val groupList: ArrayList<Group>
        get() {
            return GroupAdapter(this).getAllGroups()
        }
    private var dialogShown = false
    private lateinit var mDetector: GestureDetectorCompat

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.add_member)
        mbAdapter = MemberAdapter(this)
        FragmentGroupMain.gpAdapter = GroupAdapter(this)
        FragmentGroupMain.listAdp = GroupFragmentViewAdapter(this, groupList)
        beforeBelong = null
        afterBelong = null
        findViews()
        ageEditText!!.setText("")
        val i = intent
        val member = i.getSerializableExtra(AddMemberKeys.MEMBER.key) as Member?
        fromMode = i.getStringExtra(AddMemberKeys.FROM_MODE.key)
        val memberImg = findViewById<ImageView>(R.id.memberIcon)
        sexGroup!!.setOnCheckedChangeListener { _, checkedId: Int ->
            when (checkedId) {
                R.id.manBtn -> memberImg.setColorFilter(PublicMethods.getColor(this, R.color.man))
                R.id.womanBtn -> memberImg.setColorFilter(PublicMethods.getColor(this, R.color.woman))
            }
        }
        if (member != null) {
            switch_continuously_mode.visibility = View.GONE
            setItem(member)
            member_registration_continue_btn.visibility = View.GONE
        }

        var yomigana = ""

        nameEditText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (s.matches("^[a-zA-Z0-9ぁ-ん\\s]+$".toRegex()) || s.toString() == "") {
                    yomigana = s.toString()
                }
                readEditText?.setText(yomigana)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        Toast.makeText(this, getText(R.string.double_tap), Toast.LENGTH_SHORT).show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDetector = GestureDetectorCompat(this, MyGestureListener(imm, nameEditText as EditText))
        mDetector.setOnDoubleTapListener(MyGestureListener(imm, nameEditText as EditText))
    }

    //スクロールビューの場合こっち呼ぶ
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        super.dispatchTouchEvent(event)
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    //Viewの宣言
    private fun findViews() {
        nameEditText = findViewById<View>(R.id.input_name) as TextInputEditText
        readEditText = findViewById<View>(R.id.input_name_read) as TextInputEditText
        textInputLayout = findViewById<View>(R.id.member_form_input_layout) as TextInputLayout
        sexGroup = findViewById<View>(R.id.sexGroup) as RadioGroup
        ageEditText = findViewById<View>(R.id.input_age) as EditText
        belongDropdown = findViewById<View>(R.id.select_group_choicer) as AutoCompleteTextView
        belongDropdown?.let { button -> button.setOnClickListener { onSelectGroupDropdownClicked(button) } }
        findViewById<Button>(R.id.member_registration_finish_btn).setOnClickListener { register(true) }
        findViewById<Button>(R.id.member_registration_continue_btn).setOnClickListener { register(false) }
        findViewById<Button>(R.id.member_cancel_btn).setOnClickListener { finish() }
        findViewById<Button>(R.id.switch_continuously_mode).setOnClickListener {
            val intent = Intent(this, AddMemberInBulk::class.java)
            intent.putExtra(AddMemberKeys.FROM_MODE.key, fromMode)
            finish()
            startActivity(intent)
        }
    }

    //Updateの場合の初期アイテム表示
    private fun setItem(member: Member) {
        val listId = member.id
        val name = member.name
        val read = member.read
        val sex = member.sex
        val age = member.age.toString()
        val belong = MemberClick.viewBelong(member, FragmentGroupMain.groupList)

        nameEditText!!.setText(name)
        readEditText!!.setText(read)
        if (sex == getString(R.string.woman)) {
            sexGroup!!.check(R.id.womanBtn)
        } else {
            sexGroup!!.check(R.id.manBtn)
        }
        ageEditText!!.setText(age)
        belongDropdown!!.setText(belong)

        update(listId)

    }


    @SuppressLint("SetTextI18n")
    internal fun onSelectGroupDropdownClicked(view: View) {
        if (!dialogShown) { //二回呼ばれる問題の解決
            dialogShown = true
            // 選択中の候補を取得
            val buttonText = belongDropdown!!.text.toString()
            val textArray = buttonText.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
            dialog.setMultiChoiceItems(belongArray, checkArray) { _, _, _ -> }
            dialog.setPositiveButton(getText(R.string.decide)) { _, _ ->
                val buffer = StringBuilder()
                for (i in belongArray.indices) {
                    if (checkArray[i]) {
                        buffer.append((if (buffer.isEmpty()) "" else ", ") + belongArray[i])
                    }
                }
                belongDropdown!!.setText(buffer)
                dialogShown = false
            }
            dialog.setNeutralButton(getText(R.string.clear)) { _, _ ->
                belongDropdown!!.setText("")
                dialogShown = false
                // 再表示
                onSelectGroupDropdownClicked(view)
            }
            dialog.setNegativeButton(getText(R.string.cancel)) { _, _ -> dialogShown = false }
            dialog.show()

            beforeBelong = textArray
        }
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
            afterBelong = belongDropdown!!.text.toString().split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            MemberMethods.updateBelongNo(this)
            val newMember = MemberAdapter(this).newMember

            when (fromMode) {
                "normal" -> NormalMode.memberArray.add(newMember)
                "others" -> SelectMember.memberArray.add(newMember)
            }

            Toast.makeText(this, getText(R.string.member).toString() + " \"" + name + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show()
            FirebaseAnalyticsEvents.memberRegisterEvent(newMember)

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
        val belong = MemberMethods.belongConvertToNo(belongDropdown!!.text.toString(), groupList)

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
                afterBelong = belongDropdown!!.text.toString().split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                MemberMethods.updateBelongNo(this)
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
        val belong = MemberMethods.belongConvertToNo(belongDropdown!!.text.toString(), groupList)

        mbAdapter!!.updateMember(listId, name, sex, age, belong, read)
        FragmentMemberMain().loadName()
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

}







