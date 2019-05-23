package com.pandatone.kumiwake.kumiwake;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.GPListViewAdapter;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;
import com.pandatone.kumiwake.member.Name;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/08.
 */
public class NormalKumiwakeConfirmation extends AppCompatActivity {
    ArrayList<Name> memberArray;
    ArrayList<GroupListAdapter.Group> groupArray;
    MBListViewAdapter memberAdapter;
    GPListViewAdapter groupAdapter;
    boolean even_fm_ratio, even_leader_ratio, even_age_ratio, even_grade_ratio;
    String even_role;
    static ListView memberList, groupList;
    static TextView customReview, memberNo, groupNo,title,betweenArrows;
    android.support.v7.widget.AppCompatButton runButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kumiwake_confirmation);
        ButterKnife.bind(this);
        Intent i = getIntent();
        memberArray = (ArrayList<Name>) i.getSerializableExtra("NormalModeMemberArray");
        groupArray = (ArrayList<GroupListAdapter.Group>) i.getSerializableExtra("NormalModeGroupArray");
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false);
        even_leader_ratio = i.getBooleanExtra("EvenLeaderRatio", false);
        even_age_ratio = i.getBooleanExtra("EvenAgeRatio", false);
        even_grade_ratio = i.getBooleanExtra("EvenGradeRatio", false);
        even_role = i.getStringExtra("EvenRole");
        findViews();
        setAdapter();
        setViews();
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_2);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        });
    }

    protected void findViews() {
        memberList = (ListView) findViewById(R.id.kumiwake_member_listView);
        groupList = (ListView) findViewById(R.id.kumiwake_group_listView);
        memberNo = (TextView) findViewById(R.id.member_no);
        groupNo = (TextView) findViewById(R.id.group_no);
        customReview = (TextView) findViewById(R.id.custom_review);
        title=(TextView)findViewById(R.id.confirmation_title);
        betweenArrows=(TextView)findViewById(R.id.between_arrows);
        runButton=(android.support.v7.widget.AppCompatButton)findViewById(R.id.kumiwake_button);
        if(KumiwakeSelectMode.sekigime){
            title.setText(R.string.sekigime_confirm);
            betweenArrows.setText("SEKIGIME");
            runButton.setText(R.string.go_select_seats_type);
        }
        memberNo.setText(String.valueOf(memberArray.size()) + " " + getText(R.string.person)
                + "(" + getText(R.string.man) + ":" + String.valueOf(countManNo()) + getText(R.string.person)
                + "," + getText(R.string.woman) + ":" + String.valueOf(memberArray.size() - countManNo()) + getText(R.string.person) + ")");
        groupNo.setText(String.valueOf(groupArray.size()) + " " + getText(R.string.group));
    }

    public int countManNo() {
        int manNo = 0;
        for (int i = 0; i < memberArray.size(); i++) {
            if (memberArray.get(i).getSex().equals(getText(R.string.man))) {
                manNo++;
            }
        }
        return manNo;
    }

    public void setViews() {
        StringBuilder custom_text = new StringBuilder();
        String[] array_str = getResources().getStringArray(R.array.role);

        if (even_fm_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n");
        }
        if (even_age_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_age_ratio) + "\n");
        }
        if (even_grade_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_grade_ratio) + "\n");
        }
        if (!even_role.equals(array_str[0])) {
            custom_text.append("☆" + even_role + getText(R.string.even_out) + "\n");
        }
        customReview.setText(custom_text.toString());

        MBListViewAdapter.setRowHeight(memberList, memberAdapter);
        GPListViewAdapter.setRowHeight(groupList, groupAdapter);
    }

    @OnClick(R.id.kumiwake_button)
    void onClicked() {
        Intent intent = new Intent(this, NormalKumiwakeResult.class);
        intent.putExtra("NormalModeMemberArray", memberArray);
        intent.putExtra("NormalModeGroupArray", groupArray);
        intent.putExtra("EvenFMRatio", even_fm_ratio);
        intent.putExtra("EvenLeaderRatio", even_leader_ratio);
        intent.putExtra("EvenAgeRatio", even_age_ratio);
        intent.putExtra("EvenGradeRatio", even_grade_ratio);
        intent.putExtra("EvenRole", even_role);
        startActivity(intent);
    }

    protected void setAdapter() {
        Collections.sort(memberArray, new KumiwakeLeaderComparator());
        memberAdapter = new MBListViewAdapter(this, memberArray, 0);
        groupAdapter = new GPListViewAdapter(this, groupArray);
        memberList.setAdapter(memberAdapter);
        groupList.setAdapter(groupAdapter);
    }
}

