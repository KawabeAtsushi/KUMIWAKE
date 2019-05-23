package com.pandatone.kumiwake.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.GroupListAdapter;
import com.pandatone.kumiwake.member.Sort;

import java.util.HashMap;
import java.util.List;

/**
 * Created by atsushi_2 on 2016/03/20.
 */
public class GroupNameListAdapter extends BaseAdapter {
    private Context context;
    private List<GroupListAdapter.Group> groupList;
    private static HashMap<Integer, Boolean> gSelection = new HashMap<Integer, Boolean>();

    public GroupNameListAdapter(Context context, List<GroupListAdapter.Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTextView, numberOfMemberTextView;
        View v = convertView;
        GroupListAdapter.Group listItem = groupList.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (v == null) {
            v = inflater.inflate(R.layout.row_group, null);
        }
        if (gSelection.get(position) != null) {
            v.setBackgroundColor(Sort.name_getContext().getResources().getColor(R.color.checked_list));
        } else {
            v = inflater.inflate(R.layout.row_group, null);
        }

        if (listItem != null) {
            nameTextView = (TextView) v.findViewById(R.id.groupName);
            numberOfMemberTextView = (TextView) v.findViewById(R.id.theNumberOfMember);
            nameTextView.setText(listItem.getGroup());
            numberOfMemberTextView.setText(String.valueOf(listItem.getBelongNo()) + Sort.name_getContext().getText(R.string.person));
        }

        return v;
    }

    public void setNewSelection(int position, boolean value) {
        gSelection.put(position, value);
        notifyDataSetChanged();
    }

    public static boolean isPositionChecked(int position) {
        Boolean result = gSelection.get(position);
        return result == null ? false : result;
    }

    public void removeSelection(int position) {
        gSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        gSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }
}
