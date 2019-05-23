package com.pandatone.kumiwake.customize;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.setting.DBBackup;

import java.io.File;

/**
 * Created by atsushi_2 on 2016/11/11.
 */

public class CustomDialog extends DialogFragment {
    private String mTitle = "";
    private CharSequence mMessage = "";
    public static View.OnClickListener mPositiveBtnListener = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.custom_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // タイトル設定
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(mTitle);
        // メッセージ設定
        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(mMessage);
        // OK ボタンのリスナ
        if (mPositiveBtnListener == null) {
            dialog.findViewById(R.id.negative_button).setVisibility(View.GONE);
            dialog.findViewById(R.id.positive_button).setOnClickListener(mOnClickLisner);
        } else {
            dialog.findViewById(R.id.positive_button).setOnClickListener(mPositiveBtnListener);
        }

        // いいえボタンのリスナ
        dialog.findViewById(R.id.negative_button).setOnClickListener(mOnClickLisner);
        return dialog;
    }

    //タイトル
    public void setTitle(String title) {
        mTitle = title;
        mPositiveBtnListener = null;
    }

    //メッセージ設定
    public void setMessage(CharSequence msg) {
        mMessage = msg;
    }

    public void setOnPositiveClickListener(final int code) {
        mPositiveBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (code){
                    case 1:
                        DBBackup.DBBackup(getActivity());
                        break;
                    case 2:
                        DBBackup.DBImport(getActivity());
                        break;
                    case 3:
                        File mb_file = new File(Environment.getExternalStorageDirectory().getPath() + "/KUMIWAKE_Backup/mb.db");
                        File gp_file = new File(Environment.getExternalStorageDirectory().getPath() + "/KUMIWAKE_Backup/gp.db");
                        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/KUMIWAKE_Backup");

                        if (!dir.exists()) {
                            Toast.makeText(getActivity(), getString(R.string.not_exist_file), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mb_file.delete();
                        gp_file.delete();
                        dir.delete();
                        Toast.makeText(getActivity(), getString(R.string.deleted_backup_file), Toast.LENGTH_SHORT).show();
                        break;
                }
                dismiss();
            }
        };
        }

    //onClickリスナ
    private View.OnClickListener mOnClickLisner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
}
