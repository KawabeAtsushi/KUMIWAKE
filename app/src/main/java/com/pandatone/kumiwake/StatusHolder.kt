package com.pandatone.kumiwake

import androidx.annotation.StyleRes
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter

object StatusHolder {

    var normalMode: Boolean = true //true: ノーマルモード, false: クイックモード
    var sekigime: Boolean = false //true: 席決めモード, false: 組み分けモード

    @StyleRes
    var nowTheme: Int = R.style.AppTheme

    var mbNowSort = MemberAdapter.MB_ID //ソート対象要素
    var mbSortType = "ASC" //降順・昇順
    var gpNowSort = GroupAdapter.GP_ID //ソート対象要素
    var gpSortType = "ASC" //降順・昇順

    //Special Sex
    var none = "none"
    var index = "index"
}

//AddMemberのintent key
enum class AddMemberKeys(val key:String){
    MEMBER("tableNo"),
    FROM_NORMAL_MODE("fromNormalMode")
}

//AddGroupのintent key
enum class AddGroupKeys(val key: String){
    EDIT_ID("edit_id"),
    MEMBER_ARRAY("memberArray")
}

//組み分け条件のkey
enum class KumiwakeCustomKeys(val key: String){
    EVEN_FM_RATIO("even_fm_ratio"),
    EVEN_AGE_RATIO("even_age_ratio")
}

//kumiwake arrayのintent key
enum class KumiwakeArrayKeys(val key:String){
    MEMBER_LIST("MemberList"),
    GROUP_LIST("GroupList"),
    LEADER_LIST("LEADER_LIST")
}