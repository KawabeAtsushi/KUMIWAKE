package com.pandatone.kumiwake.kumiwake;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.EditGroupListAdapter;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;
import com.pandatone.kumiwake.member.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/27.
 */
public class KumiwakeCustom extends AppCompatActivity {

    public static ListView memberList, groupList;
    static TextView numberOfSelectedMember, groupNo;
    Button addMember;
    ImageView backgroundImage;
    Spinner spinner;
    public static ScrollView scrollView;
    CheckBox even_fm_ratio_check, even_age_ratio_check, even_grade_ratio_check;
    MBListViewAdapter mbAdapter;
    EditGroupListAdapter gpAdapter;
    public static ArrayList<Name> memberArray, origMemberArray;
    static ArrayList<GroupListAdapter.Group> groupArray, newGroupArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.kumiwake_custom);
        ButterKnife.bind(this);

        memberArray = (ArrayList<Name>) getIntent().getSerializableExtra("NormalModeMemberArray");
        groupArray = (ArrayList<GroupListAdapter.Group>) getIntent().getSerializableExtra("NormalModeGroupArray");
        mbAdapter = new MBListViewAdapter(this, memberArray, groupArray.size());
        gpAdapter = new EditGroupListAdapter(this, groupArray);
        findViews();
        setViews();
        memberList.setAdapter(mbAdapter);
        groupList.setAdapter(gpAdapter);
        setLeader();

        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                memberList.requestFocus();
                changeLeader(position);
            }
        });

        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        origMemberArray = memberArray;
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*memberArray = origMemberArray;
        mbAdapter = new MBListViewAdapter(this, memberArray, groupArray.size());
        setViews();
        memberList.setAdapter(mbAdapter);
        setLeader();*/


    }

    public void findViews() {
        scrollView = (ScrollView) findViewById(R.id.custom_scroll);
        memberList = (ListView) findViewById(R.id.add_group_listview).findViewById(R.id.reviewListView);
        numberOfSelectedMember = (TextView) findViewById(R.id.add_group_listview).findViewById(R.id.numberOfSelectedMember);
        memberList.setEmptyView(findViewById(R.id.add_group_listview).findViewById(R.id.emptyMemberList));
        addMember = (Button) findViewById(R.id.add_group_listview).findViewById(R.id.member_add_btn);
        groupList = (ListView) findViewById(R.id.kumiwake_group_listView);
        groupNo = (TextView) findViewById(R.id.group_no);
        backgroundImage = (ImageView) findViewById(R.id.background_img);


        even_fm_ratio_check = (CheckBox) findViewById(R.id.even_fm_ratio_check);
        even_age_ratio_check = (CheckBox) findViewById(R.id.even_age_ratio_check);
        even_grade_ratio_check = (CheckBox) findViewById(R.id.even_grade_ratio_check);
        even_age_ratio_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (even_age_ratio_check.isChecked() == true) {
                    even_grade_ratio_check.setEnabled(false);
                    even_grade_ratio_check.setChecked(false);
                } else {
                    even_grade_ratio_check.setEnabled(true);
                }
            }
        });
        even_grade_ratio_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (even_grade_ratio_check.isChecked() == true) {
                    even_age_ratio_check.setEnabled(false);
                    even_age_ratio_check.setChecked(false);
                } else {
                    even_age_ratio_check.setEnabled(true);
                }
            }
        });

        spinner = (Spinner) findViewById(R.id.custom_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        String[] strs = getResources().getStringArray(R.array.role);
        List<String> list = new ArrayList<String>(Arrays.asList(strs)); // 新インスタンスを生成
        list.remove(1);
        adapter.addAll(list);
        spinner.setAdapter(adapter);
    }

    public void setViews() {
        addMember.setVisibility(View.GONE);
        MBListViewAdapter.setRowHeight(memberList, mbAdapter);
        EditGroupListAdapter.setRowHeight(groupList, gpAdapter);
        numberOfSelectedMember.setText(memberArray.size() + getString(R.string.person));
        groupNo.setText(String.valueOf(groupArray.size()) + " " + getText(R.string.group));
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        backgroundImage.getLayoutParams().height = screenHeight;
    }

    public void setLeader() {
        for (int i = 0; i < memberArray.size(); i++) {
            if (memberArray.get(i).getRole()!=null) {
                if (memberArray.get(i).getRole().matches(".*" + getText(R.string.leader) + ".*")) {

                    String[] roleArray = memberArray.get(i).getRole().split(",");
                    List<String> list = new ArrayList<String>(Arrays.asList(roleArray));
                    Set<String> hs = new HashSet<>();
                    hs.addAll(list);
                    list.clear();
                    list.addAll(hs);
                    list.remove("");
                    Collections.sort(list);
                    final int nowLDNo = Integer.parseInt(list.get(0).substring(2));
                    final int finalI = i;
                    groupList.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView leader = (TextView) groupList.getChildAt(nowLDNo - 1).findViewById(R.id.leader);
                            leader.setText(getText(R.string.leader) + ":" + memberArray.get(finalI).getName());
                        }
                    });
                }
            }
        }
    }

    @OnClick(R.id.normal_kumiwake_button)
    void onClicked() {
        int memberSum = 0;
        Boolean allowToNext = true;
        for (int i = 0; i < groupList.getCount(); i++) {
            int memberNo = EditGroupListAdapter.getMemberNo(i);
            memberSum += memberNo;
            if (memberNo <= 0 || memberSum > memberArray.size()) {
                allowToNext = false;
            }
        }

        if (allowToNext) {
            recreateGrouplist();
            Intent intent = new Intent(this, NormalKumiwakeConfirmation.class);
            intent.putExtra("NormalModeMemberArray", memberArray);
            intent.putExtra("NormalModeGroupArray", newGroupArray);
            intent.putExtra("EvenFMRatio", even_fm_ratio_check.isChecked());
            intent.putExtra("EvenAgeRatio", even_age_ratio_check.isChecked());
            intent.putExtra("EvenGradeRatio", even_grade_ratio_check.isChecked());
            intent.putExtra("EvenRole", (String) spinner.getSelectedItem());
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        } else {
            TextView error_mbNo = (TextView) findViewById(R.id.error_member_no);
            error_mbNo.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.back_to_initial_mbNo)
    void onbcClicked() {
        EditText et;
        for (int i = 0; i < groupList.getCount(); i++) {
            et = (EditText) groupList.getChildAt(i).findViewById(R.id.editTheNumberOfMember);
            et.setFocusable(false);
            et.setText(String.valueOf(groupArray.get(i).getBelongNo()));
            et.setFocusableInTouchMode(true);
            et.setTextColor(Color.BLACK);
        }
    }

    public void changeLeader(int position) {
        StringBuilder newRole = new StringBuilder();
        String roleItem = memberArray.get(position).getRole();
        if (roleItem.matches(".*" + getText(R.string.leader) + ".*")) {
            List<String> list = deleteLeaderList(roleItem);
            int nowLDNo = Integer.parseInt(list.get(0).substring(2));
            MBListViewAdapter.LDNoArray.remove(MBListViewAdapter.LDNoArray.indexOf(nowLDNo));
            list.remove(list.get(0));
            TextView leader = (TextView) groupList.getChildAt(nowLDNo - 1).findViewById(R.id.leader);
            leader.setText(getText(R.string.leader) + ":" + getText(R.string.nothing));

            for (int j = 0; j < list.size(); j++) {
                newRole.append(list.get(j));
                if (j != list.size() - 1) {
                    newRole.append(",");
                }
            }
        } else {
            MBListViewAdapter.LDNo = 1;
            while (MBListViewAdapter.LDNoArray.contains(MBListViewAdapter.LDNo)) {
                MBListViewAdapter.LDNo++;
            }
            if (MBListViewAdapter.LDNo != groupArray.size() + 1) {
                newRole.append(roleItem);
                newRole.append("," + getText(R.string.leader));
                TextView leader = (TextView) groupList.getChildAt(MBListViewAdapter.LDNo - 1).findViewById(R.id.leader);
                leader.setText(getText(R.string.leader) + ":" + memberArray.get(position).getName());
            }
        }
        memberArray.set(position, new Name(memberArray.get(position).getId(), memberArray.get(position).getName(),
                memberArray.get(position).getSex(), memberArray.get(position).getAge(),
                memberArray.get(position).getGrade(), memberArray.get(position).getBelong(), newRole.toString(),
                memberArray.get(position).getName_read()));
        mbAdapter.notifyDataSetChanged();
    }

    public static List<String> deleteLeaderList(String roleItem) {
        String[] roleArray = roleItem.split(",");
        List<String> list = new ArrayList<String>(Arrays.asList(roleArray));
        Set<String> hs = new HashSet<>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        list.remove(MainActivity.getContext().getText(R.string.leader));
        list.remove("");
        Collections.sort(list);
        return list;
    }

    public void recreateGrouplist() {
        newGroupArray = new ArrayList<GroupListAdapter.Group>();
        for (int i = 0; i < groupList.getCount(); i++) {
            String groupName = EditGroupListAdapter.getGroupName(i);
            int memberNo = EditGroupListAdapter.getMemberNo(i);
            newGroupArray.add(new GroupListAdapter.Group(i, groupName, memberNo, null));
        }
    }

    public static void changeBelongNo(int position, int addNo) {
        EditText et;
        int nowNo = 0, newNo = 0;
        if (position == groupList.getCount() - 1) {
            et = (EditText) groupList.getChildAt(0).findViewById(R.id.editTheNumberOfMember);
        } else {
            et = (EditText) groupList.getChildAt(position + 1).findViewById(R.id.editTheNumberOfMember);
        }
        if (et.getText().toString().length() > 0) {
            nowNo = Integer.parseInt(et.getText().toString());
        }
        newNo = nowNo + addNo;
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        et.setText(String.valueOf(newNo));
        if (newNo < 0) {
            et.setTextColor(Color.RED);
        } else {
            et.setTextColor(Color.BLACK);
        }
    }

}