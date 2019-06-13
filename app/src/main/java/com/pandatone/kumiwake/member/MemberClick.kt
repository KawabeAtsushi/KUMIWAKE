package com.pandatone.kumiwake.member

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2016/04/17.
 */
object MemberClick {
    internal var name: TextView
    internal var sex: TextView
    internal var age: TextView
    internal var grade: TextView
    internal var belong: TextView
    internal var role: TextView
    internal var okBt: Button
    protected val context: Context?
        get() = MemberMain.context


    fun MemberInfoDialog(view: View, builder: AlertDialog.Builder) {

        name = view.findViewById<View>(R.id.infoName) as TextView
        sex = view.findViewById<View>(R.id.infoSex) as TextView
        age = view.findViewById<View>(R.id.infoAge) as TextView
        grade = view.findViewById<View>(R.id.infoGrade) as TextView
        belong = view.findViewById<View>(R.id.infoBelong) as TextView
        role = view.findViewById<View>(R.id.infoRole) as TextView
        okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.information)
        builder.setView(view)

    }

    fun SetInfo(position: Int) {
        name.text = context!!.getText(R.string.member_name).toString() + " : " + FragmentMember.nameList[position].name + " (" + FragmentMember.nameList[position].name_read + ")"
        sex.text = context!!.getText(R.string.sex).toString() + " : " + FragmentMember.nameList[position].sex
        age.text = context!!.getText(R.string.age).toString() + " : " + FragmentMember.nameList[position].age.toString()
        grade.text = context!!.getText(R.string.grade).toString() + " : " + FragmentMember.nameList[position].grade.toString()

        if (ViewBelong(position) !== "") {
            belong.text = context!!.getText(R.string.belong).toString() + " : " + ViewBelong(position)
        } else {
            belong.text = context!!.getText(R.string.belong).toString() + " : " + context!!.getText(R.string.nothing)
        }

        if (FragmentMember.nameList[position].role != "") {
            role.text = context!!.getText(R.string.role).toString() + " : " + FragmentMember.nameList[position].role
        } else {
            role.text = context!!.getText(R.string.role).toString() + " : " + context!!.getText(R.string.nothing)
        }

    }

    fun ViewBelong(position: Int): String {
        val result: String

        FragmentMember.dbAdapter.open()
        val belongText = FragmentMember.nameList[position].belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val newBelong = StringBuilder()

        for (i in belongArray.indices) {
            val belongGroup = belongArray[i]
            for (j in 0 until FragmentGroup.ListCount) {
                val listItem = FragmentGroup.nameList[j]
                val groupId = listItem.id.toString()
                if (belongGroup == groupId) {
                    val listName = listItem.group
                    newBelong.append("$listName,")
                }
            }
        }
        FragmentMember.dbAdapter.close()
        if (newBelong.toString() == "") {
            result = ""
        } else {
            result = newBelong.substring(0, newBelong.length - 1)
        }

        return result
    }
}



