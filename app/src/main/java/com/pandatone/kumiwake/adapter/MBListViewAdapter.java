package com.pandatone.kumiwake.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.kumiwake.KumiwakeCustom;
import com.pandatone.kumiwake.member.Name;
import com.pandatone.kumiwake.member.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by atsushi_2 on 2016/04/16.
 */
public class MBListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context context;
    static ArrayList<Name> listElements;
    public static ArrayList<Integer> LDNoArray;
    public static int LDNo = 1, groupNo;

    public MBListViewAdapter(Context context, ArrayList<Name> nameList, int groupNo) {
        this.context = context;
        this.listElements = nameList;
        this.groupNo = groupNo;
        LDNo=1;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(groupNo < 1000) {
            LDNoArray = new ArrayList<>(groupNo);
        }
    }

    @Override
    public int getCount() {
        return listElements.size();
    }

    @Override
    public String getItem(int position) {
        return String.valueOf(listElements.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if(groupNo < 1000){
        return true;}else{return false;}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTextView;
        View v = convertView;
        String listItem = getItem(position);


        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_member, null);
        }

        setStarIcon(v, position);

        if (listItem != null) {
            setSexIcon(v, position);
            nameTextView = (TextView) v.findViewById(R.id.memberName);
            nameTextView.setText(listElements.get(position).getName());
        }

        return v;
    }

    public static void setSexIcon(View v, int position) {
        ImageView memberIcon;
        memberIcon = (ImageView) v.findViewById(R.id.memberIcon);
        if (listElements.get(position).getSex().equals("男")) {
            memberIcon.setImageResource(R.drawable.member_img);
        } else {
            memberIcon.setColorFilter(Sort.name_getContext().getResources().getColor(R.color.woman));
        }
    }

    public static void setStarIcon(View v, int position) {
        ImageView starIcon, memberIcon;
        TextView leaderNo;
        starIcon = (ImageView) v.findViewById(R.id.starIcon);
        memberIcon = (ImageView) v.findViewById(R.id.memberIcon);
        leaderNo = (TextView) v.findViewById(R.id.leaderNo);
        if (listElements.get(position).getRole() != null &&
                listElements.get(position).getRole().matches(".*" + Sort.name_getContext().getText(R.string.leader) + ".*")) {
            memberIcon.setVisibility(View.GONE);
            starIcon.setVisibility(View.VISIBLE);
            leaderNo.setVisibility(View.GONE);

            if (groupNo != 1000 && LDNo != groupNo+1
                    && !listElements.get(position).getRole().matches(".*" + "LD" + ".*")) {
                LDNoArray.add(LDNo);
                StringBuilder newRole = new StringBuilder();
                newRole.append(listElements.get(position).getRole());
                newRole.append("," + "LD" + String.valueOf(LDNo));
                listElements.set(position, new Name(listElements.get(position).getId(), listElements.get(position).getName()
                        ,listElements.get(position).getSex(), listElements.get(position).getAge(),
                        listElements.get(position).getGrade(), listElements.get(position).getBelong(),
                        newRole.toString(), listElements.get(position).getName_read()));
                LDNo++;
            }else if(groupNo != 1000 && LDNo == groupNo+1
                    && !listElements.get(position).getRole().matches(".*" + "LD" + ".*")){
                List<String> list = KumiwakeCustom.deleteLeaderList(listElements.get(position).getRole());
                StringBuilder newRole = new StringBuilder();
                for (int j = 0; j < list.size(); j++) {
                    newRole.append(list.get(j));
                    if (j != list.size() - 1) {
                        newRole.append(",");
                    }
                }
                listElements.set(position, new Name(listElements.get(position).getId(), listElements.get(position).getName(),
                        listElements.get(position).getSex(), listElements.get(position).getAge(),
                        listElements.get(position).getGrade(), listElements.get(position).getBelong(),
                        newRole.toString(), listElements.get(position).getName_read()));
            }

            if(groupNo != 2000 && listElements.get(position).getRole().matches(".*" + "LD" + ".*")) {
                leaderNo.setVisibility(View.VISIBLE);
                String[] roleArray = listElements.get(position).getRole().split(",");
                List<String> list = new ArrayList<String>(Arrays.asList(roleArray));
                leaderNo.setText(String.valueOf(list.get(list.size()-1).substring(2)));
            }
        } else {
            memberIcon.setVisibility(View.VISIBLE);
            starIcon.setVisibility(View.GONE);
            leaderNo.setVisibility(View.GONE);
            setSexIcon(v, position);
        }
    }

    public static void setRowHeight(ListView listView, MBListViewAdapter listAdapter) {
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

