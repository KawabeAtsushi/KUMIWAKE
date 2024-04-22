package com.pandatone.kumiwake.ui.dialogs


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(
    private var mTitle: String,
    private var mMessage: CharSequence,
    private val backup: Boolean
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val dialog = activity?.let { AppCompatDialog(it) }!!
        // タイトル非表示
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = mTitle
        // メッセージ設定
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = mMessage
        // OK ボタンのリスナ
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            if (backup) {
                activity?.let { it1 -> DBBackup.dbBackup(it1, dialog) }
            } else {
                setImportErrorView(dialog)
            }
        }
        // いいえボタンのリスナ
        (dialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }

        return dialog
    }

    //バックアップエラーの際のダイアログ生成
    private fun setMkdirErrorView(mDialog: AppCompatDialog) {
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        val pathTextView = mDialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text =
            getString(R.string.failed_to_mkdirs)
        // OKボタンのリスナ
        (mDialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbBackup(it1, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        (mDialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }
    }

    //インポートエラーの際のダイアログ生成
    private fun setImportErrorView(mDialog: AppCompatDialog) {
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        val messageTextView = mDialog.findViewById<View>(R.id.dialog_message) as TextView
        val pathTextView = mDialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        val warningTxt =
            getString(R.string.nothing_file) +"\n\n" + getString(R.string.failed_import)
        messageTextView.text = warningTxt

        // OKボタンのリスナ
        (mDialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            val location = pathTextView.text.toString()
            activity?.let { it1 -> DBBackup.dbImport(it1, location, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        (mDialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }
    }

}
