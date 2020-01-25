package com.pandatone.kumiwake.setting

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.ui.DialogWarehouse
import com.pandatone.kumiwake.ui.FMDialogViewModel
import com.pandatone.kumiwake.ui.FileManagerDialog
import com.pandatone.kumiwake.ui.sekigime.SekigimeFragment
import com.pandatone.kumiwake.ui.settings.SettingsFragment
import java.io.*


object DBBackup {
    private val dir_path: String
        get() {
            val sdDir = Environment.getExternalStorageDirectory().path     //SDカードディレクトリ
            return "$sdDir/KUMIWAKE_Backup"
        }
    private val mb_db_file: String
        get() {
            MemberListAdapter(MyApplication.context!!).also {
                it.open()
                it.getDB
                it.close()
            }
            return MemberListAdapter.db.path
        }
    private val gp_db_file: String
        get() {
            GroupListAdapter(MyApplication.context!!).also {
                it.open()
                it.getDB
                it.close()
            }
            return GroupListAdapter.db.path   //DBのディレクトリとファイル名
        }
    private var b: Boolean = false

    fun dbBackup(c: Context) {
        checkSDStatus(c)

        val f = File(dir_path)

        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {          //ディレクトリが存在しないので作成。
            b = f.mkdirs()    //　sdcard/kumiwakeディレクトリを作ってみる。
            if (!b) {
                Toast.makeText(c, MyApplication.context?.getString(R.string.failed_backup), Toast.LENGTH_SHORT).show()
                return          //ディレクトリ作成失敗
            }
        }

        var err = 0
        err += fileCopy(mb_db_file, "$dir_path/mb.db", c)
        err += fileCopy(gp_db_file, "$dir_path/gp.db", c)//DBのファイルをSDにコピー

        if (err == 0) {
            Toast.makeText(c, MyApplication.context?.getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(c, MyApplication.context?.getString(R.string.failed_backup), Toast.LENGTH_SHORT).show()
        }
    }

    fun dbImport(importDir: String, c: Context) {
        checkSDStatus(c)

        var err = 0
        val f = File(importDir)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {
            Toast.makeText(c, MyApplication.context?.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
            err = 3
        }

        err += fileCopy("$importDir/mb.db", mb_db_file, c)
        err += fileCopy("$importDir/gp.db", gp_db_file, c) //DBのファイルをインポート

        if (err != 0) {
            FMDialogViewModel().path.postValue(importDir)
        } else {
            Toast.makeText(c, MyApplication.context?.getString(R.string.import_completed), Toast.LENGTH_SHORT).show()
            FMDialogViewModel().showDialog.postValue(false)
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
            Toast.makeText(c, MyApplication.context?.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            err = 2
            Toast.makeText(c, MyApplication.context?.getString(R.string.error_has_occurred), Toast.LENGTH_SHORT).show()
        }
        return err
    }

    private fun checkSDStatus(c: Context) {

        b = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED     //SDカードの状態
        if (!b) {  //書込み状態でマウントされていない。
            Toast.makeText(c, MyApplication.context!!.getString(R.string.not_mounted), Toast.LENGTH_SHORT).show()
            return          //ディレクトリ作成失敗
        }

    }

}
