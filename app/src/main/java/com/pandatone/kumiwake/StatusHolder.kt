package com.pandatone.kumiwake

import androidx.annotation.StyleRes
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter

object StatusHolder {

    //ad status
    var adDeleted = false //広告は非表示か
    var adCheck = true //広告削除購入済みかチェック
    var checkStatus = false //購入履歴チェックモード

    //mode
    var normalMode: Boolean = true //true: ノーマルモード, false: クイックモード
    var mode: String = ModeKeys.Kumiwake.key
    var notDuplicate: Boolean = false //true: 重複なし, false: 普通

    @StyleRes
    var nowTheme: Int = R.style.AppTheme

    //Sort status
    var mbNowSort = MemberAdapter.MB_ID //ソート対象要素
    var mbSortType = "ASC" //降順・昇順
    var gpNowSort = GroupAdapter.GP_ID //ソート対象要素
    var gpSortType = "ASC" //降順・昇順

    //Special Sex
    var none = "none"
    var index = "index"

    //Purchase sku
    const val ad_free_sku = "ad_free_kumiwake"
}

//機能選択のキー
enum class ModeKeys(val key: String) {
    Kumiwake("Kumiwake"),
    Sekigime("Sekigime"),
    History("History"),
    Order("Order"),
    Role("Role"),
    Drawing("Drawing"),
    Classroom("Classroom")
}

//AddMemberのintent key
enum class AddMemberKeys(val key: String) {
    MEMBER("tableNo"),
    FROM_MODE("fromMode")
}

//AddGroupのintent key
enum class AddGroupKeys(val key: String) {
    EDIT_ID("edit_id"),
    MEMBER_ARRAY("memberArray")
}

//組み分け条件のkey
enum class KumiwakeCustomKeys(val key: String) {
    EVEN_FM_RATIO("even_fm_ratio"),
    EVEN_AGE_RATIO("even_age_ratio")
}

//クラスルーム条件のkey
enum class ClassroomCustomKeys(val key: String) {
    ALTER_FM_SEAT("alter_fm_seat"),
    ATTACH_SEAT("attach_seats")
}

//kumiwake arrayのintent key
enum class KumiwakeArrayKeys(val key: String) {
    MEMBER_LIST("MemberList"),
    GROUP_LIST("GroupList"),
    LEADER_LIST("LEADER_LIST")
}