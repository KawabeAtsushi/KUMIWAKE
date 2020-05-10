package com.pandatone.kumiwake

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member

object FirebaseAnalyticsEvents {
    //@onCreate()
    //FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    lateinit var firebaseAnalytics: FirebaseAnalytics

    //どの機能がよく使われているか？
    fun functionSelectEvent(func: String, mode: String) {
        val bundle = Bundle()
        bundle.putString("function", func)
        bundle.putString("mode", mode)
        firebaseAnalytics.logEvent("function_select", bundle)
    }

    //メンバー数・グループ数
    fun countEvent(memberNo:Int,groupNo:Int,func: String, mode: String) {
        val bundle = Bundle()
        bundle.putString("member_no", memberNo.toString())
        bundle.putString("group_no", groupNo.toString())
        bundle.putString("function", func)
        bundle.putString("mode", mode)
        firebaseAnalytics.logEvent("number_define", bundle)
    }

    //メンバー情報
    fun memberRegisterEvent(member: Member) {
        val bundle = Bundle()
        bundle.putString("id", member.id.toString())
        bundle.putString("name", member.name)
        bundle.putString("sex", member.sex)
        bundle.putString("age", member.age.toString())
        firebaseAnalytics.logEvent("new_member", bundle)
    }

    //グループ情報
    fun groupRegisterEvent(groupName:String,belongNo:Int) {
        val bundle = Bundle()
        bundle.putString("name", groupName)
        bundle.putString("belong_no", belongNo.toString())
        firebaseAnalytics.logEvent("new_group", bundle)
    }
}