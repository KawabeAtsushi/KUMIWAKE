package com.pandatone.kumiwake.setting

import android.annotation.SuppressLint
import android.content.ContentResolver.MimeTypeInfo
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.FileProvider
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.history.HistoryAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@SuppressLint("StaticFieldLeak")
object DBBackup {
     private val mb_db_file: String
        get() {
            MemberAdapter(context).also {
                it.open()
                it.getDB
                it.close()
            }
            return MemberAdapter.db.path
        }
    private val gp_db_file: String
        get() {
            GroupAdapter(context).also {
                it.open()
                it.getDB
                it.close()
            }
            return GroupAdapter.db.path   //DBのディレクトリとファイル名
        }
    private val hs_db_file: String
        get() {
            HistoryAdapter(context).also {
                it.open()
                it.getDB
                it.close()
            }
            return HistoryAdapter.db.path   //DBのディレクトリとファイル名
        }
    private lateinit var context: Context
    private var b: Boolean = false

    private fun createFolderWithFiles(context: Context, vararg filePaths: String): File? {
        val folderName = "kumiwake_backup"
        val filesDir = context.getExternalFilesDir(null)
        val folder = File(filesDir, folderName)

        if (!folder.exists()) {
            val created = folder.mkdirs()
            if (!created) {
                Log.e(TAG, "Cannot create backup folder")
                return null
            }
        }

        for (filePath in filePaths) {
            val file = File(filePath)
            if (file.exists()) {
                val destinationFile = File(folder, file.name)
                try {
                    file.copyTo(destinationFile, overwrite = true)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to copy file: ${file.name}", e)
                }
            }
        }

        return zipFolder(context, folder)
    }

    private fun zipFolder(context: Context, folder: File): File? {
        val zipFileName = "${folder.name}.zip"
        val zipFile = File(context.cacheDir, zipFileName)

        try {
            ZipOutputStream(zipFile.outputStream()).use { zipOutputStream ->
                addFolderToZip(folder, zipOutputStream)
                zipOutputStream.finish()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create zip file", e)
            return null
        }

        zipFile.copyTo(File(folder.parent, zipFileName), true)

        return zipFile
    }

    private fun addFolderToZip(folder: File, zipOutputStream: ZipOutputStream) {
        folder.listFiles()?.forEach { file ->
            val filePath = file.name
            try {
                FileInputStream(file).use { fileInputStream ->
                    val zipEntry = ZipEntry(filePath)
                    zipOutputStream.putNextEntry(zipEntry)
                    fileInputStream.copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to add file to zip: $filePath", e)
            }
        }
    }

    private fun sendFile(file: File) {
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = getMimeType(file)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share via")
        context.startActivity(chooserIntent)
    }

    private fun getMimeType(file:File):String{
            val fileExtension: String =
                MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        return mimeType ?: "*/*"
        }

    @SuppressLint("SetTextI18n")
    fun dbBackup(c: Context, dialog: AppCompatDialog) {
        context = c
        val backupFile = createFolderWithFiles(context, mb_db_file, gp_db_file, hs_db_file)
        if (backupFile != null) {
            try {
                val zipFile = createFolderWithFiles(context, mb_db_file, gp_db_file, hs_db_file)
                zipFile?.let {
                    sendFile(zipFile)
                    Toast.makeText(c, c.getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                Toast.makeText(c, c.getString(R.string.failed_backup), Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "backup file is null")
            Toast.makeText(c, c.getString(R.string.failed_backup), Toast.LENGTH_SHORT).show()
        }

        dialog.dismiss()
    }

    @SuppressLint("SetTextI18n")
    fun dbImport(c: Context, path: String, dialog: AppCompatDialog) {
        context = c

        var err = 0
        val f = File(path)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {
            Toast.makeText(c, c.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
            return
        }

        err += fileCopy("$path/mb.db", mb_db_file, c)
        err += fileCopy("$path/gp.db", gp_db_file, c)//DBのファイルをインポート

        if (err == 0) {
            dialog.dismiss()
            Toast.makeText(c, c.getString(R.string.import_completed), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(c, "Error : $err", Toast.LENGTH_SHORT).show()
            // メッセージ設定
            (dialog.findViewById<View>(R.id.dialog_message) as TextView).text =
                c.getString(R.string.nothing_file) + "\n" + path +
                        "\n\n" + c.getString(R.string.failed_import)
        }
    }


    //ファイルのコピー（チャネルを使用）
    private fun fileCopy(src_path: String, dest_path: String, c: Context): Int {
        var err = 0

        val src = File(src_path)
        val dest = File(dest_path)

        try {
            src.copyTo(dest, overwrite = true)
        } catch (e: FileNotFoundException) {
            err = 1
            Toast.makeText(
                c,
                c.getString(R.string.error_has_occurred) + "\n(FileNotFoundException)",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            err = 2
            Toast.makeText(
                c,
                c.getString(R.string.error_has_occurred) + "\n(IOException)",
                Toast.LENGTH_SHORT
            ).show()
        }
        return err
    }

}
