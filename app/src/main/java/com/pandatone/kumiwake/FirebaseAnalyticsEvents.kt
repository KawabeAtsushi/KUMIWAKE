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
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, func)
        bundle.putString("mode", mode)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
    }

    //メンバー数・グループ数
    fun countEvent(memberNo:Int,groupNo:Int,func: String, mode: String) {
        val bundle = Bundle()
        bundle.putString("member_no", memberNo.toString())
        bundle.putString("group_no", groupNo.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, func)
        bundle.putString("mode", mode)
        firebaseAnalytics.logEvent("number_count", bundle)
    }

    //メンバー情報
    fun memberRegisterEvent(member: Member) {
        val bundle = Bundle()
        bundle.putString("id", member.id.toString())
        bundle.putString("name", member.name)
        bundle.putString("sex", member.sex)
        bundle.putString("age", member.age.toString())
        firebaseAnalytics.logEvent("registered_member", bundle)
    }

    //グループ情報
    fun groupRegisterEvent(group: Group) {
        val bundle = Bundle()
        bundle.putString("id", group.id.toString())
        bundle.putString("name", group.name)
        bundle.putString("belong_no", group.belongNo.toString())
        firebaseAnalytics.logEvent("registered_group", bundle)
    }
}