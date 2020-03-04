package com.pandatone.kumiwake

import android.app.Activity
import android.view.WindowManager
import android.widget.Switch
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object PublicMethods {

    //ステータスバーの色変更
    fun setStatusBarColor(activity: Activity, @ColorRes colorId: Int) {
        activity.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, colorId)
        }
        when(colorId){
            Theme.Kumiwake.primaryColor -> StatusHolder.nowTheme = R.style.AppTheme
            Theme.Sekigime.primaryColor -> StatusHolder.nowTheme = R.style.SekigimeTheme
            Theme.Member.primaryColor -> StatusHolder.nowTheme = R.style.MemberTheme
        }
    }

}