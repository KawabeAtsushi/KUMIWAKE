package com.pandatone.kumiwake.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var backupZipLauncher: ActivityResultLauncher<String>
    private lateinit var importZipLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backupZipLauncher =
            registerForActivityResult(CreateDocument(mimeType)) { uri ->
                if (uri != null) {
                    backupZipFile(uri)
                } else {
                    showBackupErrorDialog()
                }
            }

        importZipLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    importZipFile(uri)
                } else {
                    showImportErrorDialog()
                }
            }
    }

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
                backupZipLauncher.launch("kumiwake_backup.zip")
            } else {
                importZipLauncher.launch(mimeType)
            }
        }
        (dialog.findViewById<View>(R.id.negative_button) as TextView).setOnClickListener { dismiss() }

        return dialog
    }

    private fun backupZipFile(uri: Uri) {
        requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
            try {
                ZipOutputStream(outputStream).use { zipOutputStream ->
                    DBBackup.addBackupFiles(requireContext(), zipOutputStream)
                }
                showBackupCompleteDialog()
            } catch (e: Exception) {
                showBackupErrorDialog()
            }
        } ?: showBackupErrorDialog()
    }

    private fun importZipFile(uri: Uri) {
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            try {
                ZipInputStream(inputStream).use { zipInputStream ->
                    DBBackup.readFile(requireContext(), zipInputStream)
                }
                showImportCompleteDialog()
            } catch (e: Exception) {
                showImportErrorDialog()
            }
        } ?: showImportErrorDialog()
    }

    private fun showBackupErrorDialog() =
        showDialog(getString(R.string.error), getString(R.string.failed_backup))

    private fun showImportErrorDialog() =
        showDialog(getString(R.string.error), getString(R.string.failed_import))

    private fun showBackupCompleteDialog() =
        showDialog(getString(R.string.success), getString(R.string.back_up_completed))

    private fun showImportCompleteDialog() =
        showDialog(getString(R.string.success), getString(R.string.import_completed))


    //成功/エラーの際のダイアログ生成
    private fun showDialog(title: String, message: String) {
        val dialog = AppCompatDialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog_layout)
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = title
        // メッセージ設定
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = message
        // OKボタン
        (dialog.findViewById<View>(R.id.positive_button) as TextView).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.negative_button) as TextView).visibility = View.GONE
        dialog.show()
    }
}
