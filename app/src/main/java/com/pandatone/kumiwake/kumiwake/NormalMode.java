package com.pandatone.kumiwake.kumiwake;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;
import com.pandatone.kumiwake.member.MemberMain;
import com.pandatone.kumiwake.member.Name;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/05/02.
 */
public class NormalMode extends AppCompatActivity {
    AppCompatEditText GpNoEditText;
    static TextView errorGroup, errorMember,numberOfSelectedMember;
    static ListView listView;
    Button addMember;
    ImageView backgroundImage;
    CheckBox even_fm_ratio_check, even_person_ratio_check;
    MBListViewAdapter adapter;
    static ArrayList<Name> memberArray;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Slide());
        }
        setContentView(R.layout.normal_mode);
        ButterKnife.bind(this);
        findViews();
        memberArray = new ArrayList<Name>();
        addMember.setOnClickListener(clicked);
        numberOfSelectedMember.setText("0" + getString(R.string.person) + getString(R.string.selected));
        setKeyboardListener();
    }

    public void findViews(){
        GpNoEditText = (AppCompatEditText) findViewById(R.id.input_group_no);
        errorGroup = (TextView) findViewById(R.id.error_group_no);
        errorMember = (TextView) findViewById(R.id.error_member_no);
        listView = (ListView) findViewById(R.id.add_group_listview).findViewById(R.id.reviewListView);
        numberOfSelectedMember = (TextView) findViewById(R.id.add_group_listview).findViewById(R.id.numberOfSelectedMember);
        listView.setEmptyView(findViewById(R.id.add_group_listview).findViewById(R.id.emptyMemberList));
        addMember = (Button) findViewById(R.id.add_group_listview).findViewById(R.id.member_add_btn);
        backgroundImage =(ImageView)findViewById(R.id.background_img);

        even_fm_ratio_check = (CheckBox) findViewById(R.id.even_fm_ratio_check);
        even_person_ratio_check = (CheckBox) findViewById(R.id.even_person_ratio_check);
    }

    private View.OnClickListener clicked = new View.OnClickListener() {

        public void onClick(View v) {
            moveMemberMain();
        }
    };

    public void moveMemberMain() {
        Intent intent = new Intent(this, MemberMain.class);
        intent.putExtra("visible", true);
        intent.putExtra("delete_icon_visible", false);
        intent.putExtra("START_ACTIONMODE", true);
        intent.putExtra("kumiwake_select", true);
        intent.putExtra("memberArray", memberArray);
        startActivityForResult(intent, 1000);
    }


    @OnClick(R.id.normal_kumiwake_button)
    void onClicked() {
        String group_no = GpNoEditText.getText().toString();

        if (TextUtils.isEmpty(group_no)) {
            errorGroup.setText(R.string.error_empty_group_no);
            scrollToTop();
        }
        if (adapter == null) {
            errorMember.setText(R.string.error_empty_member_list);
        } else if (!group_no.equals("") && Integer.parseInt(group_no) > adapter.getCount()) {
            errorGroup.setText(R.string.number_of_groups_is_much_too);
            errorMember.setText("");
            scrollToTop();
        } else if (TextUtils.isEmpty(group_no)) {
            errorGroup.setText(R.string.error_empty_group_no);
            errorMember.setText("");
            scrollToTop();
        }else{
            ArrayList<GroupListAdapter.Group> groupArray = new ArrayList<GroupListAdapter.Group>();
            int groupNo =Integer.parseInt(group_no);
                int eachMemberNo = memberArray.size() / groupNo;
                int remainder = memberArray.size() % groupNo;

                for (int i = 0; i < remainder; i++) {
                    groupArray.add( new GroupListAdapter.Group(i,getText(R.string.group) + " " + String.valueOf(i+1),eachMemberNo+1,null ));
                }

                for (int i = remainder; i < groupNo; i++) {
                    groupArray.add(new GroupListAdapter.Group(i,getText(R.string.group) + " " + String.valueOf(i+1),eachMemberNo,null ));
                }

            Intent intent = new Intent(this, KumiwakeCustom.class);
            intent.putExtra("NormalModeMemberArray", memberArray);
            intent.putExtra("NormalModeGroupArray", groupArray);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent i){
        if (resultCode == RESULT_OK){
        memberArray = (ArrayList<Name>)i.getSerializableExtra("memberArray");
            adapter = new MBListViewAdapter(this, memberArray,1000);
            listView.setAdapter(adapter);
            MBListViewAdapter.setRowHeight(listView, adapter);

        numberOfSelectedMember.setText(memberArray.size() + getString(R.string.person) + getString(R.string.selected));}
    }

    public void scrollToTop(){
        final ScrollView scrollView = (ScrollView)findViewById(R.id.normal_mode_scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        });
    }

    public final void setKeyboardListener() {
        final View activityRootView = findViewById(R.id.normal_mode);
        final android.support.v7.widget.AppCompatButton view
                = (android.support.v7.widget.AppCompatButton) findViewById(R.id.normal_kumiwake_button);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect r = new Rect();

            @Override
            public void onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - r.height();
                if (heightDiff > 100) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
