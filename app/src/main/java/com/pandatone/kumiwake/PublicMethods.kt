package com.pandatone.kumiwake

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.member.function.Group
import android.graphics.Bitmap
import android.view.View
import java.io.ByteArrayOutputStream


object PublicMethods {

    //ステータスバーの色変更
    fun setStatusBarColor(activity: Activity, @ColorRes colorId: Int) {
        activity.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, colorId)
        }
        when (colorId) {
            Theme.Kumiwake.primaryColor -> StatusHolder.nowTheme = R.style.AppTheme
            Theme.Sekigime.primaryColor -> StatusHolder.nowTheme = R.style.SekigimeTheme
            Theme.Member.primaryColor -> StatusHolder.nowTheme = R.style.MemberTheme
        }
    }

    //Kumiwake 初期グループ生成（人数均等）
    fun initialGroupArray(context: Context, groupNo: Int, memberNo: Int): ArrayList<Group> {
        val groupArray = ArrayList<Group>()
        val eachMemberNo = memberNo / groupNo
        val remain = memberNo % groupNo
        for (i in 0 until remain) {
            groupArray.add(Group(i, context.getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo + 1))
        }
        for (i in remain until groupNo) {
            groupArray.add(Group(i, context.getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo))
        }
        return groupArray
    }

}










