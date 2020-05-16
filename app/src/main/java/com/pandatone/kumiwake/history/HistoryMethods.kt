package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Member
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object HistoryMethods {

    private fun resultArrayToString(resultArray: ArrayList<ArrayList<Member>>): String {
        //",/"がグループ区切りの合図
        var resultString = ","
        resultArray.forEach { group ->
            resultString += "/"
            group.forEach { member ->
                resultString += (member.id.toString() + ",")
            }
        }
        return resultString
    }

    fun stringToResultArray(context: Context, resultStr: String): ArrayList<ArrayList<Member>> {
        val groups = resultStr.split(",/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().toCollection(ArrayList())
        groups.removeAll { it.count() == 0 } //空配列を削除
        val resultArray = ArrayList<ArrayList<Member>>()
        val memberList = MemberAdapter(context).getAllMembers()
        groups.forEach { group ->
            val resultNoArray = group.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val result = ArrayList<Member>()
            resultNoArray.forEach { id ->
                result.add(pickMember(context, id, memberList))
            }
            resultArray.add(result)
        }
        return resultArray
    }

    private fun pickMember(context: Context, id: String, memberList: ArrayList<Member>): Member {
        val noMember = Member(-1, context.getString(R.string.deleted_mamber), "none", -1, "", "", -1)
        val member = memberList.find { it.id.toString() == id }
        member?.let { it ->
            // memberがnullでないときだけ実行
            return it
        } ?: run {
            // memberがnullのときだけ実行
            return noMember
        }
    }

    fun saveResultToHistory(context: Context, resultArray: ArrayList<ArrayList<Member>>, mode: Int, again: Boolean) {
        val hsAdapter = HistoryAdapter(context)
        val resultStr = resultArrayToString(resultArray)
        if (!again) {
            hsAdapter.saveHistory(resultStr, mode, 0)
        } else {
            hsAdapter.changeHistory(resultStr, mode, 0)
        }
    }

    //日付の表記を適切にローカライズ
    fun changeDateFormat(dateStr: String): String {
        var name = dateStr
        if (dateStr.length == 19) { //タイムスタンプなら
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dt = df.parse(dateStr)
            val locale = Locale.getDefault()
            val format = DateFormat.getBestDateTimePattern(locale, "yyyyMMMdHHm")
            val dateFormat = SimpleDateFormat(format, locale)
            name = dateFormat.format(dt)
        }
        return name
    }

    //ソート
    var sortType = "ASC"
    fun historySort(activity: Activity, historyList: ArrayList<History>, listAdp: HistoryFragmentViewAdapter) {
        val hsAdapter = HistoryAdapter(activity)
        if (sortType == "ASC") {
            sortType = "DESC"
        } else {
            sortType = "ASC"
        }
        hsAdapter.sortGroups(sortType, historyList)
        listAdp.notifyDataSetChanged() //loadName()を呼ばない！
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///  ForKumiwake  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //履歴の結果とかぶらないようにする
    lateinit var historyResultArray: ArrayList<ArrayList<Member>>

    //重複解除処理
    fun ArrayList<ArrayList<Member>>.avoidDuplicate(memberNo: Float) {
        //resultArrayの(メンバー,元のグループ番号)のペア
        var oldGroups = getOldGroups(this)
        //グループごとのかぶりの数　例：[0, 2, 1] →　履歴のグループ２からは二人いる
        var duplicateNo = getDuplicatedNos(oldGroups)
        //グループごとの必要最低数　例：[1, 1, 1] →　履歴のそれぞれのグループから一人ずつ必要
        val needNoArray = needNos(this, memberNo)
        //グループごとの余分にいる数　例：[1, -1, 0] →　履歴のグループ１は１人抜けても大丈夫。逆にグループ２は一人足りない。
        var extraMemberCount = arraySubtraction(duplicateNo, needNoArray)

        this.forEach { arrayList ->
            Log.d("Group",arrayList.map{it.name}.toString())
        }

        extraMemberCount.forEach {
            Log.d("EXTRA", it.toString())
        }

        for (oldGroupNo in 0 until historyResultArray.size) {
            val extraNoArray = ArrayList<Int>()
            for (i in 0 until extraMemberCount.size) {
                val extraNo = extraMemberCount[i][oldGroupNo]
                extraNoArray.add(extraNo)
            }
            var min = extraNoArray.min()
            while (min!! < 0) {
                Log.d("-", "----------------------------------------------------------")
                Log.d("min", min.toString())
                extraNoArray.let { it ->
                    val minIndex = it.indexOf(min?.toInt())
                    val maxIndex = it.indexOf(it.max())
                    extraMemberCount[minIndex].let { minExtraList ->
                        //足りないグループの一番余っているグループナンバー
                        val minIndexExtraNo = minExtraList.indexOf(minExtraList.max())
                        //Swap
                        Log.d("minIndex", minIndex.toString())
                        Log.d("maxIndex", maxIndex.toString())
                        Log.d("minIndexExtraNo", minIndexExtraNo.toString())
                        this.swapMaxAndMin(oldGroupNo, minIndex, maxIndex, oldGroups, minIndexExtraNo)
                    }
                }
                oldGroups = getOldGroups(this)
                duplicateNo = getDuplicatedNos(oldGroups)
                extraMemberCount = arraySubtraction(duplicateNo, needNoArray)
                extraNoArray.clear()
                for (i in 0 until extraMemberCount.size) {
                    val extraNo = extraMemberCount[i][oldGroupNo]
                    extraNoArray.add(extraNo)
                }
                this.forEach { arrayList ->
                    Log.d("Group",arrayList.map{it.name}.toString())
                }

                extraMemberCount.forEach {
                    Log.d("EXTRA", it.toString())
                }
                min = extraNoArray.min()
            }
        }
    }

    //足りないところに補充するようにメンバーを入れ替え
    private fun ArrayList<ArrayList<Member>>.swapMaxAndMin(oldGroupNo: Int, minIndex: Int, maxIndex: Int, oldGroups: ArrayList<ArrayList<Pair<Member, Int>>>, extraGroupNo: Int) {
        val maxSwapMember = oldGroups[maxIndex].find { it.second == oldGroupNo }?.first
        //余っているやつと交換
        val minSwapMember = oldGroups[minIndex].find { it.second == extraGroupNo }?.first
        Log.d("minSwapMember", minSwapMember!!.name)
        Log.d("maxSwapMember", maxSwapMember!!.name)
        this.swap(minSwapMember!!, maxSwapMember!!)
    }

    //array同士の減算
    private fun arraySubtraction(before: ArrayList<ArrayList<Int>>, after: ArrayList<ArrayList<Int>>): ArrayList<ArrayList<Int>> {
        val needNoArray = ArrayList(before)
        for (i in 0 until before.size) {
            for (j in 0 until before[i].size) {
                needNoArray[i][j] = before[i][j] - after[i][j]
            }
        }
        return needNoArray
    }

    //メンバーの入れ替え Extension
    private fun ArrayList<ArrayList<Member>>.swap(mem1: Member, mem2: Member) {
        var mem1Index = 0
        var mem2Index = 0
        run loop@{
            for ((i, group) in this.withIndex()) {
                if (group.contains(mem1)) {
                    mem1Index = i
                }
                if (group.contains(mem2)) {
                    mem2Index = i
                }
            }
        }
        this[mem1Index].remove(mem1)
        this[mem1Index].add(mem2)
        this[mem2Index].remove(mem2)
        this[mem2Index].add(mem1)
    }

    //元グループの番号ごとのかぶり数を抽出　(Indexが元グループの番号)
    private fun getDuplicatedNos(historyNoArray: ArrayList<ArrayList<Pair<Member, Int>>>): ArrayList<ArrayList<Int>> {
        val notDuplicatedGroups: ArrayList<ArrayList<Int>> = ArrayList()
        historyNoArray.forEach { list ->
            val mDupNoArr: ArrayList<Int> = ArrayList()
            for (i in 0 until historyResultArray.size) {
                mDupNoArr.add(list.count { it.second == i })
            }
            notDuplicatedGroups.add(mDupNoArr)
        }
        return notDuplicatedGroups
    }

    //(メンバー,元のグループ番号のペア)
    private fun getOldGroups(resultArray: ArrayList<ArrayList<Member>>): ArrayList<ArrayList<Pair<Member, Int>>> {
        val historyNoArray: ArrayList<ArrayList<Pair<Member, Int>>> = ArrayList()
        resultArray.forEach { list ->
            val mHisNoArr: ArrayList<Pair<Member, Int>> = ArrayList()
            list.forEach {
                for ((i, arr) in historyResultArray.withIndex()) {
                    if (arr.contains(it)) {
                        mHisNoArr.add(Pair(it, i))
                        break
                    }
                }
            }
            historyNoArray.add(mHisNoArr)
        }
        return historyNoArray
    }

    //最低でも割り当てるべき人数
    private fun needNos(resultArray: ArrayList<ArrayList<Member>>, memberNo: Float): ArrayList<ArrayList<Int>> {
        val needNoArray: ArrayList<ArrayList<Int>> = ArrayList()
        resultArray.forEach { list ->
            val needNoRow: ArrayList<Int> = ArrayList()
            val ratio = list.size / memberNo
            historyResultArray.forEach {
                needNoRow.add((it.size * ratio).toInt())
            }
            needNoArray.add(needNoRow)
        }
        return needNoArray
    }
}

//leaderでない　→　かぶりカウント　→　かぶりが一番多い人　→　元の所属が最も少ないグループに移動（かぶりが一番多いやつとスワップ）　
// →　かぶりカウント　→　繰り返し