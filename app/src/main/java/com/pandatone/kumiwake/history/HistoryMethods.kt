package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object HistoryMethods {

    //結果配列を文字列に
    private fun resultArrayToString(resultArray: ArrayList<ArrayList<Member>>): String {
        //  ",/"がグループ区切りの合図
        var resultString = ","
        resultArray.forEach { group ->
            resultString += "/"
            group.forEach { member ->
                resultString += (member.id.toString() + ",")
            }
        }
        return resultString
    }

    //文字列を結果配列に
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

    //グループ名結果配列を文字列に
    private fun resultGroupArrayToString(groupArray: ArrayList<Group>): String {
        //  ","がグループ区切りの合図
        var resultString = ""
        groupArray.forEach { group ->
            resultString += (group.name + ",")
        }
        return resultString
    }

    //文字列をグループ名結果配列に
    fun stringToResultGroupArray(groupStr: String): Array<String> {
        return groupStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    //メンバーリストからメンバーを検索
    private fun pickMember(context: Context, id: String, memberList: ArrayList<Member>): Member {
        val noMember = Member(-1, context.getString(R.string.deleted_member), "none", -1, "", "", -1)
        val member = memberList.find { it.id.toString() == id }
        member?.let { it ->
            // memberがnullでないときだけ実行
            return it
        } ?: run {
            // memberがnullのときだけ実行
            return noMember
        }
    }

    //kumiwake結果を履歴に保存
    fun saveResultToHistory(context: Context, resultArray: ArrayList<ArrayList<Member>>, groupArray: ArrayList<Group>, mode: Int, again: Boolean) {
        val hsAdapter = HistoryAdapter(context)
        val resultStr = resultArrayToString(resultArray)
        val groupStr = resultGroupArrayToString(groupArray)
        if (!again) {
            hsAdapter.saveHistory(resultStr, groupStr, mode, 0)
        } else {
            hsAdapter.changeHistory(resultStr, groupStr, mode, 0)
        }
    }

    //日付の表記を適切にローカライズ
    fun changeDateFormat(dateStr: String): String {
        var name = dateStr
        if (dateStr.length == 19) { //タイムスタンプなら
            try {
                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val dt = df.parse(dateStr)
                val locale = Locale.getDefault()
                val format = DateFormat.getBestDateTimePattern(locale, "yyyyMMMdHHm")
                val dateFormat = SimpleDateFormat(format, locale)
                name = dateFormat.format(dt)
            } catch (e: ParseException) {
                Log.d("Caused ParseException", e.toString())
            }
        }
        return name
    }

    //ソート
    var sortType = "ASC"
    fun historySort(activity: Activity, historyList: ArrayList<History>, listAdp: HistoryFragmentViewAdapter) {
        val hsAdapter = HistoryAdapter(activity)
        sortType = if (sortType == "ASC") {
            "DESC"
        } else {
            "ASC"
        }
        hsAdapter.sortGroups(sortType, historyList)
        listAdp.notifyDataSetChanged() //loadName()を呼ばない！
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///  ForKumiwake  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //履歴の結果とかぶらないようにする
    lateinit var historyResultArray: ArrayList<ArrayList<Member>>

    // 永久ループ防止用
    private var loop = 0

    //重複解除処理 グループごとに足りない数を補充する形でメンバー入れ替え
    fun ArrayList<ArrayList<Member>>.avoidDuplicate(memberNo: Float) {
        //resultArrayの(メンバー,元のグループ番号)のペア
        var oldGroups = getOldGroups(this)
        //グループごとのかぶりの数　例：[0, 2, 1] →　履歴のグループ２からは二人いる
        var duplicateNo = getDuplicatedNos(oldGroups)
        //グループごとの必要最低数　例：[1, 1, 1] →　履歴のそれぞれのグループから一人ずつ必要 ((一定))
        val needNoArray = needNos(this, memberNo)
        //グループごとの余分にいる数　例：[1, -1, 0] →　履歴のグループ１は１人抜けても大丈夫。逆にグループ２は一人足りない。
        var extraMemberCount = arraySubtraction(duplicateNo, needNoArray)

        var min = 0
        var max = 0
        val extraNoArray = ArrayList<Int>()

        //不足メンバー数集計
        fun checkExtras(oldGroupNo: Int) {
            extraNoArray.clear()
            for (i in extraMemberCount.indices) {
                val extraNo = extraMemberCount[i][oldGroupNo]
                extraNoArray.add(extraNo)
            }
            min = extraNoArray.minOrNull()!!
            max = extraNoArray.maxOrNull()!!
        }

        for (oldGroupNo in historyResultArray.indices) {
            checkExtras(oldGroupNo)
            while (min < 0 || max - min >= 2) {
                extraNoArray.let {
                    val minIndex = it.indexOf(min)
                    val maxIndex = it.indexOf(max)
                    extraMemberCount[minIndex].let { minExtraList ->
                        //足りていないグループのなかで一番余っているグループ番号
                        val minIndexExtraNo = minExtraList.indexOf(minExtraList.maxOrNull())
                        //Swap　(1)一番足りているグループ→(2)足りないグループ、　(2)の一番余っているメンバー→(1)
                        this.swapMaxAndMin(oldGroupNo, minIndex, maxIndex, oldGroups, minIndexExtraNo)
                    }
                }

                if (loop > this.size) break

                oldGroups = getOldGroups(this)
                duplicateNo = getDuplicatedNos(oldGroups)
                extraMemberCount = arraySubtraction(duplicateNo, needNoArray)
                //再集計
                checkExtras(oldGroupNo)
            }
        }
    }

    //足りないところに補充するようにメンバーを入れ替え
    private fun ArrayList<ArrayList<Member>>.swapMaxAndMin(oldGroupNo: Int, minIndex: Int, maxIndex: Int, oldGroups: ArrayList<ArrayList<Pair<Member, Int>>>, extraGroupNo: Int) {
        //一番余っているメンバー＆＆リーダーでない
        val maxSwapMember = oldGroups[maxIndex].find { it.second == oldGroupNo && it.first.leader < 0 }?.first
        //一番足りないメンバー＆＆リーダーでない
        val minSwapMember = oldGroups[minIndex].find { it.second == extraGroupNo && it.first.leader < 0 }?.first
        if (minSwapMember != null && maxSwapMember != null) {
            this.swap(minSwapMember, maxSwapMember)
        } else {
            loop++
        }
    }

    //ArrayList同士の減算
    private fun arraySubtraction(before: ArrayList<ArrayList<Int>>, after: ArrayList<ArrayList<Int>>): ArrayList<ArrayList<Int>> {
        val needNoArray = ArrayList(before)
        for (i in before.indices) {
            for (j in before[i].indices) {
                needNoArray[i][j] = before[i][j] - after[i][j]
            }
        }
        return needNoArray
    }

    //メンバーの入れ替え(Extension)
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
            for (i in historyResultArray.indices) {
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

    //行のロングクリックの処理
    fun onLongClick(history: History, activity: Activity, hsAdapter: HistoryAdapter, pageIsKeeps: Boolean) {
        val builder = AlertDialog.Builder(activity)
        val favoriteText = if (history.keep == -1) {
            activity.getText(R.string.add_favorite)
        } else {
            activity.getText(R.string.remove_favorite)
        }
        val items = arrayOf(activity.getText(R.string.edit_title), favoriteText, activity.getText(R.string.delete))
        builder.setTitle(changeDateFormat(history.name))
        builder.setItems(items) { _, which ->
            when (which) {
                0 -> HistoryInfo(activity).editTextDialog(history)
                1 -> {
                    hsAdapter.updateHistoryState(history, "", true)
                    FragmentHistory().loadName()
                    FragmentKeeps().loadName()
                    if (pageIsKeeps) {
                        FragmentKeeps.toolbarTitle = activity.getString(R.string.favorite) + " " + FragmentKeeps.historyList.count().toString() + "♥s"
                        HistoryMain.toolbar.title = FragmentKeeps.toolbarTitle
                    }
                }
                2 -> {
                    deleteHistory(history, activity, hsAdapter, pageIsKeeps)
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //履歴削除
    private fun deleteHistory(history: History, activity: Activity, hsAdapter: HistoryAdapter, pageIsKeeps: Boolean) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(changeDateFormat(history.name))
        builder.setMessage(R.string.Do_delete)
        // OKの時の処理
        builder.setPositiveButton("OK") { _, _ ->
            val listId = history.id
            hsAdapter.selectDelete(listId.toString())
            FragmentHistory().loadName()
            FragmentKeeps().loadName()
            FragmentHistory.toolbarTitle = activity.getString(R.string.history) + " " + FragmentHistory.historyList.count().toString() + "times"
            FragmentKeeps.toolbarTitle = activity.getString(R.string.favorite) + " " + FragmentKeeps.historyList.count().toString() + "♥s"
            if (pageIsKeeps) {
                HistoryMain.toolbar.title = FragmentKeeps.toolbarTitle
            } else {
                HistoryMain.toolbar.title = FragmentHistory.toolbarTitle
            }
        }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }
}