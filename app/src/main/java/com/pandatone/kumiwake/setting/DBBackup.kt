package com.pandatone.kumiwake.setting

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@SuppressLint("StaticFieldLeak")
object DBBackup {
    val dir_path: String
        get() {
            val sdDir = Environment.getExternalStorageDirectory().path     //SDカードディレクトリ
            return "$sdDir/KUMIWAKE_Backup"
        }
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
    private lateinit var context: Context
    private var b: Boolean = false

    @SuppressLint("SetTextI18n")
    fun dbBackup(c: Context, path: String, dialog: AppCompatDialog) {
        context = c
        checkSDStatus(context)

        val f = File(path)

        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {          //ディレクトリが存在しないので作成。
            b = f.mkdirs()    //　sdcard/kumiwakeディレクトリを作ってみる。
            if (!b) {
                Toast.makeText(c, c.getString(R.string.failed_to_mkdirs), Toast.LENGTH_SHORT).show()
                return          //ディレクトリ作成失敗
            }
        }

        var err = 0
        err += fileCopy(mb_db_file, "$path/mb.db", c)
        err += fileCopy(gp_db_file, "$path/gp.db", c)//DBのファイルをSDにコピー

        if (err == 0) {
            Toast.makeText(c, c.getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(c, c.getString(R.string.failed_backup), Toast.LENGTH_SHORT).show()
        }
        dialog.dismiss()
    }

    @SuppressLint("SetTextI18n")
    fun dbImport(c: Context, path: String, dialog: AppCompatDialog) {
        context = c
        checkSDStatus(context)

        var err = 0
        val f = File(path)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {
            Toast.makeText(c, c.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
            return
        }

        err += fileCopy("$path/mb.db", mb_db_file, c)
        err += fileCopy("$path/gp.db", gp_db_file, c) //DBのファイルをインポート

        if (err == 0) {
            dialog.dismiss()
            Toast.makeText(c, c.getString(R.string.import_completed), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(c, "Error : $err", Toast.LENGTH_SHORT).show()
            // メッセージ設定
            (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = c.getString(R.string.nothing_file) + "\n" + path +
                    "\n\n" + c.getString(R.string.failed_import)
        }
    }


    //ファイルのコピー（チャネルを使用）
    private fun fileCopy(src_path: String, dest_path: String, c: Context): Int {
        var err: Int = 0

        val src = File(src_path)
        val dest = File(dest_path)

        try {
            src.copyTo(dest, overwrite = true)
        } catch (e: FileNotFoundException) {
            err = 1
            Toast.makeText(c, c.getString(R.string.error_has_occurred) + "\n(FileNotFoundException)", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            err = 2
            Toast.makeText(c, c.getString(R.string.error_has_occurred) + "\n(IOException)", Toast.LENGTH_SHORT).show()
        }
        return err
    }

    private fun checkSDStatus(c: Context) {

        b = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED     //SDカードの状態
        if (!b) {  //書込み状態でマウントされていない。
            Toast.makeText(c, c.getString(R.string.not_mounted), Toast.LENGTH_SHORT).show()
            return          //ディレクトリ作成失敗
        }

    }

}
