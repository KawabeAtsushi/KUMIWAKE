package com.pandatone.kumiwake.kumiwake.function

import com.pandatone.kumiwake.member.function.Member
import java.util.Comparator

object KumiwakeComparator {

    // ソート（性別→年齢→ID）
    internal class ViewComparator : Comparator<Member> {
        override fun compare(n1: Member, n2: Member): Int {
            var value = comparedValue(n2.role, n1.role)
            if (value == 0) {
                value = compareValues(n2.sex, n1.sex)
            }
            if (value == 0) {
                value = compareValues(n2.age, n1.age)
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

    internal class LeaderComparator : Comparator<Member> {
        override fun compare(n1: Member, n2: Member): Int {
            return comparedValue(n1.role, n2.role)
        }
    }

    //ジェネリクス（宣言時に引数の型定義ができるもの。引数の型推定もできる）
    fun <T> comparedValue(n1: T, n2: T): Int {
        var value = 0
        if (n1 is String && n2 is String) {
            //n1が大きい場合には正の値、小さければ負の値
            value = n1.compareTo(n2)
        } else if (n1 is Int && n2 is Int) {
            value = when {
                n1 > n2 -> 1
                n1 < n2 -> -1
                else -> 0
            }
        }
        return value
    }
}