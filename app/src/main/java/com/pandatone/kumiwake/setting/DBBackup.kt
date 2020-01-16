package com.pandatone.kumiwake.setting

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import java.io.*
import androidx.core.app.ActivityCompat.startActivityForResult
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ui.settings.SettingsFragment


object DBBackup {
    private val dir_path: String
        get() {
//            val sdDir = Environment.getExternalStorageDirectory().path     //SDカードディレクトリ
//            return "$sdDir/KUMIWAKE_Backup"
            return SettingsFragment().decodedfilePath
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

    private val CHOSE_FILE_CODE:String = "asdfgh"

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

        fileCopy(mb_db_file, "$dir_path/mb.db", c, true)
        fileCopy(gp_db_file, "$dir_path/gp.db", c, true)//DBのファイルをSDにコピー
    }

    fun dbImport(c: Context) {
        checkSDStatus(c)

        val f = File(dir_path)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {
            Toast.makeText(c, MyApplication.context?.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
            return
        }

        fileCopy("$dir_path/mb.db", mb_db_file, c, false)
        fileCopy("$dir_path/gp.db", gp_db_file, c, false)//DBのファイルをインポート
    }


    //ファイルのコピー（チャネルを使用）
    private fun fileCopy(file_src: String, file_dist: String, c: Context, backup: Boolean) {
        var err: Int
        val fis: FileInputStream
        val fos: FileOutputStream

        Log.d("file_src",file_src)
        Log.d("file_dist",file_dist)

        err = 0
        val fi = File(file_src)
        val fo = File(file_dist)
        try {
            fis = FileInputStream(fi)
            val chi = fis.channel

            fos = FileOutputStream(fo)
            val cho = fos.channel

            chi.transferTo(0, chi.size(), cho)
            chi.close()
            cho.close()
        } catch (e: FileNotFoundException) {
            err = 1
            Toast.makeText(c, MyApplication.context?.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            err = 2
            Toast.makeText(c, MyApplication.context?.getString(R.string.error_has_occurred), Toast.LENGTH_SHORT).show()
        }

        if (err == 0 && backup) {
            Toast.makeText(c, MyApplication.context?.getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show()
        } else if (err == 0 && !backup) {
            Toast.makeText(c, MyApplication.context?.getString(R.string.import_completed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSDStatus(c: Context) {

        b = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED     //SDカードの状態
        if (!b) {  //書込み状態でマウントされていない。
            Toast.makeText(c, MyApplication.context!!.getString(R.string.not_mounted), Toast.LENGTH_SHORT).show()
            return          //ディレクトリ作成失敗
        }

    }

}
