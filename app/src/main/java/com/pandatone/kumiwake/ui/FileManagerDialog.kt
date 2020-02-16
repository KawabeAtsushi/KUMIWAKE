package com.pandatone.kumiwake.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(private var mTitle: String, private var mMessage: CharSequence, private val backup: Boolean) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let { Dialog(it) }!!
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
        dialog.findViewById<View>(R.id.positive_button).setOnClickListener {
            val path = context!!.getExternalFilesDir(null).toString() + "/KUMIWAKE_Backup"
            if (backup) {
                setMkdirErrorView(dialog, path)
            } else {
                setImportErrorView(dialog, path)
            }
        }
        // いいえボタンのリスナ
        dialog.findViewById<View>(R.id.negative_button).setOnClickListener { dismiss() }

        return dialog
    }

    private fun setMkdirErrorView(mDialog: Dialog, path: String) {
        mDialog.setContentView(R.layout.custom_dialog_layout)
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.failed_to_mkdirs) + "\n" + path
        // OKボタンのリスナ
        mDialog.findViewById<View>(R.id.positive_button).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbBackup(it1, path, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        mDialog.findViewById<View>(R.id.negative_button).setOnClickListener { dismiss() }
    }

    private fun setImportErrorView(mDialog: Dialog, path: String) {

        mDialog.setContentView(R.layout.custom_dialog_layout)
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.nothing_file) + "\n" + DBBackup.dir_path +
                "\n\n" + getString(R.string.failed_import) + "\n" + path
        // OKボタンのリスナ
        mDialog.findViewById<View>(R.id.positive_button).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbImport(it1, path, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        mDialog.findViewById<View>(R.id.negative_button).setOnClickListener { dismiss() }
    }

}
