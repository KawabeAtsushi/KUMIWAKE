package com.pandatone.kumiwake.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(private var mTitle: String, private var mMessage: CharSequence) : DialogFragment() {

    lateinit var filePathInput: EditText
    private var filePath = ""
    //onClickリスナ(Positive)
    var mPositiveBtnListener: View.OnClickListener? = null
    private val viewModel: FMDialogViewModel
        get() {
            return ViewModelProviders.of(this).get(FMDialogViewModel::class.java)
        }

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
        viewModel.path.observe(this, Observer<String> { path ->
            dialog.findViewById<View>(R.id.positive_button).setOnClickListener {
                mPositiveBtnListener
                setErrorView(dialog, path)
            }
        })
        // いいえボタンのリスナ
        dialog.findViewById<View>(R.id.negative_button).setOnClickListener { dismiss() }

        viewModel.showDialog.observe(this, Observer { if(!it){dismiss()} })

        return dialog
    }

    private fun setErrorView(mDialog: Dialog, path: String) {

        mDialog.setContentView(R.layout.file_manager_dialog_layout)
        // タイトル設定
        (mDialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        (mDialog.findViewById<View>(R.id.dialog_message) as TextView).text = setMessage(path)
        //FilePathInput
        filePathInput = mDialog.findViewById<View>(R.id.input_file_path) as EditText
        // Browseボタンのリスナ
        mDialog.findViewById<View>(R.id.browse_button).setOnClickListener { onBrowseClick() }
        // Import ボタンのリスナ
        mDialog.findViewById<View>(R.id.import_button).setOnClickListener { onImportClick(filePathInput.text.toString()) }
        // Cancel ボタンのリスナ
        mDialog.findViewById<View>(R.id.cancel_button).setOnClickListener { dismiss() }

        //Log.d("FilePath",filePathInput.text.toString())
        Log.d("FilePath","aaaaaaaaaaaa")
    }

    //メッセージ設定
    private fun setMessage(path: String): CharSequence {
        var str = ""
        str = getString(R.string.nothing_file) + "\n" + path +
                "\n\n" + getString(R.string.failed_import)
        return str
    }

    private fun onImportClick(path: String) {
        activity?.let { it1 -> DBBackup.dbImport(path, it1) }
    }

    private fun onBrowseClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "file/*"
        }
        startActivityForResult(Intent.createChooser(intent, "FileManager"), CHOSE_FILE_CODE)
    }

    // 識別用のコード
    private val CHOSE_FILE_CODE = 12345

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == CHOSE_FILE_CODE && resultCode == Activity.RESULT_OK) {
                filePath = data!!.dataString!!
                filePath = filePath.substring(filePath.indexOf("storage"))
                //filePath = data!!.dataString!!.replace("file://", "")
                filePath = URLDecoder.decode(filePath, "utf-8")
                filePathInput.setText(filePath, TextView.BufferType.NORMAL)
            }
        } catch (e: UnsupportedEncodingException) {
            // いい感じに例外処理
        }

    }
}
