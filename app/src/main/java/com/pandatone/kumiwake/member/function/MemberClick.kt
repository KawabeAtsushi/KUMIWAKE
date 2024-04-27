package com.pandatone.kumiwake.member.function

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.members.FragmentGroupMain

/**
 * Created by atsushi_2 on 2016/04/17.
 */
@SuppressLint("StaticFieldLeak")
object MemberClick {
    private lateinit var name: TextView
    private lateinit var sex: TextView
    private lateinit var age: TextView
    private lateinit var belong: TextView
    lateinit var okBt: Button

    fun memberInfoDialog(view: View, builder: AlertDialog.Builder) {

        name = view.findViewById<View>(R.id.infoName) as TextView
        sex = view.findViewById<View>(R.id.infoSex) as TextView
        age = view.findViewById<View>(R.id.infoAge) as TextView
        belong = view.findViewById<View>(R.id.infoBelong) as TextView
        okBt = view.findViewById<View>(R.id.closeBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)

    }

    @SuppressLint("SetTextI18n")
    fun setInfo(c: Context, member: Member) {
        name.text = member.name + " (" + member.read + ")"
        sex.text = member.sex
        age.text = member.age.toString()
        val belongNames = viewBelong(member, FragmentGroupMain.groupList)
        if (belongNames !== "") {
            belong.text = belongNames
        } else {
            belong.text = c.getText(R.string.nothing)
        }

    }

    fun viewBelong(member: Member, groupList: ArrayList<Group>): String {
        val result: String
        val belongText = member.belong
        val belongArray =
            belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // groupListの中からそのidがbelongArrayに含まれているものをコレクションで返す
        val groups = groupList.filter { belongArray.contains(it.id.toString()) }
        // 返されたgroupsのnameを連結した文字列
        result = groups.joinToString(separator = ", ") { it.name }

        return result
    }
}



