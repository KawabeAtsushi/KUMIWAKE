package com.pandatone.kumiwake.ui.dialogs

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.setting.DBBackup
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created by atsushi_2 on 2016/11/11.
 */

class FileManagerDialog(
    private var mTitle: String,
    private var mMessage: CharSequence,
    private val backup: Boolean
) : DialogFragment() {

    private val mimeType = "application/zip"

    private lateinit var writeZipLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        writeZipLauncher =
            registerForActivityResult(CreateDocument(mimeType)) { uri ->
                Log.e(TAG, "registerForActivityResult Called")
                if (uri != null) {
                    writeZipFile(uri)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.back_up_completed),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showWriteErrorDialog()
                }
            }
    }

//    private val readZipLauncher =
//        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//            if (uri != null) {
//                readZipFile(uri)
//                Toast.makeText(
//                    requireContext(),
//                    getString(R.string.import_completed),
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                showImportErrorDialog()
//            }
//        }

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val dialog = AppCompatDialog(requireContext())
        // タイトル非表示
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = mTitle
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = mMessage
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            if (backup) {
                writeZipLauncher.launch("kumiwake_backup.zip")
            } else {
//                readZipLauncher.launch(mimeType)
            }
            dismiss()
        }
        (dialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }

        return dialog
    }

    private fun callback(uri: Uri?) {
        if (uri != null) {
            writeZipFile(uri)
            Toast.makeText(
                requireContext(),
                getString(R.string.back_up_completed),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            showWriteErrorDialog()
        }
    }

    private fun writeZipFile(uri: Uri) {
        requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zipOutputStream ->
                DBBackup.addBackupFiles(requireContext(), zipOutputStream)
            }
        }
    }

    private fun readZipFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val zipInputStream = ZipInputStream(inputStream)
            DBBackup.readFile(requireContext(), zipInputStream)
            inputStream?.close()
        } catch (e: Exception) {
            showImportErrorDialog()
        }
    }

    //バックアップエラーの際のダイアログ生成
    private fun showWriteErrorDialog() {
        val dialog = AppCompatDialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog_layout)
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        val pathTextView = dialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text =
            getString(R.string.failed_to_mkdirs)
        // OKボタン
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.negative_button) as TextView).visibility = View.GONE
        dialog.show()
    }

    //インポートエラーの際のダイアログ生成
    private fun showImportErrorDialog() {
        val dialog = AppCompatDialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog_layout)
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = getString(R.string.error)
        // メッセージ設定
        val messageTextView = dialog.findViewById<View>(R.id.dialog_message) as TextView
        val pathTextView = dialog.findViewById<View>(R.id.dialog_path) as TextView
        pathTextView.visibility = View.VISIBLE
        messageTextView.text = getString(R.string.failed_import)
        // OKボタン
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener {
            dialog.dismiss()
        }
        (dialog.findViewById<View>(R.id.negative_button) as TextView).visibility = View.GONE
        dialog.show()
    }
}
