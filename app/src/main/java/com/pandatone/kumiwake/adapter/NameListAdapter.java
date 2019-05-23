package com.pandatone.kumiwake.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.FragmentMember;
import com.pandatone.kumiwake.member.Name;
import com.pandatone.kumiwake.member.Sort;

import java.util.HashMap;
import java.util.List;

public class NameListAdapter extends BaseAdapter {
    private Context context;
    private List<Name> nameList;
    private static String nowData="ￚ no data ￚ", preData = "ￚ no data ￚ";
    public static String nowSort = "ID";
    private static HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();
    LinearLayout.LayoutParams params;

    public NameListAdapter(Context context, List<Name> nameList) {
        this.context = context;
        this.nameList = nameList;
        this.params = new LinearLayout.LayoutParams(0, 1);
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    @Override
    public Name getItem(int position) {return nameList.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(getItem(position).getSex().equals("initial"));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTextView;
        Name listItem = getItem(position);
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (isEnabled(position)) {

            if (v == null|| v.getTag() != "member") {
                v = inflater.inflate(R.layout.row_member, null);
            }

            if (mSelection.get(position) != null) {
                v.setBackgroundColor(Sort.name_getContext().getResources().getColor(R.color.checked_list));
            } else {
                v = inflater.inflate(R.layout.row_member, null);
            }
            setSexIcon(v, position);

            if (listItem != null) {
                nameTextView = (TextView) v.findViewById(R.id.memberName);
                nameTextView.setText(listItem.getName());
            }
        } else {
            if (v == null|| v.getTag() != "initial") {
                v = inflater.inflate(R.layout.row_initial, null);
            }
            if (nowData != null) {
                addInitial(position,v);
            }
        }

        return v;
    }

    public void setSexIcon(View v, int position) {
        ImageView memberIcon;
        memberIcon = (ImageView) v.findViewById(R.id.memberIcon);
        if (getItem(position).getSex().equals("男")) {
            memberIcon.setImageResource(R.drawable.member_img);
        } else {
            memberIcon.setColorFilter(Sort.name_getContext().getResources().getColor(R.color.woman));
        }
    }

    public void addInitial(int position, View v) {
        Name nowItem=getItem(position);

        switch (nowSort) {
            case "NAME":
                nowData = nowItem.getName_read();
                break;
            case "AGE":
                nowData = String.valueOf(nowItem.getAge());
                break;
            case "GRADE":
                nowData = String.valueOf(nowItem.getGrade());
                break;
        }

        if(position!=0) {
            Name preItem = getItem(position - 2);

            switch (nowSort) {
                case "NAME":
                    preData = preItem.getName_read();
                    break;
                case "AGE":
                    preData = String.valueOf(preItem.getAge());
                    break;
                case "GRADE":
                    preData = String.valueOf(preItem.getGrade());
                    break;
            }
        }

        if ((!nowData.equals(preData)||position==0)&&!nowSort.equals("ID")) {
            TextView initialText = (TextView) v.findViewById(R.id.initial);
            initialText.setText(nowData);
        }else {
            v.setLayoutParams(params);
        }
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public static boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }
}
