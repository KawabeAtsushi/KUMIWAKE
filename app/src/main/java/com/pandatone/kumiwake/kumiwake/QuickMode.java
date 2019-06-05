package com.pandatone.kumiwake.kumiwake;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pandatone.kumiwake.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/02.
 */
public class QuickMode extends AppCompatActivity implements TextWatcher {
    AppCompatEditText GpNoEditText, MbNoEditText;
    @SuppressLint("StaticFieldLeak")
    static TextView errorGroup, errorMember, manNoText, womanNoText;
    ImageView backgroundImage;
    SeekBar seekBar;
    CheckBox even_fm_ratio_check, even_person_ratio_check;
    static int memberNo, manNo, womanNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Slide());
        }
        setContentView(R.layout.quick_mode);
        ButterKnife.bind(this);
        findViews();
        even_fm_ratio_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (even_fm_ratio_check.isChecked() == true) {
                    even_person_ratio_check.setEnabled(true);
                } else {
                    even_person_ratio_check.setChecked(false);
                    even_person_ratio_check.setEnabled(false);
                }
            }
        });
        seekBar.setEnabled(false);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        backgroundImage.getLayoutParams().height=screenHeight;
        MbNoEditText.addTextChangedListener(this);
    }

    public void findViews(){
        GpNoEditText = (AppCompatEditText) findViewById(R.id.input_group_no);
        MbNoEditText = (AppCompatEditText) findViewById(R.id.input_member_no);
        errorGroup = (TextView) findViewById(R.id.error_group_no);
        errorMember = (TextView) findViewById(R.id.error_member_no);
        manNoText = (TextView) findViewById(R.id.man_number);
        womanNoText = (TextView) findViewById(R.id.woman_number);
        seekBar = (SeekBar) findViewById(R.id.sex_seek_bar);
        even_fm_ratio_check = (CheckBox) findViewById(R.id.even_fm_ratio_check);
        even_person_ratio_check = (CheckBox) findViewById(R.id.even_person_ratio_check);
        backgroundImage =(ImageView)findViewById(R.id.background_img);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals("")) {
            memberNo = Integer.parseInt(s.toString());
            seekBar.setEnabled(true);
        } else {
            memberNo = 0;
            seekBar.setEnabled(false);
        }
        seekBar.setMax(memberNo);
        seekBar.setProgress(memberNo);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                manNo = progress;
                womanNo = memberNo - progress;
                manNoText.setText(String.valueOf(manNo));
                womanNoText.setText(String.valueOf(womanNo));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @OnClick(R.id.quick_kumiwake_button)
    void onClicked() {
        String group_no = GpNoEditText.getText().toString();
        String member_no = MbNoEditText.getText().toString();

        if (TextUtils.isEmpty(group_no)) {
            errorGroup.setText(R.string.error_empty_group_no);
        }
        if (TextUtils.isEmpty(member_no)) {
            errorMember.setText(R.string.error_empty_member_no);
        } else if (TextUtils.isEmpty(group_no)) {
            errorGroup.setText(R.string.error_empty_group_no);
        } else if (Integer.parseInt(group_no) > memberNo) {
            errorGroup.setText(R.string.number_of_groups_is_much_too);
            errorMember.setText("");
        } else {
            ArrayList<String> womanList = new ArrayList<String>();
            ArrayList<String> manList = CreateManList(manNo, womanNo);
            ArrayList<String> groupList = new ArrayList<String>();
            for (int i = 1; i <= womanNo; i++) {
                womanList.add(getText(R.string.member) + "♡" + String.valueOf(i));
            }
            for (int i = 1; i <= Integer.parseInt(group_no); i++) {
                groupList.add(getText(R.string.group) + " " + String.valueOf(i));
            }
            Intent intent = new Intent(this, QuickKumiwakeConfirmation.class);
            intent.putStringArrayListExtra("QuickModeManList", manList);
            intent.putStringArrayListExtra("QuickModeWomanList", womanList);
            intent.putStringArrayListExtra("QuickModeGroupList", groupList);
            intent.putExtra("EvenFMRatio", even_fm_ratio_check.isChecked());
            intent.putExtra("EvenPersonRatio", even_person_ratio_check.isChecked());
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }

    public ArrayList<String> CreateManList(int manNo, int womanNo) {
        ArrayList<String> manList = new ArrayList<String>();
        if (womanNo == 0) {
            for (int i = 1; i <= manNo; i++) {
                manList.add(getText(R.string.member) + " " + String.valueOf(i));
            }
        } else {
            for (int i = 1; i <= manNo; i++) {
                manList.add(getText(R.string.member) + "♠" + String.valueOf(i));
            }
        }
        return manList;
    }
}
