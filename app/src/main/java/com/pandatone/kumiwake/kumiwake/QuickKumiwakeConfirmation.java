package com.pandatone.kumiwake.kumiwake;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/08.
 */
public class QuickKumiwakeConfirmation extends AppCompatActivity {
    ArrayList<String> memberArray, manArray, womanArray, groupArray;
    ArrayAdapter<String> memberAdapter, groupAdapter;
    boolean even_fm_ratio, even_person_ratio;
    static ListView memberList, groupList;
    static TextView customReview, memberNo, groupNo, title, betweenArrows;
    android.support.v7.widget.AppCompatButton runButton;
    RelativeLayout viewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kumiwake_confirmation);
        ButterKnife.bind(this);
        Intent i = getIntent();
        manArray = i.getStringArrayListExtra("QuickModeManList");
        womanArray = i.getStringArrayListExtra("QuickModeWomanList");
        groupArray = i.getStringArrayListExtra("QuickModeGroupList");
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false);
        even_person_ratio = i.getBooleanExtra("EvenPersonRatio", false);
        memberArray = new ArrayList<String>();
        memberArray.addAll(manArray);
        memberArray.addAll(womanArray);
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
        title = (TextView) findViewById(R.id.confirmation_title);
        betweenArrows = (TextView) findViewById(R.id.between_arrows);
        runButton = (android.support.v7.widget.AppCompatButton) findViewById(R.id.kumiwake_button);
        if (KumiwakeSelectMode.sekigime) {
            title.setText(R.string.sekigime_confirm);
            betweenArrows.setText("SEKIGIME");
            runButton.setText(R.string.go_select_seats_type);
        }
        if (womanArray.size() != 0) {
            memberNo.setText(String.valueOf(memberArray.size()) + " " + getText(R.string.person)
                    + "(" + getText(R.string.man) + ":" + String.valueOf(manArray.size()) + getText(R.string.person)
                    + "," + getText(R.string.woman) + ":" + String.valueOf(womanArray.size()) + getText(R.string.person) + ")");
        } else {
            memberNo.setText(String.valueOf(memberArray.size()) + " " + getText(R.string.person));
        }
        groupNo.setText(String.valueOf(groupArray.size()) + " " + getText(R.string.group));
        viewGroup = (RelativeLayout) findViewById(R.id.confirmation_view);
        viewGroup.setBackground(null);
        viewGroup.setBackground(ContextCompat.getDrawable(this, R.drawable.quick_img));
    }

    public void setViews() {
        StringBuilder custom_text = new StringBuilder();
        if (even_fm_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_male_female_ratio) + "\n");
        }
        if (even_person_ratio == true) {
            custom_text.append("☆" + getText(R.string.even_out_person_ratio) + "\n");
        }
        customReview.setText(custom_text.toString());

        for (int i = 0; i < memberAdapter.getCount(); i++) {
            View item = memberAdapter.getView(i, null, memberList);
            ImageView memberIcon;
            memberIcon = (ImageView) item.findViewById(R.id.memberIcon);
            if (memberArray.get(i).matches(".*" + "♡" + ".*")) {
                memberIcon.setColorFilter(getResources().getColor(R.color.woman));
            }
        }
        setRowHeight(memberList, memberAdapter);
        setRowHeight(groupList, groupAdapter);
    }

    @OnClick(R.id.kumiwake_button)
    void onClicked() {
        Intent intent = new Intent(this, QuickKumiwakeResult.class);
        intent.putStringArrayListExtra("QuickModeMemberList", memberArray);
        intent.putStringArrayListExtra("QuickModeManList", manArray);
        intent.putStringArrayListExtra("QuickModeWomanList", womanArray);
        intent.putStringArrayListExtra("QuickModeGroupList", groupArray);
        intent.putExtra("EvenFMRatio", even_fm_ratio);
        intent.putExtra("EvenPersonRatio", even_person_ratio);
        startActivity(intent);
    }

    protected void setAdapter() {
        memberAdapter = new MemberArrayAdapter(this, R.layout.mini_row_member, memberArray, true);
        groupAdapter = new ArrayAdapter<String>(this, R.layout.mini_row_group, R.id.groupName, groupArray);
        memberList.setAdapter(memberAdapter);
        groupList.setAdapter(groupAdapter);
    }

    public static void setRowHeight(ListView listView, ArrayAdapter<String> listAdapter) {
        int totalHeight = 33;

        for (int j = 0; j < listAdapter.getCount(); j++) {
            View item = listAdapter.getView(j, null, listView);
            item.measure(0, 0);
            totalHeight += item.getMeasuredHeight();
        }

        listView.getLayoutParams().height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.requestLayout();
    }
}

class MemberArrayAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private ArrayList<String> items;
    private Boolean showBorder;

    public MemberArrayAdapter(Context context, int textViewResourceId, ArrayList<String> items, Boolean showBorder) {
        super(context, textViewResourceId, items);

        this.items = items;
        this.showBorder = showBorder;

        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        );
    }

    @Override
    public boolean isEnabled(int position) {
        if (showBorder) {
            return true;
        } else {
            return false;
        }
    }

    // 1アイテム分のビューを取得
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTextView;
        ImageView memberIcon;
        View v = convertView;
        String item = items.get(position);

        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_member, null);
        }

        memberIcon = (ImageView) v.findViewById(R.id.memberIcon);
        if (items.get(position).matches(".*" + "♡" + ".*")) {
            memberIcon.setColorFilter(getContext().getResources().getColor(R.color.woman));
        }

        if (item != null) {
            nameTextView = (TextView) v.findViewById(R.id.memberName);
            nameTextView.setText(item);
        }

        return v;
    }
}

