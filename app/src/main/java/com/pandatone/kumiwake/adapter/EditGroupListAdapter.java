package com.pandatone.kumiwake.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.kumiwake.KumiwakeCustom;
import com.pandatone.kumiwake.kumiwake.MainActivity;
import com.pandatone.kumiwake.member.GroupListAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by atsushi_2 on 2016/03/20.
 */
public class EditGroupListAdapter extends BaseAdapter {
    private Context context;
    private List<GroupListAdapter.Group> groupList;
    private static Map<Integer, EditText> groupNameView = new HashMap<Integer, EditText>();
    private static Map<Integer, EditText> memberNoView = new HashMap<Integer, EditText>();
    int beforeNo, afterNo;

    public EditGroupListAdapter(Context context, List<GroupListAdapter.Group> groupList) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EditText nameEditText, numberOfMemberEditText;
        TextView leader;
        View v = convertView;
        GroupListAdapter.Group listItem = groupList.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (v == null) {
            v = inflater.inflate(R.layout.row_edit_group, null);
        }

        if (listItem != null) {
            nameEditText = (EditText) v.findViewById(R.id.editGroupName);
            numberOfMemberEditText = (EditText) v.findViewById(R.id.editTheNumberOfMember);
            leader = (TextView) v.findViewById(R.id.leader);
            nameEditText.setText(listItem.getGroup());
            numberOfMemberEditText.setText(String.valueOf(listItem.getBelongNo()));
            leader.setText(MainActivity.getContext().getText(R.string.leader) + ":" + MainActivity.getContext().getText(R.string.nothing));
            groupNameView.put(position, nameEditText);
            memberNoView.put(position, numberOfMemberEditText);
            numberOfMemberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (!numberOfMemberEditText.getText().toString().equals("") && !numberOfMemberEditText.getText().toString().equals("-")) {
                            KumiwakeCustom.scrollView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return false;
                                }
                            });
                            beforeNo = Integer.parseInt(numberOfMemberEditText.getText().toString());
                        } else {
                            KumiwakeCustom.scrollView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            });
                        }
                        numberOfMemberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                    } else {

                        if (!numberOfMemberEditText.getText().toString().equals("") && !numberOfMemberEditText.getText().toString().equals("-")) {
                            KumiwakeCustom.scrollView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return false;
                                }
                            });

                                afterNo = Integer.parseInt(numberOfMemberEditText.getText().toString());

                            int addNo = beforeNo - afterNo;
                            KumiwakeCustom.changeBelongNo(position, addNo);
                            if (afterNo < 0) {
                                numberOfMemberEditText.setTextColor(Color.RED);
                            } else {
                                numberOfMemberEditText.setTextColor(Color.BLACK);
                            }
                        } else {
                            KumiwakeCustom.scrollView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            });
                            afterNo = 0;
                        }
                    }
                }
            });

        }

        return v;
    }

    public static String getGroupName(int position) {
        String groupName = MainActivity.getContext().getText(R.string.nothing).toString();
        EditText groupNameEditText = groupNameView.get(position);
        if (groupNameEditText.getText() != null) {
            groupName = groupNameEditText.getText().toString();
        }
        return groupName;
    }

    public static int getMemberNo(int position) {
        int memberNo = 0;
        EditText memberNoEditText = memberNoView.get(position);
        if (memberNoEditText.getText().toString().length() > 0) {
            memberNo = Integer.parseInt(memberNoEditText.getText().toString());
        }
        return memberNo;
    }


    public static void setRowHeight(ListView listView, EditGroupListAdapter listAdapter) {
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

