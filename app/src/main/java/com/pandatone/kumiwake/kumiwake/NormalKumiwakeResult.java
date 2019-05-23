package com.pandatone.kumiwake.kumiwake;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.customize.CustomDialog;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;
import com.pandatone.kumiwake.member.Name;
import com.pandatone.kumiwake.sekigime.SekigimeResult;
import com.pandatone.kumiwake.sekigime.SelectTableType;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/10.
 */
public class NormalKumiwakeResult extends AppCompatActivity {

    ArrayList<Name> memberArray, manArray, womanArray, resultArray;
    ArrayList<GroupListAdapter.Group> groupArray;
    ArrayList<ArrayList<Name>> arrayArray;
    static boolean even_fm_ratio;
    static boolean even_age_ratio;
    static boolean even_grade_ratio;
    String even_role;         //均等にするRole
    int groupCount, memberSize, memberSum = 0, v = 0, nowGroupNo = 0;
    private Timer timer;
    TimerTask timerTask;

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
        memberArray = (ArrayList<Name>) i.getSerializableExtra("NormalModeMemberArray");
        groupArray = (ArrayList<GroupListAdapter.Group>) i.getSerializableExtra("NormalModeGroupArray");
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false);
        even_age_ratio = i.getBooleanExtra("EvenAgeRatio", false);
        even_grade_ratio = i.getBooleanExtra("EvenGradeRatio", false);
        even_role = i.getStringExtra("EvenRole");
        groupCount = groupArray.size();
        memberSize = memberArray.size();

        startMethod();

        if (!KumiwakeSelectMode.sekigime) {
            timer = new Timer();
            timerTask = new MyTimerTask(this);
            timer.scheduleAtFixedRate(timerTask, 100, 100);
        } else {
            ArrayList<String> groupNameArray = new ArrayList<>(groupCount);
            for (int j = 0; j < groupCount; j++) {
                groupNameArray.add(groupArray.get(j).getGroup());
                Collections.shuffle(arrayArray.get(j));
            }
            Intent intent = new Intent(this, SelectTableType.class);
            SekigimeResult.groupArray = groupNameArray;
            SekigimeResult.arrayArrayNormal = arrayArray;
            startActivity(intent);
            finish();
        }

        final ScrollView scrollView = (ScrollView) findViewById(R.id.kumiwake_scroll);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (int i = 0; i < groupCount; i++) {
            outState.putSerializable("ARRAY" + String.valueOf(i), arrayArray.get(i));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        arrayArray = new ArrayList<>(groupCount);
        for (int g = 0; g < groupCount; g++) {
            arrayArray.add((ArrayList<Name>) savedInstanceState.getSerializable("ARRAY" + String.valueOf(g)));
        }
    }


    public void startMethod() {
        Collections.shuffle(memberArray);

        arrayArray = new ArrayList<>(groupCount);
        for (int g = 0; g < groupCount; g++) {
            arrayArray.add(new ArrayList<Name>());
        }

        if (even_fm_ratio) {
            CreateFmArray();    //男女それぞれの配列を作成
        }

        if (!even_fm_ratio && !even_age_ratio && !even_grade_ratio) {
            kumiwakeAll();
        } else if (even_fm_ratio && !even_age_ratio && !even_grade_ratio) {
            EvenKumiwakeSetter(manArray,womanArray, "");
            EvenCreateGroup(manArray);
            EvenCreateGroup(womanArray);
        } else if (!even_fm_ratio && even_age_ratio && !even_grade_ratio) {
            EvenKumiwakeSetter(memberArray,null, "age");
            EvenCreateGroup(memberArray);
        } else if (!even_fm_ratio && !even_age_ratio && even_grade_ratio) {
            EvenKumiwakeSetter(memberArray,null, "grade");
            EvenCreateGroup(memberArray);
        } else if (even_fm_ratio && even_age_ratio && !even_grade_ratio) {
            EvenKumiwakeSetter(manArray,womanArray, "age");
            EvenCreateGroup(manArray);
            EvenCreateGroup(womanArray);
        } else if (even_fm_ratio && !even_age_ratio && even_grade_ratio) {
            EvenKumiwakeSetter(manArray,womanArray, "grade");
            EvenCreateGroup(manArray);
            EvenCreateGroup(womanArray);
        }
    }


    public void addGroupView() {
        if (v < groupCount) {
            resultArray = arrayArray.get(v);
            Collections.sort(resultArray, new KumiwakeLeaderComparator());
            addView(resultArray, v);
            v++;
        }
        if (v == groupCount) {
            timer.cancel();
        }
    }

    @OnClick(R.id.re_kumiwake)
    void onReKumiwake() {
        memberSum = 0;
        v = 0;
        nowGroupNo = 0;
        timerTask = new MyTimerTask(this);
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
                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 100, 100);
                customDialog.dismiss();
                Toast.makeText(getApplicationContext(), getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show();
            }
        };
        customDialog.show(getFragmentManager(), "Btn");
    }

    @OnClick(R.id.go_sekigime)
    void onClicked() {
        ArrayList<String> groupNameArray = new ArrayList<>(groupCount);
        for (int j = 0; j < groupCount; j++) {
            groupNameArray.add(groupArray.get(j).getGroup());
            Collections.shuffle(arrayArray.get(j));
        }
        Intent intent = new Intent(this, SelectTableType.class);
        SekigimeResult.groupArray = groupNameArray;
        SekigimeResult.arrayArrayNormal = arrayArray;
        startActivity(intent);
    }

    @OnClick(R.id.go_home)
    void onClickedHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void kumiwakeAll() {
        setLeader(memberArray);
        setRole(memberArray);

        for (int i = 0; i < groupCount; i++) {
            int belongNo = groupArray.get(i).getBelongNo() - arrayArray.get(i).size();  //グループの規定人数－グループの現在数
            resultArray = kumiwakeCreateGroup(memberArray, belongNo);
            for (int j = 0; j < resultArray.size(); j++) {
                arrayArray.get(i).add(resultArray.get(j));
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //　　　　　　　　　　　　　　　　　　　　　　　　各種処理メソッド                                                           ///
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRole(ArrayList<Name> array) {
        int addGroupNo, initialGroupNo, nowGroupMemberCount = 0;

        if (array != womanArray) {
            Random r = new Random();
            nowGroupNo = r.nextInt(groupCount);  //r.nextInt(n) は0～n-1の値をnowGroupNoに代入
        }

        initialGroupNo = nowGroupNo;

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getRole() != null) {
                if (array.get(i).getRole().matches(".*" + even_role + ".*") && !array.get(i).getRole().matches(".*" + getText(R.string.leader) + ".*")) {
                    addGroupNo = nowGroupNo % groupCount;
                    nowGroupMemberCount = arrayArray.get(addGroupNo).size();

                    //一週目かつリーダーが指定役職かつメンバー数が規定人数以下
                    while (((nowGroupNo - initialGroupNo) < groupCount
                            && nowGroupMemberCount != 0
                            && arrayArray.get(addGroupNo).get(nowGroupMemberCount - 1).getRole().matches(".*" + even_role + ".*"))
                            || groupArray.get(addGroupNo).getBelongNo() == nowGroupMemberCount) {
                        nowGroupNo++;
                        addGroupNo = nowGroupNo % groupCount;
                        nowGroupMemberCount = arrayArray.get(addGroupNo).size();
                    }

                    nowGroupMemberCount = arrayArray.get(addGroupNo).size();

                    while (groupArray.get(addGroupNo).getBelongNo() == nowGroupMemberCount) {
                        nowGroupNo++;
                        addGroupNo = nowGroupNo % groupCount;
                        nowGroupMemberCount = arrayArray.get(addGroupNo).size();
                    }

                    arrayArray.get(addGroupNo).add(array.get(i));
                    nowGroupNo++;
                }
            }
        }
    }

    public void setLeader(ArrayList<Name> array) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getRole() != null) {
                if (array.get(i).getRole().matches(".*" + getText(R.string.leader) + ".*")) {
                    String[] roleArray = array.get(i).getRole().split(",");
                    List<String> list = new ArrayList<String>(Arrays.asList(roleArray));
                    list.remove("");
                    Collections.sort(list);
                    int LDNo = Integer.parseInt(list.get(0).substring(2));
                    arrayArray.get(LDNo - 1).add(array.get(i));
                }
            }
        }
    }

    public void CreateFmArray() {
        manArray = new ArrayList<Name>();
        womanArray = new ArrayList<Name>();

        for (int i = 0; i < memberArray.size(); i++) {
            if (memberArray.get(i).getSex().equals(getText(R.string.man))) {
                manArray.add(memberArray.get(i));
            }
        }

        for (int j = 0; j < memberArray.size(); j++) {
            if (memberArray.get(j).getSex().equals(getText(R.string.woman))) {
                womanArray.add(memberArray.get(j));
            }
        }
    }

    public void EvenKumiwakeSetter(ArrayList<Name> array1,ArrayList<Name> array2, String sortCode) {

        if (!sortCode.equals("")) {
            Collections.shuffle(array1);
            Collections.sort(array1, new KumiwakeNumberComparator(sortCode));
        }
        if (!sortCode.equals("")&&array2!=null) {
            Collections.shuffle(array2);
            Collections.sort(array2, new KumiwakeNumberComparator(sortCode));
        }
        setLeader(array1);
        if (array2!=null) {setLeader(array2);}
        setRole(array1);
        if (array2!=null) {setRole(array2);}
    }

    public ArrayList<Name> kumiwakeCreateGroup(ArrayList<Name> array, int belongNo) {
        ArrayList<Name> result = new ArrayList<Name>();

        for (int j = 0; j < belongNo; j++) {
            if (memberSum < array.size()) {
                while (array.get(memberSum).getRole().matches(".*" + getText(R.string.leader) + ".*")) {
                    memberSum++;
                }
                while (memberSum != array.size() && array.get(memberSum).getRole().matches(".*" + even_role + ".*")) {
                    memberSum++;
                }
                if (memberSum < array.size()) {
                    result.add(array.get(memberSum));
                    memberSum++;
                }
            }
        }
        return result;
    }

    public void EvenCreateGroup(ArrayList<Name> array) {
        int groupCapacity[] = new int[groupCount], addGroupNo = 0, nowGroupMemberCount, trueCount = 0;

        if (array == manArray) {
            double groupCapacity0[] = new double[groupCount], max;
            int addSum = 0, maxJ = 0;

            for (int i = 0; i < groupCount; i++) {
                groupCapacity0[i] = ((double) groupArray.get(i).getBelongNo() / memberArray.size()) * array.size();
                groupCapacity[i] = (int) groupCapacity0[i];    //型変換の際に小数点以下は切り捨てられる
                addSum += groupCapacity[i];
                groupCapacity0[i] = groupCapacity0[i] - groupCapacity[i];    //小数点以下の大きさの配列に直す
            }

            for (int k = 0; k < groupCount; k++) {
                while (groupCapacity[k] < arrayArray.get(k).size()) {
                    groupCapacity[k]++;  //すでにある要素数より許容格納数が小さいなら＋１する
                    addSum++;
                }
                if (groupCapacity[k] == 0) {
                    groupCapacity[k]++;  //許容格納数が０なら＋１する
                    addSum++;
                }
            }

            while (addSum < array.size()) {
                max = 0;
                for (int j = 0; j < groupCount; j++) {
                    if (groupCapacity0[j] > max) {
                        groupCapacity0[j] = max;
                        maxJ = j;
                    }
                }
                groupCapacity[maxJ]++;  //小数点以下が大きい要素から順に許容格納数を＋１する
                groupCapacity0[maxJ]--; //同じ要素を連続でとらないように-１する
                addSum++;
            }
        }

        boolean fullNo[] = new boolean[groupCount];  //要素数が許容格納数に達しているグループはtrue
        memberSum = 0;
        nowGroupNo = 0;
        nowGroupMemberCount = arrayArray.get(0).size();

        while (memberSum < array.size()) {

            while (memberSum < array.size() && (array.get(memberSum).getRole().matches(".*" + getText(R.string.leader) + ".*")
                    || array.get(memberSum).getRole().matches(".*" + even_role + ".*"))) {
                memberSum++;
            }

            if (array == manArray) {

                int roopCount = 0, min = 5000, minJ = 0;

                while (memberSum < array.size() &&
                        (groupCapacity[addGroupNo] == nowGroupMemberCount || fullNo[addGroupNo])) {
                    nowGroupNo++;
                    addGroupNo = nowGroupNo % groupCount;
                    nowGroupMemberCount = 0;

                    if (!fullNo[addGroupNo]) {
                        for (int i = 0; i < arrayArray.get(addGroupNo).size(); i++) {
                            if (arrayArray.get(addGroupNo).get(i).getSex().equals(getText(R.string.man))) {
                                nowGroupMemberCount++;
                            }
                        }

                        if (roopCount > groupCount) {  //一周してもループを抜けない場合
                            for (int j = 0; j < groupCount; j++) {
                                if (!fullNo[j] && arrayArray.get(j).size() < min) {
                                    min = arrayArray.get(j).size();
                                    minJ = j;
                                }
                            }
                            addGroupNo = minJ;  //要素数が許容格納数に達していないグループに追加（groupCapacityは達している）
                            break;             //ループを抜ける
                        }
                    }

                    if (groupArray.get(addGroupNo).getBelongNo() == arrayArray.get(addGroupNo).size()) {
                        fullNo[addGroupNo] = true;
                    }
                    roopCount++;
                }
            } else {
                while (memberSum < array.size() && groupArray.get(addGroupNo).getBelongNo() == nowGroupMemberCount) {
                    nowGroupNo++;
                    addGroupNo = nowGroupNo % groupCount;
                    nowGroupMemberCount = arrayArray.get(addGroupNo).size();
                }
            }

            if (memberSum < array.size()) {
                arrayArray.get(addGroupNo).add(array.get(memberSum));
                memberSum++;
                nowGroupNo++;
                addGroupNo = nowGroupNo % groupCount;
                nowGroupMemberCount = arrayArray.get(addGroupNo).size();
            }
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////                                 描画メソッド                                             ////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addView(ArrayList<Name> resultArray, int i) {
        TextView groupName;
        ListView arrayList;
        LinearLayout layout = (LinearLayout) findViewById(R.id.result_layout);
        View v = getLayoutInflater().inflate(R.layout.result_parts, null);
        if (i == 0) {
            layout.removeAllViews();
        }
        layout.addView(v);
        groupName = (TextView) v.findViewById(R.id.result_group);
        groupName.setText(groupArray.get(i).getGroup());
        arrayList = (ListView) v.findViewById(R.id.result_member_listView);
        MBListViewAdapter adapter = new MBListViewAdapter(this, resultArray, 2000);
        arrayList.setAdapter(adapter);
        setBackGround(v);
        MBListViewAdapter.setRowHeight(arrayList, adapter);
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

/////////////////////////////////////////////////////////////////////////////////////////////////////////
////                                   補助処理メソッド                                              ////
/////////////////////////////////////////////////////////////////////////////////////////////////////////

class MyTimerTask extends TimerTask {
    private Handler handler;
    private Context context;

    public MyTimerTask(Context context) {
        handler = new Handler();
        this.context = context;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((NormalKumiwakeResult) context).addGroupView();
            }
        });
    }

}

class KumiwakeLeaderComparator implements Comparator<Name> {
    NormalKumiwakeResult A;

    public int compare(Name n1, Name n2) {
        int value = 0;
        String leader = ".*" + MainActivity.getContext().getText(R.string.leader) + ".*";

        if (n1.getRole() != null && n2.getRole() != null) {
            if (n1.getRole().matches(leader) && !n2.getRole().matches(leader)) {
                value = -1;
            } else if (n2.getRole().matches(leader) && !n1.getRole().matches(leader)) {
                value = 1;
            }

            if (n1.getRole().matches(leader) && n2.getRole().matches(leader)) {
                String[] roleArray1 = n1.getRole().split(","), roleArray2 = n2.getRole().split(",");
                List<String> list1 = new ArrayList<String>(Arrays.asList(roleArray1)), list2 = new ArrayList<String>(Arrays.asList(roleArray2));
                list1.remove("");
                list2.remove("");
                Collections.sort(list1);
                Collections.sort(list2);
                int LDNo1 = Integer.parseInt(list1.get(0).substring(2)), LDNo2 = Integer.parseInt(list2.get(0).substring(2));
                if (LDNo1 < LDNo2) {
                    value = -1;
                } else {
                    value = 1;
                }
            }
        }

        if (value == 0) {
            String n1_sex = n1.getSex();
            String n2_sex = n2.getSex();

            value = n2_sex.compareTo(n1_sex);
        }

        if (A.even_age_ratio && !A.even_grade_ratio) {
            if (value == 0) {
                int n1_age = n1.getAge();
                int n2_age = n2.getAge();
                if (n1_age < n2_age) {
                    value = -1;
                } else if (n1_age > n2_age) {
                    value = 1;
                } else {
                    value = 0;
                }
            }
        }

        if (!A.even_age_ratio && A.even_grade_ratio) {
            if (value == 0) {
                int n1_grade = n1.getGrade();
                int n2_grade = n2.getGrade();
                if (n1_grade < n2_grade) {
                    value = -1;
                } else {
                    value = 1;
                }
            }
        }

        return value;
    }
}

class KumiwakeNumberComparator implements Comparator<Name> {
    String sortCode;

    KumiwakeNumberComparator(String sortCode) {
        this.sortCode = sortCode;
    }

    public int compare(Name n1, Name n2) {
        int value = 0;

        if (sortCode.equals("age")) {
            int n1_age = n1.getAge();
            int n2_age = n2.getAge();
            if (n1_age < n2_age) {
                value = -1;
            } else if (n1_age > n2_age) {
                value = 1;
            } else {
                value = 0;
            }
        }


        if (sortCode.equals("grade")) {
            int n1_grade = n1.getGrade();
            int n2_grade = n2.getGrade();
            if (n1_grade < n2_grade) {
                value = -1;
            } else {
                value = 1;
            }
        }

        return value;
    }
}

