package com.pandatone.kumiwake.member.function

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ui.members.FragmentGroupMain

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
        okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)

    }

    @SuppressLint("SetTextI18n")
    fun setInfo(c: Context, member: Member) {
        name.text = member.name + " (" + member.read + ")"
        sex.text = member.sex
        age.text = member.age.toString()

        if (viewBelong(member) !== "") {
            belong.text = viewBelong(member)
        } else {
            belong.text = c.getText(R.string.nothing)
        }

    }

    fun viewBelong(member: Member): String {
        val result: String
        val belongText = member.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newBelong = StringBuilder()

        for (belongGroup in belongArray) {
            for (group in FragmentGroupMain.groupList) {
                val groupId = group.id.toString()
                if (belongGroup == groupId) {
                    val listName = group.name
                    newBelong.append("$listName,")
                }
            }
        }
        result = if (newBelong.toString() == "") {
            ""
        } else {
            newBelong.substring(0, newBelong.length - 1)
        }

        return result
    }
}



