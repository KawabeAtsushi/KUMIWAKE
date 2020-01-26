package com.pandatone.kumiwake.ui.members

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.member.FragmentGroupChoiceMode
import com.pandatone.kumiwake.member.Name

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
    fun setInfo(c:Context, member: Name, dbAdapter:MemberListAdapter) {
        name.text = c.getText(R.string.member_name).toString() + " : " + member.name + " (" + member.read + ")"
        sex.text = c.getText(R.string.sex).toString() + " : " + member.sex
        age.text = c.getText(R.string.age).toString() + " : " + member.age.toString()

        if (viewBelong(member, dbAdapter) !== "") {
            belong.text = c.getText(R.string.belong).toString() + " : " + viewBelong(member, dbAdapter)
        } else {
            belong.text = c.getText(R.string.belong).toString() + " : " + c.getText(R.string.nothing)
        }

    }

    fun viewBelong(member: Name, dbAdapter:MemberListAdapter): String {
        val result: String

        dbAdapter.open()
        val belongText = member.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newBelong = StringBuilder()

        for (belongGroup in belongArray) {
            for (listItem in FragmentGroupChoiceMode.groupList) {
                val groupId = listItem.id.toString()
                if (belongGroup == groupId) {
                    val listName = listItem.group
                    newBelong.append("$listName,")
                }
            }
        }
        dbAdapter.close()
        result = if (newBelong.toString() == "") {
            ""
        } else {
            newBelong.substring(0, newBelong.length - 1)
        }

        return result
    }
}



