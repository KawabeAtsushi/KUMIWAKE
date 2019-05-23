package com.pandatone.kumiwake.kumiwake;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.customize.CustomDialog;
import com.pandatone.kumiwake.sekigime.SekigimeResult;
import com.pandatone.kumiwake.sekigime.SelectTableType;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/10.
 */
public class QuickKumiwakeResult extends AppCompatActivity {

    ArrayList<String> memberArray, manArray, womanArray, groupArray, resultArray;
    static boolean even_fm_ratio;
    static boolean even_person_ratio;
    int groupNo;
    ArrayList<ArrayList<String>> arrayArray;
    RelativeLayout viewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kumiwake_result);
        ButterKnife.bind(this);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2315101868638564/8665451539");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build();
        mAdView.loadAd(adRequest);
        Intent i = getIntent();
        memberArray = i.getStringArrayListExtra("QuickModeMemberList");
        manArray = i.getStringArrayListExtra("QuickModeManList");
        womanArray = i.getStringArrayListExtra("QuickModeWomanList");
        groupArray = i.getStringArrayListExtra("QuickModeGroupList");
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false);
        even_person_ratio = i.getBooleanExtra("EvenPersonRatio", false);
        groupNo = groupArray.size();
        viewGroup = (RelativeLayout) findViewById(R.id.result_view);
        viewGroup.setBackground(ContextCompat.getDrawable(this, R.drawable.quick_img));

        startMethod();

        if (!KumiwakeSelectMode.sekigime) {
            for (int v = 0; v < groupNo; v++) {
                resultArray = arrayArray.get(v);
                Collections.sort(resultArray, new KumiwakeComparator());
                addView(resultArray, v);
            }
        } else {
            for (int v = 0; v < groupNo; v++) {
                resultArray = arrayArray.get(v);
                Collections.shuffle(resultArray);
            }
            Intent intent = new Intent(this, SelectTableType.class);
            SekigimeResult.groupArray = groupArray;
            SekigimeResult.arrayArrayQuick = arrayArray;
            startActivity(intent);
            finish();
        }

        final ScrollView scrollView=(ScrollView)findViewById(R.id.kumiwake_scroll);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                }});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for(int i=0;i<groupNo;i++){
            outState.putStringArrayList("ARRAY" + String.valueOf(i), arrayArray.get(i));}
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        arrayArray = new ArrayList<>(groupNo);
        for (int g = 0; g < groupNo; g++) {
            arrayArray.add(savedInstanceState.getStringArrayList("ARRAY" + String.valueOf(g)));
        }
    }


    public void startMethod() {
        arrayArray = new ArrayList<>(groupNo);

        for (int g = 0; g < groupNo; g++) {
            arrayArray.add(new ArrayList<String>());
        }

        if (even_fm_ratio == false) {
            kumiwakeAll();
        } else if (even_person_ratio == false) {
            kumiwakeFm(0);
        } else {
            kumiwakeFm(manArray.size() % groupNo);
        }
    }

    @OnClick(R.id.re_kumiwake)
    void onReKumiwake() {
        String title = getString(R.string.re_kumiwake_title);
        String message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation);
        final CustomDialog customDialog = new CustomDialog();
        customDialog.setTitle(title);
        customDialog.setMessage(message);
        CustomDialog.mPositiveBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                ScrollView scrollView = (ScrollView) findViewById(R.id.kumiwake_scroll);
                                scrollView.scrollTo(0, 0);
                                startMethod();
                                for (int i = 0; i < groupNo; i++) {
                                    resultArray = arrayArray.get(i);
                                    Collections.sort(resultArray, new KumiwakeComparator());
                                    addView(resultArray, i);
                                }
                                customDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show();
                            }
                        };
        customDialog.show(getFragmentManager(), "Btn");
    }

    @OnClick(R.id.go_sekigime)
    void onClicked() {
        for (int v = 0; v < groupNo; v++) {
            resultArray = arrayArray.get(v);
            Collections.shuffle(resultArray);
        }
        Intent intent = new Intent(this, SelectTableType.class);
        SekigimeResult.groupArray = groupArray;
        SekigimeResult.arrayArrayQuick = arrayArray;
        startActivity(intent);
    }

    @OnClick(R.id.go_home)
    void onClickedHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void kumiwakeAll() {
        Collections.shuffle(memberArray);
        int remainder;

        for (int i = 0; i < memberArray.size(); i++) {
            remainder = i % groupNo;
            arrayArray.get(remainder).add(memberArray.get(i));
        }
    }

    public void kumiwakeFm(int firstPos) {
        Collections.shuffle(manArray);
        Collections.shuffle(womanArray);
        int manRemainder, womanRemainder;

        for (int i = 0; i < manArray.size(); i++) {
            manRemainder = i % groupNo;
            arrayArray.get(manRemainder).add(manArray.get(i));
        }

        for (int i = 0; i < womanArray.size(); i++) {
            womanRemainder = (i + firstPos) % groupNo;
            arrayArray.get(womanRemainder).add(womanArray.get(i));
        }
    }


    public void addView(ArrayList<String> resultArray, int i) {
        TextView groupName;
        ListView arrayList;
        LinearLayout layout = (LinearLayout) findViewById(R.id.result_layout);
        View v = getLayoutInflater().inflate(R.layout.result_parts, null);
        if (i == 0) {
            layout.removeAllViews();
        }
        layout.addView(v);
        groupName = (TextView) v.findViewById(R.id.result_group);
        groupName.setText(getText(R.string.group) + " " + String.valueOf(i + 1));
        arrayList = (ListView) v.findViewById(R.id.result_member_listView);
        ArrayAdapter<String> adapter = new MemberArrayAdapter(this, R.layout.mini_row_member, resultArray, false);
        arrayList.setAdapter(adapter);
        setBackGround(v);
        QuickKumiwakeConfirmation.setRowHeight(arrayList, adapter);
    }

    public LinearLayout.LayoutParams setMargin(int leftDp, int topDp, int rightDp, int bottomDp) {
        float scale = getResources().getDisplayMetrics().density; //画面のdensityを指定。
        int left = (int) (leftDp * scale + 0.5f);
        int top = (int) (topDp * scale + 0.5f);
        int right = (int) (rightDp * scale + 0.5f);
        int bottom = (int) (bottomDp * scale + 0.5f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(left, top, right, bottom);
        return layoutParams;
    }


    public void setBackGround(View v) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.mutate();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(25);
        int R = 0, G = 0, B = 0;
        while (R < 200 && G < 200 && B < 200) {
            R = (int) ((Math.random() * 0.5 + 0.5) * 256);
            G = (int) ((Math.random() * 0.5 + 0.5) * 256);
            B = (int) ((Math.random() * 0.5 + 0.5) * 256);
        }

        drawable.setColor(Color.argb(150, R, G, B));
        v.setLayoutParams(setMargin(10, 12, 10, 0));
        v.setBackgroundDrawable(drawable);

    }
}


class KumiwakeComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
        String s1_name = s1.substring(0, 5);
        String s2_name = s2.substring(0, 5);

        int value = s1_name.compareTo(s2_name);
        if (value == 0) {
            int s1_no = Integer.parseInt(s1.substring(5));
            int s2_no = Integer.parseInt(s2.substring(5));
            if (s1_no < s2_no) {
                value = -1;
            } else {
                value = 1;
            }
        }
        return value;
    }
}

