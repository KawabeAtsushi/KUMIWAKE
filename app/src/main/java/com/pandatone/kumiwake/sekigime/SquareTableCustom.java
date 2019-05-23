package com.pandatone.kumiwake.sekigime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.Name;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/07/15.
 */
public class SquareTableCustom extends AppCompatActivity {
    RadioGroup squareRadioGroup;
    RadioButton doubleDeployButton;
    SeekBar seekBar;
    TextView noText;
    int seatNo = 0, mingroupNo = 1000;
    boolean doubleDeploy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.square_custom);
        ButterKnife.bind(this);
        squareRadioGroup = (RadioGroup) findViewById(R.id.squareGroup);
        doubleDeployButton = (RadioButton) findViewById(R.id.doubleSquareType);
        seekBar = (SeekBar) findViewById(R.id.square_seek_bar);
        noText = (TextView) findViewById(R.id.seat_number);

        if (SekigimeResult.Normalmode) {
            ArrayList<ArrayList<Name>> array = SekigimeResult.arrayArrayNormal;
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).size() < mingroupNo) {
                    mingroupNo = array.get(i).size();
                }
            }
        } else {
            ArrayList<ArrayList<String>> array = SekigimeResult.arrayArrayQuick;
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).size() < mingroupNo) {
                    mingroupNo = array.get(i).size();
                }
            }
        }
        seekBar.setMax(mingroupNo / 4);
        if (mingroupNo < 4) {
            seekBar.setEnabled(false);
        }
        doubleDeployButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (doubleDeployButton.isChecked() == true) {
                    seekBar.setMax(mingroupNo / 4);
                    if (mingroupNo<4) {
                        seekBar.setEnabled(false);
                    }
                } else {
                    seekBar.setMax(mingroupNo / 3);
                    if (mingroupNo<3) {
                        seekBar.setEnabled(false);
                    }else {
                        seekBar.setEnabled(true);
                    }
                }

            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seatNo = progress;
                noText.setText(String.valueOf(seatNo));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @OnClick(R.id.move_result)
    void onClicked() {
        SekigimeResult.square_no = seatNo;
        doubleDeploy = doubleDeployButton.isChecked();
        SekigimeResult.doubleDeploy = doubleDeploy;
        Intent intent = new Intent(getApplicationContext(), SekigimeResult.class);
        startActivity(intent);
    }
}
