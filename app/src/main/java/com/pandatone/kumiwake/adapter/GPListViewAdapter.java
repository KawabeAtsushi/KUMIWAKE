package com.pandatone.kumiwake.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.member.Sort;

import java.util.ArrayList;

/**
 * Created by atsushi_2 on 2016/04/16.
 */
public class GPListViewAdapter extends BaseAdapter {

    private Context context;
    static ArrayList<GroupListAdapter.Group> listElements;

    public GPListViewAdapter(Context context, ArrayList<GroupListAdapter.Group> nameList) {
        this.context = context;
        this.listElements = nameList;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTextView,memberNoTextView;
        View v = convertView;
        String listItem = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null) {
            v = inflater.inflate(R.layout.mini_row_group, null);
        }


        if (listItem != null) {
            nameTextView = (TextView) v.findViewById(R.id.groupName);
            memberNoTextView =(TextView)v.findViewById(R.id.memberNo);
            memberNoTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(listElements.get(position).getGroup());
            memberNoTextView.setText(String.valueOf(listElements.get(position).getBelongNo())+ Sort.name_getContext().getText(R.string.person));
        }

        return v;
    }


    public static void setRowHeight(ListView listView, GPListViewAdapter listAdapter) {
        int totalHeight = 0;

        for (int j = 0; j < listAdapter.getCount(); j++) {
            View item = listAdapter.getView(j, null, listView);
            item.measure(0, 0);
            totalHeight += item.getMeasuredHeight();
        }

        listView.getLayoutParams().height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.requestLayout();
    }
}

