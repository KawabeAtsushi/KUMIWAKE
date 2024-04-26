package com.pandatone.kumiwake.setting

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.history.HistoryAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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

    private fun getFileNameByDB(dbFilePath: String): String {
        return when (dbFilePath) {
            File(mb_db_file).name -> "mb.db"
            File(gp_db_file).name -> "gp.db"
            File(hs_db_file).name -> "hs.db"
            else -> {
                throw Exception("Invalid File")
            }
        }
    }

    private fun getDBByFileName(fileName: String): String {
        return when (fileName) {
            "mb.db" -> mb_db_file
            "gp.db" -> gp_db_file
            "hs.db" -> hs_db_file
            else -> {
                throw Exception("Invalid File Name")
            }
        }
    }

    fun addBackupFiles(c: Context, zipOutputStream: ZipOutputStream) {
        Log.d(TAG, "addBackupFiles")
        context = c
        val backupFile = createFolderWithFiles(context, mb_db_file, gp_db_file, hs_db_file)
        addFolderToZip(backupFile, zipOutputStream)
        zipOutputStream.finish()
        backupFile.copyTo(File(c.getExternalFilesDir(null), "kumiwake_backup"), overwrite = true)
    }

    private fun createFolderWithFiles(context: Context, vararg filePaths: String): File {
        val folderName = "kumiwake_backup"
        val filesDir = context.getExternalFilesDir(null)
        val folder = File(filesDir, folderName)

        if (!folder.exists()) {
            val created = folder.mkdirs()
            if (!created) {
                Log.e(TAG, "Cannot create backup folder")
            }
        }

        for (filePath in filePaths) {
            val file = File(filePath)
            if (file.exists()) {
                val destinationFile = File(folder, getFileNameByDB(file.name))
                try {
                    file.copyTo(destinationFile, overwrite = true)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to copy file: ${file.name}", e)
                }
            }
        }

        return folder
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

    fun readFile(c: Context, zipInputStream: ZipInputStream) {
        context = c
        val buffer = ByteArray(1024)
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val fileName = entry.name
            val file = File(getDBByFileName(fileName))
            val outputStream = FileOutputStream(file)
            var len: Int
            while (zipInputStream.read(buffer).also { len = it } > 0) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()
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
