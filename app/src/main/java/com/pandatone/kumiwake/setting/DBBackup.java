package com.pandatone.kumiwake.setting;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.adapter.MemberListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DBBackup {
    static String sd_dir, sd_stt, dir_path, mb_db_file, gp_db_file;
    static boolean b;

    public static void DBBackup(Context c) {
        getPath(c);

        File f = new File(dir_path);
        b = f.exists();           //SDカードにkumiwakeディレクトリがあるか。
        if (b == false) {          //ディレクトリが存在しないので作成。
            b = f.mkdirs();    //　sdcard/kumiwakeディレクトリを作ってみる。
            if (b == false) {
                Toast.makeText(c, SettingHelp.getContext().getString(R.string.failed_back_up), Toast.LENGTH_SHORT).show();
                return;         //ディレクトリ作成失敗
            }
        }

        filecopy(mb_db_file, dir_path + "/mb.db", c, true);
        filecopy(gp_db_file, dir_path + "/gp.db", c, true);//DBのファイルをSDにコピー
    }

    public static void DBImport(Context c) {
        getPath(c);

        File f = new File(dir_path);
        b = f.exists();           //SDカードにkumiwakeディレクトリがあるか。
        if (b == false) {
            Toast.makeText(c, SettingHelp.getContext().getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show();
            return;
        }

        filecopy(dir_path + "/mb.db", mb_db_file, c, false);
        filecopy(dir_path + "/gp.db", gp_db_file, c, false);//DBのファイルをインポート
    }


    //ファイルのコピー（チャネルを使用）
    public static void filecopy(String file_src, String file_dist, Context c, Boolean backup) {
        int err;
        FileInputStream fis;
        FileOutputStream fos;

        err = 0;
        File fi = new File(file_src);
        File fo = new File(file_dist);
        try {
            fis = new FileInputStream(fi);
            FileChannel chi = fis.getChannel();

            fos = new FileOutputStream(fo);
            FileChannel cho = fos.getChannel();

            chi.transferTo(0, chi.size(), cho);
            chi.close();
            cho.close();
        } catch (FileNotFoundException e) {
            err = 1;
        } catch (IOException e) {
            err = 2;
        }
        if (err == 0 && backup) {
            Toast.makeText(c, SettingHelp.getContext().getString(R.string.back_up_completed), Toast.LENGTH_SHORT).show();
        } else if (err == 0 && !backup) {
            Toast.makeText(c, SettingHelp.getContext().getString(R.string.import_completed), Toast.LENGTH_SHORT).show();
        }
    }

    public static void getPath(Context c) {
        sd_dir = Environment.getExternalStorageDirectory().getPath();     //SDカードディレクトリ
        sd_stt = Environment.getExternalStorageState();           //SDカードの状態を取得
        MemberListAdapter dbAdapter = new MemberListAdapter(SettingHelp.getContext());
        GroupListAdapter gpdbAdapter = new GroupListAdapter(SettingHelp.getContext());
        dbAdapter.open();
        dbAdapter.getAllNames();
        dbAdapter.close();
        gpdbAdapter.open();
        gpdbAdapter.getAllNames();
        gpdbAdapter.close();
        mb_db_file = MemberListAdapter.db.getPath();
        gp_db_file = GroupListAdapter.db.getPath();   //DBのディレクトリとファイル名
        dir_path = sd_dir + "/KUMIWAKE_Backup";

        b = sd_stt.equals(Environment.MEDIA_MOUNTED);     //SDカードの状態
        if (b == false) {  //書込み状態でマウントされていない。
            Toast.makeText(c, SettingHelp.getContext().getString(R.string.not_mounted), Toast.LENGTH_SHORT).show();
            return;         //ディレクトリ作成失敗
        }

    }

}
