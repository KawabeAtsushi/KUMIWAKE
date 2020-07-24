package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Member
import java.util.*

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class AddInBulkViewAdapter(val context: Context, val memberList: List<Member>) : BaseAdapter() {
    private var setFocus = 0
    private var outFocus = 0

    //ListViewのEditTextのマップ
    @SuppressLint("UseSparseArrays")
    private val nameEditTextList = HashMap<Int, EditText>()
    private val ageEditTextList = HashMap<Int, EditText>()
    private val readTextViewList = HashMap<Int, TextView>()
    private val sexIconList = HashMap<Int, ImageView>()

    override fun getCount(): Int {
        return memberList.size
    }

    override fun getItem(position: Int): Any {
        return memberList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var v = convertView
        val member = memberList[position]
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null)
        }

        val nameEditText = v!!.findViewById<View>(R.id.editGroupName) as EditText
        nameEditText.isFocusable = true
        nameEditText.isFocusableInTouchMode = true
        val ageEditText = v.findViewById<View>(R.id.editTheNumberOfMember) as EditText
        ageEditText.isFocusable = true
        ageEditText.isFocusableInTouchMode = true
        nameEditText.setText(member.name)
        ageEditText.setText(member.age.toString())
        val readText = v.findViewById<TextView>(R.id.leader)
        readText.text = member.read
        val icon = v.findViewById<ImageView>(R.id.rowIconGroup)
        icon.setImageResource(R.drawable.ic_human)
        icon.tag = member.sex
        val manStr = context.getString(R.string.man)
        val womanStr = context.getString(R.string.woman)
        when (icon.tag) {
            manStr -> icon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            womanStr -> icon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
        }
        icon.setOnClickListener {
            if (icon.tag == manStr) {
                icon.tag = womanStr
                icon.setColorFilter(PublicMethods.getColor(context, R.color.woman))
            } else {
                icon.tag = manStr
                icon.setColorFilter(PublicMethods.getColor(context, R.color.man))
            }
        }

        v.findViewById<TextView>(R.id.personTex).text = context.getString(R.string.age)

        nameEditTextList[position] = nameEditText
        ageEditTextList[position] = ageEditText
        readTextViewList[position] = readText
        sexIconList[position] = icon

        ageEditText.setOnFocusChangeListener { _, hasFocus ->
            //hasFocus: 入力開始 -> true,移動 -> false
            if (hasFocus) {
                setFocus = position
            } else {
                outFocus = position
            }
            if (setFocus != outFocus) {//別のところにフォーカスが移ったら
                val beforeEditText = ageEditTextList[outFocus]
                if (beforeEditText?.text.toString() == "" || beforeEditText?.text.toString() == "-") {
                    beforeEditText?.setText("0")
                }
            }
        }

        var yomigana = ""
        //読み仮名の自動入力
        nameEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (s.matches("^[a-zA-Z0-9ぁ-ん]+$".toRegex()) || s.toString() == "") {
                    yomigana = s.toString()
                }
                readText.text = yomigana
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        //数の入力変更の際に呼ばれる処理
        ageEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(text: Editable?) {

                if (text.toString() != "" && text.toString() != "-") {
                    val afterNo = Integer.parseInt(text.toString())

                    if (afterNo < 0) {
                        ageEditText.setTextColor(Color.RED)
                    } else {
                        ageEditText.setTextColor(PublicMethods.getColor(context, R.color.gray))
                    }
                }
            }

            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )

        return v
    }

    //i番目の名前を取得
    fun getName(position: Int): String {
        var name = R.string.nothing.toString()
        val nameEditText = nameEditTextList[position]
        if (nameEditText!!.text != null) {
            name = nameEditText.text.toString()
        }
        return name
    }

    //i番目の性別を取得
    fun getSex(position: Int): String {
        val sexIcon = sexIconList[position]
        return sexIcon?.tag.toString()
    }

    //i番目の年齢を取得
    fun getAge(position: Int): Int {
        var age = 0
        val ageEditText = ageEditTextList[position]
        if (ageEditText!!.text.toString().isNotEmpty()) {
            age = Integer.parseInt(ageEditText.text.toString())
        }
        return age
    }

    //i番目の読み仮名を取得
    fun getRead(position: Int): String {
        var read = ""
        val readTextView = readTextViewList[position]
        if (readTextView!!.text != null) {
            read = readTextView.text.toString()
        }
        return read
    }


    fun setRowHeight(listView: ListView) {
        var totalHeight = 0

        for (j in 0 until this.count) {
            val item = this.getView(j, null, listView)
            item?.measure(0, 0)
            totalHeight += item!!.measuredHeight
        }

        listView.layoutParams.height = totalHeight + listView.dividerHeight * (this.count - 1)
        listView.requestLayout()
    }

}

