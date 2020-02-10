package com.pandatone.kumiwake

class StatusHolder {

    companion object {
        var normalMode:Boolean = true //true: ノーマルモード, false: クイックモード
        var sekigime: Boolean = false //true: 席決めモード, false: 組み分けモード
    }

}

//Enum classes

enum class requestCodes(val code:Int){

}

//intent keys for intent array
enum class ArrayKeys(val key:String){
    NORMAL_MEMBER_ARRAY("normal_memberArray"),
    NORMAL_GROUP_ARRAY("normal_groupArray"),
    LEADER_ARRAY("leader_array")
}

//AddMemberのintent key
enum class AddMemberKeys(val key:String){
    MEMBER("position"),
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

//quickModeのintent key
enum class QuickModeKeys(val key:String){
    EVEN_FM_RATIO("even_fm_ratio"),
    MAN_LIST("QuickModeManList"),
    WOMAN_LIST("QuickModeWomanList"),
    GROUP_LIST("QuickModeGroupList"),
    MEMBER_LIST("QuickModeMemberList")
}