package com.pandatone.kumiwake.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup
import java.io.UnsupportedEncodingException


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(private var mTitle: String, private var mMessage: CharSequence,private val backup:Boolean) : DialogFragment() {

    lateinit var filePathInput: EditText

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
            val path = DBBackup.dir_path
            if(backup) {
                setMkdirErrorView(dialog,path)
                activity?.let { DBBackup.dbBackup(it, path, dialog) }
            }else{
                setImportErrorView(dialog,path)
                activity?.let { DBBackup.dbImport(it, path, dialog) }
            }
        }
        // いいえボタンのリスナ
        dialog.findViewById<View>(R.id.negative_button).setOnClickListener { dismiss() }

        return dialog
    }

    private fun setMkdirErrorView(mDialog: Dialog, path: String) {

        mDialog.setContentView(R.layout.file_manager_dialog_layout)
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.failed_to_mkdirs)
        //FilePathInput
        filePathInput = mDialog.findViewById<View>(R.id.input_file_path) as EditText
        // Browseボタンのリスナ
        mDialog.findViewById<View>(R.id.browse_button).setOnClickListener { onBrowseClick() }
        (mDialog.findViewById<View>(R.id.run_button) as Button).text = getString(R.string.back_up_db)
        // Backup ボタンのリスナ
        mDialog.findViewById<View>(R.id.run_button).setOnClickListener {
                activity?.let { it1 -> DBBackup.dbBackup(it1,filePathInput.text.toString(),mDialog) }
        }
        // Cancel ボタンのリスナ
        mDialog.findViewById<View>(R.id.cancel_button).setOnClickListener { dismiss() }
    }

    private fun setImportErrorView(mDialog: Dialog, path: String) {

        mDialog.setContentView(R.layout.file_manager_dialog_layout)
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.nothing_file) + "\n" + path +
                "\n\n" + getString(R.string.failed_import)
        //FilePathInput
        filePathInput = mDialog.findViewById<View>(R.id.input_file_path) as EditText
        // Browseボタンのリスナ
        mDialog.findViewById<View>(R.id.browse_button).setOnClickListener { onBrowseClick() }
        // Import ボタンのリスナ
        mDialog.findViewById<View>(R.id.run_button).setOnClickListener {
            activity?.let { it1 -> DBBackup.dbImport(it1,filePathInput.text.toString(),mDialog) }
        }
        // Cancel ボタンのリスナ
        mDialog.findViewById<View>(R.id.cancel_button).setOnClickListener { dismiss() }
    }

    private fun onBrowseClick() {
        if(backup) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_TITLE, "KUMIWAKE_Backup");
            startActivityForResult(intent, REQUEST_MKDIR)
        }else{
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "file/*"
            }
            startActivityForResult(intent, REQUEST_IMPORT)
        }
    }

    // 識別用のコード
    private val REQUEST_MKDIR = 12345
    private val REQUEST_IMPORT = 12345

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == 12345 && resultCode == Activity.RESULT_OK) {
                val filePath = data!!.dataString
                filePathInput.setText(filePath, TextView.BufferType.NORMAL)
            }
        } catch (e: UnsupportedEncodingException) {
            // いい感じに例外処理
        }

    }
}
