package com.pandatone.kumiwake.member;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.MemberListAdapter;
import com.pandatone.kumiwake.adapter.NameListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by atsushi_2 on 2016/02/23.
 */
public class FragmentMember extends ListFragment {
    private static MemberMain parent;
    static MemberListAdapter dbAdapter;
    public static NameListAdapter listAdapter;
    static GroupListAdapter gpdbAdapter;
    public static List<Name> nameList;
    static Name listItem;
    static FloatingActionButton fab;
    static int ListCount, checkedCount=0;
    String groupId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter = new MemberListAdapter(getActivity());
        gpdbAdapter = new GroupListAdapter(getActivity());
        nameList = new ArrayList<>();
        listAdapter = new NameListAdapter(getActivity(), nameList);
        groupId = String.valueOf(parent.groupId);
        NameListAdapter.nowSort = "ID";
        Sort.initial=2;
        setListAdapter(listAdapter);
        loadName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_member, container, false);
        fab = (FloatingActionButton) view.findViewById(R.id.member_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.moveMember();
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListCount = getListView().getCount();
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new CallbackMB());
        getListView().setFastScrollEnabled(true);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                final AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view2 = inflater.inflate(R.layout.member_info,
                        (ViewGroup) getActivity().findViewById(R.id.info_layout));
                final String membername = nameList.get(position).getName();
                if (MemberMain.searchView.isActivated() == true)
                    MemberMain.searchView.onActionViewCollapsed();
                FragmentGroup.loadName();

                final String[] Items =
                        {Sort.name_getContext().getString(R.string.information),
                                Sort.name_getContext().getString(R.string.edit),
                                Sort.name_getContext().getString(R.string.delete),};
                builder.setTitle(membername);
                builder.setItems(Items, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                MemberClick.MemberInfoDialog(view2, builder2);
                                MemberClick.SetInfo(position);
                                final AlertDialog dialog2 = builder2.create();
                                dialog2.show();
                                MemberClick.okBt.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        dialog2.dismiss();
                                    }
                                });
                                break;
                            case 1:
                                Intent i = new Intent(getActivity(), AddMember.class);
                                i.putExtra("POSITION", position);
                                startActivity(i);
                                break;
                            case 2:
                                DeleteSingleMember(position, membername);
                                break;

                        }
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if (parent.start_actionmode == true) {
            getListView().startActionMode(new CallbackMB());
            dbAdapter.open();
            for (int i = 1; i < ListCount; i+=2) {
                listItem = nameList.get(i);
                String belongText = listItem.getBelong();
                if(belongText!=null) {
                    String[] belongArray = belongText.split(",");
                    List<String> list = new ArrayList<String>(Arrays.asList(belongArray));
                    if (list.contains(groupId)) {
                        getListView().setItemChecked(i, !listAdapter.isPositionChecked(i));
                    }
                }
                dbAdapter.close();
            }
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                //行をクリックした時の処理
                public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                    getListView().setItemChecked(position, !listAdapter.isPositionChecked(position));
                }
            });
        }

        if (parent.kumiwake_select == true) {
            getListView().startActionMode(new CallbackMB());
            dbAdapter.open();
            for (int j = 0; j < parent.memberArray.size(); j++) {
                for (int i = 1; i < ListCount; i+=2) {
                    listItem = nameList.get(i);
                    if (listItem.getId() == parent.memberArray.get(j).getId()) {
                        getListView().setItemChecked(i, !listAdapter.isPositionChecked(i));
                    }
                    dbAdapter.close();
                }
            }

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                //行をクリックした時の処理
                public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                    getListView().setItemChecked(position, !listAdapter.isPositionChecked(position));
                }
            });
        }

        // 行を長押しした時の処理
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getListView().setItemChecked(position, !listAdapter.isPositionChecked(position));
                return false;
            }
        });

        getListView().setTextFilterEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        // アクションアイテム選択時
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;

            case R.id.item_delete:
                DeleteMember();
                break;

            case R.id.item_all_select:
                for (int i = 1; i < ListCount; i+=2) {
                    getListView().setItemChecked(i, true);
                }
                break;

            case R.id.item_sort:
                Sort.memberSort(builder);
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
                ListCount = getListView().getCount();
                break;

            case R.id.item_filter:
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.filter_member,
                        (ViewGroup) getActivity().findViewById(R.id.filter_member));
                final Spinner belongSpinner = (Spinner) layout.findViewById(R.id.filter_belong_spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
                List<String> list = new ArrayList<String>(); // 新インスタンスを生成
                list.add(getString(R.string.no_selected));
                for (int j = 0; j < FragmentGroup.ListCount; j++) {
                    GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
                    String groupName = listItem.getGroup();
                    list.add(groupName);
                }
                adapter.addAll(list);
                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );
                belongSpinner.setAdapter(adapter);
                builder.setTitle(getText(R.string.filtering));
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                // back keyを使用不可に設定
                builder.setCancelable(false);
                final android.support.v7.app.AlertDialog dialog2 = builder.create();
                dialog2.show();

                Button okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filter(layout, belongSpinner, dialog2);
                        ListCount=getListView().getCount();
                    }
                });
                break;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        parent = (MemberMain) context;
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadName();
        dbAdapter.notifyDataSetChanged();
    }


    public static void loadName() {
        dbAdapter.open();
        Cursor c = dbAdapter.getAllNames();
        dbAdapter.getCursor(c);
        dbAdapter.close();
    }

    public static void selectName(String newText) throws IOException {
        if (TextUtils.isEmpty(newText)) {
            dbAdapter.picName(null);
        } else {
            dbAdapter.picName(newText);
        }
    }

    public void DeleteSingleMember(final int position, String name) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle(name);
        builder.setMessage(R.string.Do_delete);
        // OKの時の処理
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbAdapter.open();
                listItem = nameList.get(position);
                int listId = listItem.getId();
                dbAdapter.selectDelete(String.valueOf(listId));
                dbAdapter.close();    // DBを閉じる
                parent.reload();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void DeleteMember() {
        // アラートダイアログ表示
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(parent.getApplicationContext());
        builder.setTitle(checkedCount + " " + parent.getString(R.string.member) + parent.getString(R.string.delete));
        builder.setMessage(R.string.Do_delete);
        // OKの時の処理
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dbAdapter.open();     // DBの読み込み(読み書きの方)
                for (int i = 1; i < ListCount; i+=2) {
                    final SparseBooleanArray booleanArray = getListView().getCheckedItemPositions();
                    boolean checked = booleanArray.get(i);
                    if (checked == true) {
                        // IDを取得する
                        listItem = nameList.get(i);
                        int listId = listItem.getId();
                        dbAdapter.selectDelete(String.valueOf(listId));     // DBから取得したIDが入っているデータを削除する
                    }
                }
                dbAdapter.close();    // DBを閉じる
                listAdapter.clearSelection();
                parent.reload();
            }
        });
    }

    public void filter(View layout, Spinner belongSpinner, android.support.v7.app.AlertDialog dialog2) {
        TextView error_age, error_grade;
        EditText maxageV, minageV, maxgradeV, mingradeV;
        Spinner roleSpinner;
        int maxage, minage, maxgrade, mingrade;
        String sex, groupName, belong, belongNo = "", role;
        maxageV = (EditText) layout.findViewById(R.id.max_age);
        minageV = (EditText) layout.findViewById(R.id.min_age);
        maxgradeV = (EditText) layout.findViewById(R.id.max_grade);
        mingradeV = (EditText) layout.findViewById(R.id.min_grade);
        error_age = (TextView) layout.findViewById(R.id.error_age_range);
        error_grade = (TextView) layout.findViewById(R.id.error_grade_range);
        RadioGroup sexGroup = (RadioGroup) layout.findViewById(R.id.sexGroup);
        RadioButton sexButton = (RadioButton) layout.findViewById(sexGroup.getCheckedRadioButtonId());
        roleSpinner = (Spinner) layout.findViewById(R.id.filter_role_spinner);
        sex = (String) sexButton.getText();

        if (!maxageV.getText().toString().equals("")) {
            maxage = AddMember.getValue(maxageV);
        } else {
            maxage = 1000;
        }
        if (!minageV.getText().toString().equals("")) {
            minage = AddMember.getValue(minageV);
        } else {
            minage = 0;
        }
        if (!maxgradeV.getText().toString().equals("")) {
            maxgrade = AddMember.getValue(maxgradeV);
        } else {
            maxgrade = 1000;
        }
        if (!minageV.getText().toString().equals("")) {
            mingrade = AddMember.getValue(mingradeV);
        } else {
            mingrade = 0;
        }

        if (maxage < minage) {
            error_age.setVisibility(View.VISIBLE);
            error_age.setText(R.string.range_error);
        } else {
            error_age.setVisibility(View.GONE);
        }
        if (maxgrade < mingrade) {
            error_grade.setVisibility(View.VISIBLE);
            error_grade.setText(R.string.range_error);
        } else {
            error_grade.setVisibility(View.GONE);
        }
        belong = (String) belongSpinner.getSelectedItem();
        if (belong.equals(getString(R.string.no_selected))) {
            belongNo = "";
        } else {
            for (int j = 0; j < FragmentGroup.ListCount; j++) {
                GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
                groupName = listItem.getGroup();
                if (belong.equals(groupName)) {
                    belongNo = String.valueOf(listItem.getId()) + ",";
                }
            }
        }
        role = (String) roleSpinner.getSelectedItem();
        if (role.equals(getString(R.string.no_selected))) {
            role = "";
        }
        if (sex.equals(getString(R.string.no_specified))) {
            sex = "";
        }
        try {
            if (maxage >= minage && maxgrade >= mingrade) {
                dbAdapter.filterName(sex, minage, maxage, mingrade, maxgrade, belongNo, role);
                dialog2.dismiss();
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    class CallbackMB implements ListView.MultiChoiceModeListener {

        final SparseBooleanArray booleanArray = getListView().getCheckedItemPositions();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // アクションモード初期化処理
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.member_main_menu, menu);
            MenuItem searchIcon = menu.findItem(R.id.search_view);
            MenuItem deleteIcon = menu.findItem(R.id.item_delete);
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            parent.decision.setOnClickListener(decision_clicked);
            searchIcon.setVisible(false);
            deleteIcon.setVisible(parent.delete_icon_visible);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            // アクションアイテム選択時
            switch (item.getItemId()) {
                case R.id.item_delete:
                    DeleteMember();
                    break;

                case R.id.item_all_select:
                    for (int i = 1; i < ListCount; i+=2) {
                        getListView().setItemChecked(i, true);
                    }
                    break;

                case R.id.item_sort:
                    Sort.memberSort(builder);
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                    ListCount = getListView().getCount();
                    break;

                case R.id.item_filter:
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.filter_member,
                            (ViewGroup) getActivity().findViewById(R.id.filter_member));
                    final Spinner belongSpinner = (Spinner) layout.findViewById(R.id.filter_belong_spinner);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
                    List<String> list = new ArrayList<String>(); // 新インスタンスを生成
                    list.add(getString(R.string.no_selected));
                    for (int j = 0; j < FragmentGroup.ListCount; j++) {
                        GroupListAdapter.Group listItem = FragmentGroup.nameList.get(j);
                        String groupName = listItem.getGroup();
                        list.add(groupName);
                    }
                    adapter.addAll(list);
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    belongSpinner.setAdapter(adapter);
                    builder.setTitle(getText(R.string.filtering));
                    builder.setView(layout);
                    builder.setPositiveButton("OK", null);
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    // back keyを使用不可に設定
                    builder.setCancelable(false);
                    final android.support.v7.app.AlertDialog dialog2 = builder.create();
                    dialog2.show();

                    Button okButton = dialog2.getButton(AlertDialog.BUTTON_POSITIVE);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            filter(layout, belongSpinner, dialog2);
                            ListCount=getListView().getCount();
                        }
                    });
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // 決定ボタン押下時
            listAdapter.clearSelection();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // アクションモード表示事前処理
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            // アクションモード時のアイテムの選択状態変更時

            checkedCount=getListView().getCheckedItemCount();

            if (checked) {
                listAdapter.setNewSelection(position, checked);
            } else {
                listAdapter.removeSelection(position);
            }

            mode.setTitle(checkedCount + getString(R.string.selected));
        }

        private View.OnClickListener decision_clicked = new View.OnClickListener() {

            public void onClick(View v) {
                if (parent.kumiwake_select == false) {
                    dbAdapter.open();     // DBの読み込み(読み書きの方)
                    for (int i = 1; i < ListCount; i+=2) {
                        boolean checked = booleanArray.get(i);
                        listItem = nameList.get(i);
                        int listId = listItem.getId();
                        int newId = parent.groupId;
                        if (checked == true) {
                            StringBuilder newBelong = new StringBuilder();
                            newBelong.append(listItem.getBelong());
                            newBelong.append("," + newId);
                            dbAdapter.addBelong(String.valueOf(listId), newBelong.toString());
                        } else {
                            DeleteBelongInfo(newId, listId);
                        }
                    }
                    dbAdapter.close();    // DBを閉じる
                    loadName();
                    parent.finish();
                } else {
                    recreateKumiwakeList();
                    parent.moveKumiwake();
                }
                listAdapter.clearSelection();
            }

        };

        public void DeleteMember() {
            // アラートダイアログ表示
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setTitle(checkedCount + " " + getString(R.string.member) + getString(R.string.delete));
            builder.setMessage(R.string.Do_delete);
            // OKの時の処理
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dbAdapter.open();     // DBの読み込み(読み書きの方)
                    for (int i = 1; i < ListCount; i+=2) {
                        boolean checked = booleanArray.get(i);
                        if (checked == true) {
                            // IDを取得する
                            listItem = nameList.get(i);
                            int listId = listItem.getId();
                            dbAdapter.selectDelete(String.valueOf(listId));     // DBから取得したIDが入っているデータを削除する
                        }
                    }
                    dbAdapter.close();    // DBを閉じる
                    listAdapter.clearSelection();
                    parent.reload();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();

            getListView().setTextFilterEnabled(true);
        }

        public void recreateKumiwakeList() {
            parent.memberArray.clear();
            dbAdapter.open();
            for (int i = 1; i < ListCount; i+=2) {
                boolean checked = booleanArray.get(i);
                listItem = nameList.get(i);
                if (checked == true&&!listItem.getSex().equals("initial")) {
                    parent.memberArray.add(listItem);
                }
            }
            dbAdapter.close();
        }
    }

    public static ArrayList<Name> searchBelong(String belongId) {
        ArrayList<Name> memberArrayByBelong = new ArrayList<Name>();
        dbAdapter.open();
        for (int i = 1; i < ListCount; i+=2) {
            listItem = nameList.get(i);
            String listName = listItem.getName();
            String listSex = listItem.getSex();
            String belongText = listItem.getBelong();
            if (belongText != null) {
                String[] belongArray = belongText.split(",");
                if (Arrays.asList(belongArray).contains(belongId)) {
                    memberArrayByBelong.add(new Name(0, listName,listSex, 0, 0, null, null,null));
                }
            }
        }
        dbAdapter.close();
        loadName();
        return memberArrayByBelong;
    }

    public static void DuplicateBelong() {
        dbAdapter.open();
        for (int i = 1; i < ListCount; i+=2) {
            listItem = nameList.get(i);
            int listId = listItem.getId();
            String belongText = listItem.getBelong();
            if (belongText != null) {
                String[] belongArray = belongText.split(",");
                List<String> list = new ArrayList<String>(Arrays.asList(belongArray));
                Set<String> hs = new HashSet<>();
                hs.addAll(list);
                list.clear();
                list.addAll(hs);
                StringBuilder newBelong = new StringBuilder();

                for (int j = 0; j < list.size(); j++) {
                    newBelong.append(list.get(j));
                    if (j != list.size() - 1) {
                        newBelong.append(",");
                    }
                }
                dbAdapter.addBelong(String.valueOf(listId), newBelong.toString());
            }
        }
        dbAdapter.close();
        loadName();
    }

    public static void DeleteBelongInfo(int groupId, int listId) {
        String belongText = listItem.getBelong();
        if(belongText!=null) {
            String[] belongArray = belongText.split(",");
            List<String> list = new ArrayList<String>(Arrays.asList(belongArray));
            Set<String> hs = new HashSet<>();
            hs.addAll(list);
            list.clear();
            list.addAll(hs);
            if (list.contains(String.valueOf(groupId))) {
                list.remove(String.valueOf(groupId));
                StringBuilder newBelong = new StringBuilder();

                for (int j = 0; j < list.size(); j++) {
                    newBelong.append(list.get(j));
                    if (j != list.size() - 1) {
                        newBelong.append(",");
                    }
                }
                dbAdapter.addBelong(String.valueOf(listId), newBelong.toString());
            }
        }
    }

    public static void DeleteBelongInfoAll(int groupId) {
        dbAdapter.open();
        for (int i = 1; i < ListCount; i++) {
            listItem = nameList.get(i);
            int listId = listItem.getId();
            DeleteBelongInfo(groupId, listId);
        }
        dbAdapter.close();
        loadName();
    }

    public static void addGroupByGroup(int newId, int myId) {
        dbAdapter.open();
        for (int i = 1; i < ListCount; i+=2) {
            listItem = nameList.get(i);
            int listId = listItem.getId();
            String belongText = listItem.getBelong();
            String[] belongArray = belongText.split(",");
            List<String> list = new ArrayList<String>(Arrays.asList(belongArray));
            Set<String> hs = new HashSet<>();
            hs.addAll(list);
            list.clear();
            list.addAll(hs);
            if (list.contains(String.valueOf(myId))) {
                StringBuilder newBelong = new StringBuilder();
                for (int j = 0; j < list.size(); j++) {
                    newBelong.append(list.get(j));
                    if (j != list.size() - 1) {
                        newBelong.append(",");
                    }
                }
                newBelong.append("," + newId);
                dbAdapter.addBelong(String.valueOf(listId), newBelong.toString());
            }
        }
        dbAdapter.close();
        loadName();
    }

    public static void createKumiwakeListByGroup(int groupId) {
        dbAdapter.open();
        for (int i = 1; i < ListCount; i+=2) {
            listItem = nameList.get(i);
            String belongText = listItem.getBelong();
            if (belongText != null) {
                String[] belongArray = belongText.split(",");
                List<String> list = new ArrayList<String>(Arrays.asList(belongArray));
                if (list.contains(String.valueOf(groupId))) {
                    parent.memberArray.add(listItem);
                }
            }
        }
        dbAdapter.close();
    }

}






