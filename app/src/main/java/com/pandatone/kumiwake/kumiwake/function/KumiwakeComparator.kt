package com.pandatone.kumiwake.kumiwake.function

import com.pandatone.kumiwake.member.function.Member
import java.text.Collator

object KumiwakeComparator {

    enum class SortType(val priority: Int) {
        DEFAULT(0),
        SEX(1),
        NAME(3),
        AGE(2)
    }

    // ソート（リーダ→性別→年齢→名前→ID）
    internal class ViewComparator(private val sortType: SortType) : Comparator<Member> {
        override fun compare(n1: Member, n2: Member): Int {
            var value = 0
            if (sortType.priority <= 0) {
                value = comparedValue(n1.leader, n2.leader)
            }
            if (value == 0 && sortType.priority <= 1) {
                value = compareValues(n2.sex, n1.sex)
            }
            if (value == 0 && sortType.priority <= 2) {
                value = compareValues(n2.age, n1.age)
            }
            if (value == 0 && sortType.priority <= 3) {
                value = compareValues(n1.read, n2.read)
            }
            if (value == 0) {
                value = compareValues(n1.id, n2.id)
            }
            return value
        }
    }

    internal class AgeComparator : Comparator<Member> {
        override fun compare(n1: Member, n2: Member): Int {
            return compareValues(n2.age, n1.age)
        }
    }

    //ジェネリクス（宣言時に引数の型定義ができるもの。引数の型推定もできる）
    fun <T> comparedValue(n1: T, n2: T): Int {
        var value = 0
        if (n1 is String && n2 is String) {
            //n1が大きい場合には正の値、小さければ負の値
            val collator = Collator.getInstance()
            value = collator.compare(n1, n2 as String)
        } else if (n1 is Int && n2 is Int) {
            value = when {
                //負の場合は必ず後ろにする
                n1 < 0 && n2 >= 0 -> 1
                n1 >= 0 && n2 < 0 -> -1
                n1 > n2 -> 1
                n1 < n2 -> -1
                else -> 0
            }
        }
        return value
    }
}