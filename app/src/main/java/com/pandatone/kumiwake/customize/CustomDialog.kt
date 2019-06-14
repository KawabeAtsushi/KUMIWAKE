package com.pandatone.kumiwake.customize

import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup

import java.io.File

/**
 * Created by atsushi_2 on 2016/11/11.
 */

class CustomDialog : DialogFragment() {
    private var mTitle = ""
    private var mMessage: CharSequence = ""

    //onClickリスナ
    private val mOnClickLisner = View.OnClickListener { dismiss() }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val dialog = Dialog(activity)
        // タイトル非表示
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = mTitle
        // メッセージ設定
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = mMessage
        // OK ボタンのリスナ
        if (mPositiveBtnListener == null) {
            dialog.findViewById<View>(R.id.negative_button).visibility = View.GONE
            dialog.findViewById<View>(R.id.positive_button).setOnClickListener(mOnClickLisner)
        } else {
            dialog.findViewById<View>(R.id.positive_button).setOnClickListener(mPositiveBtnListener)
        }

        // いいえボタンのリスナ
        dialog.findViewById<View>(R.id.negative_button).setOnClickListener(mOnClickLisner)
        return dialog
    }

    //タイトル
    fun setTitle(title: String) {
        mTitle = title
        mPositiveBtnListener = null
    }

    //メッセージ設定
    fun setMessage(msg: CharSequence) {
        mMessage = msg
    }

    fun setOnPositiveClickListener(code: Int) {
        mPositiveBtnListener = View.OnClickListener {
            when (code) {
                1 -> DBBackup.dbBackup(activity)
                2 -> DBBackup.DBImport(activity)
                3 -> {
                    val mb_file = File(Environment.getExternalStorageDirectory().path + "/KUMIWAKE_Backup/mb.db")
                    val gp_file = File(Environment.getExternalStorageDirectory().path + "/KUMIWAKE_Backup/gp.db")
                    val dir = File(Environment.getExternalStorageDirectory().path + "/KUMIWAKE_Backup")

                    if (!dir.exists()) {
                        Toast.makeText(activity, getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }
                    mb_file.delete()
                    gp_file.delete()
                    dir.delete()
                    Toast.makeText(activity, getString(R.string.deleted_backup_file), Toast.LENGTH_SHORT).show()
                }
            }
            dismiss()
        }
    }

    companion object {
        var mPositiveBtnListener: View.OnClickListener? = null
    }
}
