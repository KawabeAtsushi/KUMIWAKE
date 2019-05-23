package com.pandatone.kumiwake.member;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.MBListViewAdapter;

import java.util.ArrayList;

/**
 * Created by atsushi_2 on 2016/04/17.
 */
public class GroupClick {
    static TextView group;
    static TextView number;
    static TextView belongMb;
    static ListView belongList;
    static Button okBt;


    public static void GroupInfoDialog(View view, final AlertDialog.Builder builder) {

        group = (TextView) view.findViewById(R.id.infoName);
        number = (TextView) view.findViewById(R.id.infoNoOfMb);
        belongMb = (TextView) view.findViewById(R.id.infoMember);
        belongList = (ListView)view.findViewById(R.id.belongList);
        okBt = (Button) view.findViewById(R.id.okBt);

        builder.setTitle(R.string.information);
        builder.setView(view);

    }

    public static void SetInfo(int position) {
        ArrayList<Name> nameByBelong = FragmentMember.searchBelong(String.valueOf(FragmentGroup.nameList.get(position).getId()));
        MBListViewAdapter adapter = new MBListViewAdapter(getContext(), nameByBelong,0);

        group.setText(getContext().getText(R.string.group_name) + " : " +FragmentGroup.nameList.get(position).getGroup());
        number.setText(getContext().getText(R.string.number_of_member) + " : " +adapter.getCount() + getContext().getString(R.string.person));
        belongMb.setText(getContext().getText(R.string.belong) + "" + getContext().getText(R.string.member));
        belongList.setAdapter(adapter);

        //メンバー数の更新
        int id,belongNo;
        String name,name_read="ￚ no data ￚ";
        GroupListAdapter GPdbAdapter=new GroupListAdapter(getContext());
        GroupListAdapter.Group listItem = FragmentGroup.nameList.get(position);

        id = listItem.getId();
        name=listItem.getGroup();
        belongNo=adapter.getCount();
        GPdbAdapter.updateGroup(id,name,name_read,belongNo);
    }

    protected static Context getContext() {
        return MemberMain.getContext();
    }
}



