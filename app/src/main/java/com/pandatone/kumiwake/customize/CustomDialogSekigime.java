package com.pandatone.kumiwake.customize;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.sekigime.SekigimeResult;
import com.pandatone.kumiwake.sekigime.SquareTableCustom;

/**
 * Created by atsushi_2 on 2016/11/11.
 */

public class CustomDialogSekigime extends DialogFragment {
    private String mTitle = "";
    private int mPosition = 1;
    private ImageView iv;
    private CheckBox fmDeploy;
    public static View.OnClickListener mPositiveBtnListener = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.custom_dialog_layout_sekigime);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fmDeploy = (CheckBox)dialog.findViewById(R.id.even_fm_deploy_check);
        // タイトル設定
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(mTitle);
        // 画像設定
        iv = ((ImageView) dialog.findViewById(R.id.table_img));
        setImage();

        //カスタム表示
        TextView custom =((TextView) dialog.findViewById(R.id.be_custom));
        Button positive_bt = ((Button) dialog.findViewById(R.id.positive_button));
        if(mPosition==1){
            custom.setVisibility(View.VISIBLE);
            positive_bt.setText(getText(R.string.move_custom));
        }
        // OK ボタンのリスナ
        setOnPositiveClickListener();
        positive_bt.setOnClickListener(mPositiveBtnListener);

        // いいえボタンのリスナ
        dialog.findViewById(R.id.negative_button).setOnClickListener(mOnClickLisner);
        return dialog;
    }

    //タイトル
    public void setTitle(String title) {
        mTitle = title;
    }

    //メッセージ設定
    public void setPosition(int position) {
        mPosition = position;
    }

    public void setImage(){
        int width,height,widthDp=0,heightDp=0;
        switch (mPosition){
            case 1:
                iv.setImageResource(R.drawable.square_table);
                widthDp=180;
                heightDp=250;
                break;
            case 2:
                iv.setImageResource(R.drawable.parallel_table);
                widthDp=180;
                heightDp=250;
                break;
            case 3:
                iv.setImageResource(R.drawable.circle_table);
                widthDp=200;
                heightDp=200;
                break;
            case 4:
                iv.setImageResource(R.drawable.counter_table);
                widthDp=80;
                heightDp=210;
                break;
        }
        float scale = getResources().getDisplayMetrics().density; //画面のdensityを指定。
        width = (int) (widthDp * scale + 0.5f);
        height=(int) (heightDp * scale + 0.5f);
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        iv.setLayoutParams(layoutParams);
    }

    public void setOnPositiveClickListener() {
        mPositiveBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SekigimeResult.fmDeploy = fmDeploy.isChecked();
                if (mPosition == 1) {
                    Intent intent = new Intent(getActivity(), SquareTableCustom.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), SekigimeResult.class);
                    startActivity(intent);
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
