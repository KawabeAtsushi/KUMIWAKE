package com.pandatone.kumiwake.sekigime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.customize.CustomDialog;
import com.pandatone.kumiwake.kumiwake.MainActivity;
import com.pandatone.kumiwake.member.Name;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by atsushi_2 on 2016/07/16.
 */
public class SekigimeResult extends AppCompatActivity {

    public static ArrayList<ArrayList<Name>> arrayArrayNormal, arrayArrayNormalMan, arrayArrayNormalWoman;
    public static ArrayList<ArrayList<String>> arrayArrayQuick, arrayArrayQuickMan, arrayArrayQuickWoman;
    public static ArrayList<String> groupArray;
    public static boolean Normalmode, doubleDeploy, fmDeploy;
    public static int square_no, groupNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float scale = getResources().getDisplayMetrics().density;
        RelativeLayout reLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        reLayout.setLayoutParams(param1);
        RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param2.addRule(RelativeLayout.CENTER_IN_PARENT);
        param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        param2.setMargins(param2.leftMargin, param2.topMargin, param2.rightMargin, (int) (12 * scale));
        AdView adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-2315101868638564/8665451539");
        adView.setAdSize(AdSize.BANNER);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build();
        adView.loadAd(adRequest);

        groupNo = groupArray.size();
        if (fmDeploy) {
            convertAlternatelyFmArray();
        }
        final DrawTableView draw = new DrawTableView(this);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        Spinner group_spinner = new Spinner(this);
        LinearLayout layout = new LinearLayout(this);
        Button re_sekigime = new Button(this);
        Button go_home = new Button(this);
        int buttonWidth = (int) (180 * scale);
        int buttonHeight = (int) (50 * scale);
        float centerX = (size.x - buttonWidth) / 2;
        re_sekigime.setText(getText(R.string.re_sekigime));
        go_home.setText(getText(R.string.go_home));
        LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        ViewGroup.MarginLayoutParams mlp1 = llp1;
        ViewGroup.MarginLayoutParams mlp2 = llp2;
        mlp1.setMargins(mlp1.leftMargin, (int) (10 * scale), mlp1.rightMargin, (int) (10 * scale));
        re_sekigime.setLayoutParams(mlp1);
        re_sekigime.setTranslationX(centerX);
        mlp2.setMargins(mlp2.leftMargin, (int) (10 * scale), mlp2.rightMargin, (int) (90 * scale));
        go_home.setLayoutParams(mlp2);
        go_home.setTranslationX(centerX);
        re_sekigime.setBackground(ContextCompat.getDrawable(this, R.drawable.simple_orange_button));
        go_home.setBackground(ContextCompat.getDrawable(this, R.drawable.simple_green_button));
        re_sekigime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation);
                String title = getString(R.string.re_sekigime_title);
                final CustomDialog customDialog = new CustomDialog();
                customDialog.setTitle(title);
                customDialog.setMessage(message);
                customDialog.mPositiveBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < groupNo; i++) {
                            if (Normalmode) {
                                Collections.shuffle(arrayArrayNormal.get(i));
                            } else {
                                Collections.shuffle(arrayArrayQuick.get(i));
                            }
                        }
                        if (fmDeploy) {
                            convertAlternatelyFmArray();
                        }
                        DrawTableView.point = 0;
                        draw.reDraw();
                        Toast.makeText(getApplicationContext(), getText(R.string.re_sekigime_finished), Toast.LENGTH_SHORT).show();
                        customDialog.dismiss();
                    }
                };
                customDialog.show(getFragmentManager(), "Btn");
            }
        });
        go_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackground(ContextCompat.getDrawable(this, R.drawable.sekigime_img));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        List<String> list = new ArrayList<>(); // 新インスタンスを生成
        for (int j = 0; j < groupArray.size(); j++) {
            list.add(groupArray.get(j));
        }
        adapter.addAll(list);
        group_spinner.setAdapter(adapter);
        Spinner.LayoutParams lp = new Spinner.LayoutParams(buttonWidth, buttonHeight);
        group_spinner.setLayoutParams(lp);
        group_spinner.setTranslationY(15 * scale);
        group_spinner.setTranslationX((float) (centerX * 1.1));
        group_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                DrawTableView.point = 0;
                DrawTableView.position = position;
                draw.reDraw();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        ScrollView scrollView = new ScrollView(this);
        reLayout.addView(scrollView);
        reLayout.addView(adView, param2);
        layout.addView(group_spinner);
        layout.addView(draw);
        layout.addView(re_sekigime);
        layout.addView(go_home);
        scrollView.addView(layout);
        setContentView(reLayout);
    }

    public void convertAlternatelyFmArray() {
        float manArrayNo, womanArrayNo, bigger, smaller, addNo, remainder;
        int i, j, k, a, memberSum;
        if (Normalmode) {
            createFmArrayForNormal();
            ArrayList<ArrayList<Name>> smallerArray, biggerArray;
            arrayArrayNormal = new ArrayList<>(groupNo);
            for (int g = 0; g < groupNo; g++) {
                arrayArrayNormal.add(new ArrayList<Name>());
            }
            for (i = 0; i < groupNo; i++) {
                manArrayNo = arrayArrayNormalMan.get(i).size();
                womanArrayNo = arrayArrayNormalWoman.get(i).size();
                if (manArrayNo < womanArrayNo) {
                    bigger = womanArrayNo;
                    smaller = manArrayNo;
                    biggerArray = arrayArrayNormalWoman;
                    smallerArray = arrayArrayNormalMan;
                } else {
                    bigger = manArrayNo;
                    smaller = womanArrayNo;
                    biggerArray = arrayArrayNormalMan;
                    smallerArray = arrayArrayNormalWoman;
                }
                if (smaller != 0) {
                    addNo = bigger / smaller;
                    remainder = (bigger % smaller) - 1;
                    memberSum = 0;
                    for (a = 0; a < addNo / 2; a++) {
                        arrayArrayNormal.get(i).add(biggerArray.get(i).get(memberSum));
                        memberSum++;
                    }
                    arrayArrayNormal.get(i).add(smallerArray.get(i).get(0));
                    for (j = 1; j < smaller; j++) {
                        for (k = 0; k < addNo - 1; k++) {
                            arrayArrayNormal.get(i).add(biggerArray.get(i).get(memberSum));
                            memberSum++;
                        }
                        if (remainder != 0) {
                            arrayArrayNormal.get(i).add(biggerArray.get(i).get(memberSum));
                            memberSum++;
                            remainder--;
                        }
                        arrayArrayNormal.get(i).add(smallerArray.get(i).get(j));
                    }
                    for (; a < addNo; a++) {
                        arrayArrayNormal.get(i).add(biggerArray.get(i).get(memberSum));
                        memberSum++;
                    }
                } else {
                    for (k = 0; k < bigger; k++) {
                        arrayArrayNormal.get(i).add(biggerArray.get(i).get(k));
                    }
                }

            }
        } else {
            createFmArrayForQuick();
            ArrayList<ArrayList<String>> smallerArray, biggerArray;
            arrayArrayQuick = new ArrayList<>(groupNo);
            for (int g = 0; g < groupNo; g++) {
                arrayArrayQuick.add(new ArrayList<String>());
            }
            for (i = 0; i < groupNo; i++) {
                manArrayNo = arrayArrayQuickMan.get(i).size();
                womanArrayNo = arrayArrayQuickWoman.get(i).size();
                if (manArrayNo < womanArrayNo) {
                    bigger = womanArrayNo;
                    smaller = manArrayNo;
                    biggerArray = arrayArrayQuickWoman;
                    smallerArray = arrayArrayQuickMan;
                } else {
                    bigger = manArrayNo;
                    smaller = womanArrayNo;
                    biggerArray = arrayArrayQuickMan;
                    smallerArray = arrayArrayQuickWoman;
                }
                if (smaller != 0) {
                    addNo = bigger / smaller;
                    remainder = (bigger % smaller) - 1;
                    memberSum = 0;
                    for (a = 0; a < addNo / 2; a++) {
                        arrayArrayQuick.get(i).add(biggerArray.get(i).get(memberSum));
                        memberSum++;
                    }
                    arrayArrayQuick.get(i).add(smallerArray.get(i).get(0));
                    for (j = 1; j < smaller; j++) {
                        for (k = 0; k < addNo - 1; k++) {
                            arrayArrayQuick.get(i).add(biggerArray.get(i).get(memberSum));
                            memberSum++;
                        }
                        if (remainder != 0) {
                            arrayArrayQuick.get(i).add(biggerArray.get(i).get(memberSum));
                            memberSum++;
                            remainder--;
                        }
                        arrayArrayQuick.get(i).add(smallerArray.get(i).get(j));
                    }
                    for (; a < addNo; a++) {
                        arrayArrayQuick.get(i).add(biggerArray.get(i).get(memberSum));
                        memberSum++;
                    }

                } else {
                    for (k = 0; k < bigger; k++) {
                        arrayArrayQuick.get(i).add(biggerArray.get(i).get(k));
                    }
                }
            }

        }
    }

    public void createFmArrayForNormal() {
        Name item;
        arrayArrayNormalMan = new ArrayList<>(groupNo);
        arrayArrayNormalWoman = new ArrayList<>(groupNo);
        for (int g = 0; g < groupNo; g++) {
            arrayArrayNormalMan.add(new ArrayList<Name>());
            arrayArrayNormalWoman.add(new ArrayList<Name>());
        }
        for (int i = 0; i < arrayArrayNormal.size(); i++) {
            for (int j = 0; j < arrayArrayNormal.get(i).size(); j++) {
                item = arrayArrayNormal.get(i).get(j);
                if (item.getSex().equals(getText(R.string.man))) {
                    arrayArrayNormalMan.get(i).add(item);
                } else {
                    arrayArrayNormalWoman.get(i).add(item);
                }
            }
        }
    }

    public void createFmArrayForQuick() {
        String item;
        arrayArrayQuickMan = new ArrayList<>(groupNo);
        arrayArrayQuickWoman = new ArrayList<>(groupNo);
        for (int g = 0; g < groupNo; g++) {
            arrayArrayQuickMan.add(new ArrayList<String>());
            arrayArrayQuickWoman.add(new ArrayList<String>());
        }
        for (int i = 0; i < arrayArrayQuick.size(); i++) {
            for (int j = 0; j < arrayArrayQuick.get(i).size(); j++) {
                item = arrayArrayQuick.get(i).get(j);
                if (item.matches(".*" + "♠" + ".*")) {
                    arrayArrayQuickMan.get(i).add(item);
                } else {
                    arrayArrayQuickWoman.get(i).add(item);
                }
            }
        }
    }
}