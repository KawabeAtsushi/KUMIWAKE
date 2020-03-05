package com.pandatone.kumiwake.setting

import androidx.appcompat.app.AppCompatDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import android.content.Context
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText


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
        val pathTextView = mDialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = getString(R.string.failed_to_mkdirs)
        pathTextView.text = path
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
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        val messageTextView = mDialog.findViewById<View>(R.id.dialog_message) as TextView
        val pathTextView = mDialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        messageTextView.text = getString(R.string.nothing_file) + "\n" + DBBackup.dir_path +
                "\n\n" + getString(R.string.failed_import)
        pathTextView.text = path

        //コピーパスボタン設定
        val allocatePathButton = mDialog.findViewById<Button>(R.id.specify_path_button)
        if (allocatePathButton != null) {
            allocatePathButton.visibility = View.VISIBLE
            allocatePathButton.setOnClickListener {
                specifyPath(pathTextView)
            }
        }

        // OKボタンのリスナ
        (mDialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            val location = pathTextView.text.toString()
            activity?.let { it1 -> DBBackup.dbImport(it1, location, mDialog) }
            dismiss()
        }
        // Cancel ボタンのリスナ
        (mDialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }
    }

    //パスを指定
    private fun specifyPath(pathView:TextView) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.input_text_dialog, activity?.findViewById<View>(R.id.filter_member) as? ViewGroup)
        val pathInputBox = layout.findViewById<View>(R.id.path_input) as TextInputEditText
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder.setTitle(activity?.getText(R.string.specify_path))
        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()

        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener{
            if(!pathInputBox.text.isNullOrEmpty()){
                val location = pathInputBox.text.toString()
                pathView.text = location
            }
            dialog.dismiss()
        }
        val cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

}
