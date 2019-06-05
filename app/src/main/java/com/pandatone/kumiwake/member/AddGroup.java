package com.pandatone.kumiwake.member;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/02/24.
 */
public class AddGroup extends AppCompatActivity {

    static AppCompatEditText groupEditText;
    //static AppCompatEditText readEditText;
    static ListView listView;
    static TextView numberOfSelectedMember;
    private TextInputLayout textInputLayout;
    static GroupListAdapter dbAdapter;
    int nextId = FragmentGroup.dbAdapter.getMaxId() + 1;
    static int position;
    MBListViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group);
        ButterKnife.bind(this);
        dbAdapter = new GroupListAdapter(this);
        findViews();
        Intent i = getIntent();
        position = i.getIntExtra("POSITION", nextId);
        if (position != nextId) {
            setItem(position);
        }
        FragmentMember.DeleteBelongInfoAll(nextId);
    }

    protected void findViews() {
        groupEditText = (AppCompatEditText) findViewById(R.id.input_group);
        //readEditText = (AppCompatEditText) findViewById(R.id.input_group_read);
        textInputLayout = (TextInputLayout) findViewById(R.id.group_form_input_layout);
        listView = (ListView) findViewById(R.id.add_group_listview).findViewById(R.id.reviewListView);
        numberOfSelectedMember = (TextView) findViewById(R.id.add_group_listview).findViewById(R.id.numberOfSelectedMember);
        listView.setEmptyView(findViewById(R.id.add_group_listview).findViewById(R.id.emptyMemberList));
        Button addMember = (Button) findViewById(R.id.add_group_listview).findViewById(R.id.member_add_btn);
        addMember.setOnClickListener(clicked);
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<Name> nameByBelong;
        if (position == nextId) {
            nameByBelong = FragmentMember.searchBelong(String.valueOf(nextId));
        } else {
            nameByBelong = FragmentMember.searchBelong(String.valueOf(FragmentGroup.nameList.get(position).getId()));
        }
        adapter = new MBListViewAdapter(AddGroup.this, nameByBelong,0);
        listView.setAdapter(adapter);
        numberOfSelectedMember.setText(adapter.getCount() + getString(R.string.person) + getString(R.string.selected));
        FragmentMember.DuplicateBelong();
    }

    private View.OnClickListener clicked = new View.OnClickListener() {

        public void onClick(View v) {
            moveMemberMain();
        }
    };


    @OnClick(R.id.group_registration_button)
    void onRegistrationGroupClicked() {
        String group = groupEditText.getText().toString();
        if (TextUtils.isEmpty(group)) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(getText(R.string.error_empty_group));
        } else {
            saveItem();
            Toast.makeText(this, getText(R.string.group) + " \"" + group + "\" " + getText(R.string.registered), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    protected void saveItem() {
        String name = groupEditText.getText().toString();
        String name_read = name;
        dbAdapter.open();
        dbAdapter.saveGroup(name,name_read,adapter.getCount());
        dbAdapter.close();
        FragmentMember.loadName();
    }

    protected void updateItem(int listId) {
        String name = groupEditText.getText().toString();
        String name_read = name;
        dbAdapter.open();
        dbAdapter.updateGroup(listId,name,name_read,adapter.getCount());
        dbAdapter.close();
        FragmentMember.loadName();
    }



    public void setItem(int position) {
        GroupListAdapter.Group listItem = FragmentGroup.nameList.get(position);
        int listId = listItem.getId();
        String group = listItem.getGroup();
        groupEditText.setText(group);

        update(listId);
    }

    public void update(final int listId) {
        Button updateBt = (Button) findViewById(R.id.group_registration_button);
        updateBt.setText(R.string.update);
        updateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group = groupEditText.getText().toString();
                if (TextUtils.isEmpty(group)) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getText(R.string.error_empty_name));
                } else {
                    updateItem(listId);
                    Toast.makeText(getApplicationContext(), getText(R.string.group) + " \"" + group + "\" " + getText(R.string.updated), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public void moveMemberMain() {
        Intent intent = new Intent(this, MemberMain.class);
        intent.putExtra("visible", true);
        intent.putExtra("delete_icon_visible", false);
        intent.putExtra("START_ACTIONMODE", true);
        intent.putExtra("GROUP_ID", getGroupId());
        startActivity(intent);
    }

    public int getGroupId() {
        int groupId;
        if (position == nextId) {
            groupId = nextId;
        } else {
            groupId = FragmentGroup.nameList.get(position).getId();
        }
        return groupId;
    }

}