package com.pandatone.kumiwake.member

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R

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

    val context = MyApplication.context


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
    fun setInfo(position: Int) {
        name.text = context!!.getText(R.string.member_name).toString() + " : " + FragmentMember.nameList[position].name + " (" + FragmentMember.nameList[position].read + ")"
        sex.text = context.getText(R.string.sex).toString() + " : " + FragmentMember.nameList[position].sex
        age.text = context.getText(R.string.age).toString() + " : " + FragmentMember.nameList[position].age.toString()

        if (viewBelong(position) !== "") {
            belong.text = context.getText(R.string.belong).toString() + " : " + viewBelong(position)
        } else {
            belong.text = context.getText(R.string.belong).toString() + " : " + context.getText(R.string.nothing)
        }

    }

    fun viewBelong(position: Int): String {
        val result: String

        FragmentMember.dbAdapter.open()
        val belongText = FragmentMember.nameList[position].belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newBelong = StringBuilder()

        for (belongGroup in belongArray) {
            for (listItem in FragmentGroup.groupList) {
                val groupId = listItem.id.toString()
                if (belongGroup == groupId) {
                    val listName = listItem.group
                    newBelong.append("$listName,")
                }
            }
        }
        FragmentMember.dbAdapter.close()
        result = if (newBelong.toString() == "") {
            ""
        } else {
            newBelong.substring(0, newBelong.length - 1)
        }

        return result
    }
}



