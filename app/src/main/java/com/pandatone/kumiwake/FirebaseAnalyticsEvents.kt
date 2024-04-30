package com.pandatone.kumiwake

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.pandatone.kumiwake.member.function.Member

object FirebaseAnalyticsEvents {

    //機能選択のキー
    enum class FunctionKeys(val key: String) {
        KumiwakeNormal("KumiwakeNormal"),
        KumiwakeQuick("KumiwakeQuick"),
        KumiwakeHistory("KumiwakeHistory"),
        SekigimeNormal("SekigimeNormal"),
        SekigimeQuick("SekigimeQuick"),
        SekigimeHistory("SekigimeHistory"),
        History("History"),
        Order("Order"),
        Role("Role"),
        Drawing("Drawing"),
        Classroom("Classroom"),
        OrderHistory("OrderHistory"),
        RoleHistory("RoleHistory"),
        ClassroomHistory("ClassroomHistory")
    }

    //@onCreate()
    //FirebaseAnalyticsEvents.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    lateinit var firebaseAnalytics: FirebaseAnalytics

    //どの機能がよく使われているか？
    fun functionSelectEvent(func: String) {
        val bundle = Bundle()
        bundle.putString("function", func)
        firebaseAnalytics.logEvent("function_select", bundle)
    }

    //メンバー数・グループ数
    fun countEvent(memberNo: Int, groupNo: Int, func: String) {
        val bundle = Bundle()
        bundle.putString("member_no", memberNo.toString())
        bundle.putString("group_no", groupNo.toString())
        bundle.putString("function", func)
        firebaseAnalytics.logEvent("number_define", bundle)
    }

    //グループ・座席名
    fun groupCreateEvent(groupName: String, seatName: String) {
        val bundle = Bundle()
        bundle.putString("group_name", groupName)
        bundle.putString("seat_name", seatName)
        firebaseAnalytics.logEvent("group_create", bundle)
    }

    //メンバー登録イベント
    fun memberRegisterEvent(member: Member) {
        val bundle = Bundle()
        bundle.putString("id", member.id.toString())
        bundle.putString("name", member.name)
        bundle.putString("sex", member.sex)
        bundle.putString("age", member.age.toString())
        firebaseAnalytics.logEvent("new_member", bundle)
    }

    //グループ登録イベント
    fun groupRegisterEvent(groupName: String, belongNo: Int) {
        val bundle = Bundle()
        bundle.putString("name", groupName)
        bundle.putString("belong_no", belongNo.toString())
        firebaseAnalytics.logEvent("new_group", bundle)
    }

    //役割
    fun roleNames(role: String) {
        val bundle = Bundle()
        bundle.putString("role", role)
        firebaseAnalytics.logEvent("role_name", bundle)
    }

    //くじ
    fun ticketNames(ticket: String) {
        val bundle = Bundle()
        bundle.putString("ticket", ticket)
        firebaseAnalytics.logEvent("ticket_name", bundle)
    }

    //広告視聴(応援)
    fun support(supported: String) {
        val bundle = Bundle()
        bundle.putString("supported", supported)
        firebaseAnalytics.logEvent("supported", bundle)
    }
}