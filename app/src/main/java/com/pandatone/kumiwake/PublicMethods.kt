package com.pandatone.kumiwake

import android.app.Activity
import android.view.WindowManager
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
    }

    fun setKumiwakeTheme(activity: Activity, @ColorRes colorId: Int) {
        setStatusBarColor(activity,colorId)
    }

}