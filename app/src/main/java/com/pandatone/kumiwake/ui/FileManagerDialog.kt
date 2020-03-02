package com.pandatone.kumiwake.ui

import android.annotation.TargetApi
import android.app.ActionBar
import androidx.appcompat.app.AppCompatDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.view.*
import android.widget.*


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(private var mTitle: String, private var mMessage: CharSequence, private val backup: Boolean) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val dialog = activity?.let { AppCompatDialog(it) }!!
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
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            val path = context!!.getExternalFilesDir(null).toString() + "/KUMIWAKE_Backup"
            if (backup) {
                setMkdirErrorView(dialog, path)
            } else {
                setImportErrorView(dialog, path)
            }
        }
        // いいえボタンのリスナ
        (dialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }

        return dialog
    }

    //バックアップエラーの際のダイアログ生成
    private fun setMkdirErrorView(mDialog: AppCompatDialog, path: String) {
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.failed_to_mkdirs) + "\n" + path
        // OKボタンのリスナ
        (mDialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbBackup(it1, path, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        (mDialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }
    }

    //インポートエラーの際のダイアログ生成
    private fun setImportErrorView(mDialog: AppCompatDialog, path: String) {
        //コピーパスボタン設定
        val copyPathButton = mDialog.findViewById<Button>(R.id.copy_path_button)
        if (copyPathButton != null) {
            copyPathButton.visibility = View.VISIBLE
            copyPathButton.setOnClickListener { copyToClipboard(path) }
        }

        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.nothing_file) + "\n" + DBBackup.dir_path +
                "\n\n" + getString(R.string.failed_import) + "\n" + path

        // OKボタンのリスナ
        (mDialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbImport(it1, path, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        (mDialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }
    }

    //テキストをコピー
    private fun copyToClipboard(text: String) {
        // copy to clipboard
        val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                ?: return
        clipboardManager.setPrimaryClip(ClipData.newPlainText("copyText", text))
        Toast.makeText(activity, getString(R.string.copied_path), Toast.LENGTH_SHORT).show()
    }

}
