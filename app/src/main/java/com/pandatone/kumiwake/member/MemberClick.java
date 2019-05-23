package com.pandatone.kumiwake.member;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pandatone.kumiwake.R;

/**
 * Created by atsushi_2 on 2016/04/17.
 */
public class MemberClick {
    static TextView name;
    static TextView sex;
    static TextView age;
    static TextView grade;
    static TextView belong;
    static TextView role;
    static Button okBt;


    public static void MemberInfoDialog(View view, final AlertDialog.Builder builder) {

        name = (TextView) view.findViewById(R.id.infoName);
        sex = (TextView) view.findViewById(R.id.infoSex);
        age = (TextView) view.findViewById(R.id.infoAge);
        grade = (TextView) view.findViewById(R.id.infoGrade);
        belong = (TextView) view.findViewById(R.id.infoBelong);
        role = (TextView) view.findViewById(R.id.infoRole);
        okBt = (Button) view.findViewById(R.id.okBt);

        builder.setTitle(R.string.information);
        builder.setView(view);

    }

    public static void SetInfo(int position) {
        name.setText(getContext().getText(R.string.member_name) + " : " +FragmentMember.nameList.get(position).getName()+" ("+FragmentMember.nameList.get(position).getName_read()+")");
        sex.setText(getContext().getText(R.string.sex) + " : " +FragmentMember.nameList.get(position).getSex());
        age.setText(getContext().getText(R.string.age) + " : " +String.valueOf(FragmentMember.nameList.get(position).getAge()));
        grade.setText(getContext().getText(R.string.grade) + " : " + String.valueOf(FragmentMember.nameList.get(position).getGrade()));

        if(ViewBelong(position) != ""){
            belong.setText(getContext().getText(R.string.belong) + " : " + ViewBelong(position));
        }else{belong.setText(getContext().getText(R.string.belong) + " : " + getContext().getText(R.string.nothing));}

        if(!FragmentMember.nameList.get(position).getRole().equals("")){
            role.setText(getContext().getText(R.string.role) + " : " + FragmentMember.nameList.get(position).getRole());
        }else{role.setText(getContext().getText(R.string.role) + " : " + getContext().getText(R.string.nothing));}

    }

    public static String ViewBelong(int position) {
        String result;

        FragmentMember.dbAdapter.open();
        String belongText = FragmentMember.nameList.get(position).getBelong();
        String[] belongArray = belongText.split(",");
        StringBuilder newBelong = new StringBuilder();

        for (int i = 0; i < belongArray.length; i++) {
            String belongGroup = belongArray[i];
            for (int j = 0; j < FragmentGroup.ListCount; j++) {
                GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
                String groupId = String.valueOf(listItem.getId());
                if (belongGroup.equals(groupId)) {
                    String listName = listItem.getGroup();
                    newBelong.append(listName + ",");
                }
            }
        }
        FragmentMember.dbAdapter.close();
        if(newBelong.toString().equals("")){
            result = "";
        }else { result = newBelong.substring(0, newBelong.length() - 1); }

        return result;
    }
    protected static Context getContext() {
        return MemberMain.getContext();
    }
}



