package com.pandatone.kumiwake.kumiwake.function

import android.app.Activity
import com.pandatone.kumiwake.R

object KumiwakeMethods {



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
}