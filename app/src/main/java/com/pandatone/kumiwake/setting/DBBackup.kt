package com.pandatone.kumiwake.setting

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.adapter.GroupListAdapter
import java.io.*

object DBBackup {
    private lateinit var sd_dir: String
    private lateinit var sd_stt: String
    private lateinit var dir_path: String
    private lateinit var mb_db_file: String
    private lateinit var gp_db_file: String
    private var b: Boolean = false

    fun dbBackup(c: Context) {
        getPath(c)

        val f = File(dir_path)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {          //ディレクトリが存在しないので作成。
            b = f.mkdirs()    //　sdcard/kumiwakeディレクトリを作ってみる。
            if (!b) {
                Toast.makeText(c, SettingHelp.context?.getString(R.string.failed_back_up), Toast.LENGTH_SHORT).show()
                return          //ディレクトリ作成失敗
            }
        }

        fileCopy(mb_db_file, "$dir_path/mb.db", c, true)
        fileCopy(gp_db_file, "$dir_path/gp.db", c, true)//DBのファイルをSDにコピー
    }

    fun DBImport(c: Context) {
        getPath(c)

        val f = File(dir_path)
        b = f.exists()           //SDカードにkumiwakeディレクトリがあるか。
        if (!b) {
            Toast.makeText(c, SettingHelp.context?.getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show()
            return
        }

        fileCopy("$dir_path/mb.db", mb_db_file, c, false)
        fileCopy("$dir_path/gp.db", gp_db_file, c, false)//DBのファイルをインポート
    }


    //ファイルのコピー（チャネルを使用）
    private fun fileCopy(file_src: String, file_dist: String, c: Context, backup: Boolean?) {
        var err: Int
        val fis: FileInputStream
        val fos: FileOutputStream

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
        } catch (e: IOException) {
            err = 2
        }

        if (err == 0 && backup!!) {
            Toast.makeText(c, SettingHelp.context?.getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show()
        } else if (err == 0 && (!backup!!)) {
            Toast.makeText(c, SettingHelp.context?.getString(R.string.import_completed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPath(c: Context) {
        sd_dir = Environment.getExternalStorageDirectory().path     //SDカードディレクトリ
        sd_stt = Environment.getExternalStorageState()           //SDカードの状態を取得
        val dbAdapter = MemberListAdapter(SettingHelp.context!!)
        val gpdbAdapter = GroupListAdapter(SettingHelp.context!!)
        dbAdapter.open()
        dbAdapter.allNames
        dbAdapter.close()
        gpdbAdapter.open()
        gpdbAdapter.allNames
        gpdbAdapter.close()
        mb_db_file = MemberListAdapter.db.path
        gp_db_file = GroupListAdapter.db.path   //DBのディレクトリとファイル名
        dir_path = "$sd_dir/KUMIWAKE_Backup"

        b = sd_stt == Environment.MEDIA_MOUNTED     //SDカードの状態
        if (!b) {  //書込み状態でマウントされていない。
            Toast.makeText(c, SettingHelp.context!!.getString(R.string.not_mounted), Toast.LENGTH_SHORT).show()
            return          //ディレクトリ作成失敗
        }

    }

}
