package com.pandatone.kumiwake.kumiwake.function

import android.app.Activity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

object KumiwakeMethods {

    //for normal mode
    fun kumiwakeAll(resultArray: ArrayList<ArrayList<Member>>, memberArray: ArrayList<Member>, groupArray: ArrayList<Group>, leaderArray: ArrayList<Member>, leaderNoList: Array<Int?>) {
        setLeader(resultArray, leaderArray, leaderNoList) //リーダを先にセット

        val groupCount = groupArray.size
        val escapeArray = ArrayList<Member>() //復帰用

        for (i in 0 until groupCount) {
            val addNo = groupArray[i].belongNo - resultArray[i].size  //追加する人数＝グループの規定人数－グループの現在数
            resultArray[i].addAll(kumiwakeCreateGroup(memberArray, addNo, escapeArray))
        }
        memberArray.addAll(escapeArray)
    }

    //for quick mode
    fun kumiwakeAllQuick(resultArray: ArrayList<ArrayList<Member>>, memberArray: ArrayList<Member>, groupArray: ArrayList<Group>) {
        val groupCount = groupArray.size
        var memberNo = 0

        for (i in 0 until groupCount) {
            val addNo = groupArray[i].belongNo  //追加する人数
            for (j in 0 until addNo) {
                resultArray[i].add(memberArray[memberNo])
                memberNo++
            }
        }

    }

    //リーダーをresultArrayに追加
    fun setLeader(resultArray: ArrayList<ArrayList<Member>>, leaderArray: ArrayList<Member>, leaderNoList: Array<Int?>) {

        for (leader in leaderArray) {
            val id = leader.id
            resultArray[leaderNoList.indexOf(id)].add(leader)
        }
    }

    //年齢順に並べる
    fun arrangeByAge(array: ArrayList<Member>) {
        array.shuffle()
        Collections.sort(array, KumiwakeComparator.AgeComparator())
    }

    //resultArray[i]を作るメソッド
    private fun kumiwakeCreateGroup(array: ArrayList<Member>, addNo: Int, escapeArray: ArrayList<Member>): ArrayList<Member> {
        val result = ArrayList<Member>()
        val addIndexes = properAgeIndexes(addNo, array.size)

        for (i in 0 until addNo) {
            val member = array[addIndexes[i]]
            result.add(member)
        }
        //追加したメンバー削除 & escape
        escapeArray.addAll(result)
        array.removeAll(result)
        return result
    }

    /*
    * 均等化のアルゴリズム
    * 1.グループの人数比によって予想所属数を計算
    * 2.予想所属数が多いグループから順に配属していく。その際、配属可能数を-１する。
    * 3.リーダーの分を先にカウント
    * 4.targetArrayが空になるまで繰り返す
    * （最終周は小数点以下の大きさで決まる。それまでは整数部の大きさで決まる。）
     */
    //manArrayが均等に配分されるようにresultArrayに追加
    fun evenManDistribute(memberCount: Int, resultArray: ArrayList<ArrayList<Member>>, manArray: ArrayList<Member>, groupArray: ArrayList<Group>, man: String) {
        val groupCount = groupArray.size
        val groupCapacity = DoubleArray(groupCount)
        val escapeArray = ArrayList<Member>() //復帰用

        //配属予想数配列を作成
        for (i in 0 until groupCount) {
            groupCapacity[i] = groupArray[i].belongNo.toDouble() * (manArray.size / memberCount.toDouble()) //許容数 = グループの人数 × 男の割合
        }

        //leaderの男分を先にひく
        for (i in 0 until groupCount) {
            if (resultArray[i].isNotEmpty() && resultArray[i][0].sex == man) {
                groupCapacity[i]--
            }
        }

        //順番に追加していく
        while (0 < manArray.size) {
            val addGroupNo = groupCapacity.max()?.let { groupCapacity.indexOf(it) } //最大許容数のGroupNoを取得
            val member = manArray[0]
            resultArray[addGroupNo!!].add(member) //メンバー追加
            escapeArray.add(member)
            manArray.remove(member)
            groupCapacity[addGroupNo]--
        }

        manArray.addAll(escapeArray)
    }

    //womanArrayを残り全てのresultArrayに追加
    fun evenWomanDistribute(resultArray: ArrayList<ArrayList<Member>>, womanArray: ArrayList<Member>, groupArray: ArrayList<Group>) {
        val groupCount = groupArray.size
        val escapeArray = ArrayList<Member>() //復帰用

        var memberSum = 0
        for (i in 0 until groupCount) {
            val addNo = groupArray[i].belongNo - resultArray[i].size  //追加する人数＝グループの規定人数－グループの現在数
            resultArray[i].addAll(kumiwakeCreateGroup(womanArray, addNo, escapeArray))
            memberSum += addNo //既に追加した人数
        }
        womanArray.addAll(escapeArray)
    }

    //元配列を飛ばし飛ばしでとって、そのIndex配列を返す -> 年齢が均等にとれる
    private fun properAgeIndexes(toArraySize: Int, fromArraySize: Int): IntArray {
        val pickIndexes = IntArray(toArraySize)
        if (toArraySize != 0) {
            val interval = fromArraySize / toArraySize
            val asc = Random.nextBoolean()
            for (i in 0 until toArraySize) {
                if (asc) {
                    pickIndexes[i] = interval * i
                } else {
                    pickIndexes[i] = fromArraySize - 1 - interval * i
                }
            }
        }
        return pickIndexes
    }

    //結果共有方法選択       ?関数型: () -> T を持ちます。つまり、パラメータを取らず、型 T の値を返す関数
    fun shareResult(activity: Activity, choice1: () -> Unit, choice2: () -> Unit) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        val items = arrayOf(
                activity.getString(R.string.share_with_text),
                activity.getString(R.string.share_with_image))

        builder.setTitle(R.string.share_result)
        var checked = 0
        builder.setSingleChoiceItems(items, checked) { _, which -> checked = which }

        builder.setPositiveButton(activity.getString(R.string.share)) { _, _ ->
            when (checked) {
                0 -> choice1()
                1 -> choice2()
            }
        }
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        // back keyを使用不可に設定
        builder.setCancelable(false)
        builder.show()
    }

    //結果背景の色を生成
    private val colorList = listOf(
            "ffb7b7", "ffb7db", "ffb7ff", "dbb7ff",
            "b7b7ff", "b7dbff", "b7ffff", "b7ffdb",
            "b7ffb7", "dbffb7", "ffffb7", "ffdbb7"
    )

    //組み分け結果背景用に色変換
    fun getResultColorStr(ver: Int, groupCount: Int): String {
        val colorNum = colorList.size.toFloat()
        var skipColBias = 1f
        if (groupCount < colorNum) {
            skipColBias = colorNum / groupCount
        }
        val element = (ver % colorNum) * skipColBias
        return colorList[element.toInt()]
    }
}