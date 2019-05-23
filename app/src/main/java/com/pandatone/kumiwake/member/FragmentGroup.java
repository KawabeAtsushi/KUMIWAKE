package com.pandatone.kumiwake.member;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.GroupNameListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by atsushi_2 on 2016/02/23.
 */
public class FragmentGroup extends ListFragment {
    private MemberMain parent;
    static GroupListAdapter dbAdapter;
    static GroupNameListAdapter listAdapter;
    static List<GroupListAdapter.Group> nameList;
    static GroupListAdapter.Group listItem;
    static FloatingActionButton fab;
    static TextView adviceInFG;
    static int ListCount, checkedCount=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter = new GroupListAdapter(getActivity());
        nameList = new ArrayList<>();
        listAdapter = new GroupNameListAdapter(getActivity(), nameList);
        setListAdapter(listAdapter);
        loadName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_group, container, false);
        adviceInFG = (TextView) view.findViewById(R.id.advice_in_fg);
        fab = (FloatingActionButton) view.findViewById(R.id.group_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.moveGroup();
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListCount = getListView().getCount();
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new Callback());
        getListView().setFastScrollEnabled(true);
        ListCount = getListView().getCount();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //行をクリックした時の処理
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                final android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view2 = inflater.inflate(R.layout.group_info,
                        (ViewGroup) getActivity().findViewById(R.id.info_layout));
                final String groupname = nameList.get(position).getGroup();
                if (MemberMain.searchView.isActivated() == true)
                    MemberMain.searchView.onActionViewCollapsed();
                FragmentMember.loadName();

                final String[] Items =
                        {Sort.name_getContext().getString(R.string.information),
                                Sort.name_getContext().getString(R.string.edit),
                                Sort.name_getContext().getString(R.string.delete),};
                builder.setTitle(groupname);
                builder.setItems(Items, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                GroupClick.GroupInfoDialog(view2, builder2);
                                GroupClick.SetInfo(position);
                                final android.app.AlertDialog dialog2 = builder2.create();
                                dialog2.show();
                                GroupClick.okBt.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        dialog2.dismiss();
                                    }
                                });
                                break;
                            case 1:
                                Intent i = new Intent(getActivity(), AddGroup.class);
                                i.putExtra("POSITION", position);
                                startActivity(i);
                                break;
                            case 2:
                                DeleteSingleGroup(position, groupname);
                                break;

                        }
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if (parent.start_actionmode == true) {
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                //行をクリックした時の処理
                public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                    getListView().startActionMode(new Callback());
                    getListView().setItemChecked(position, !listAdapter.isPositionChecked(position));
                }
            });
        }

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
        // アクションアイテム選択時
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;

            case R.id.item_delete:
                DeleteGroup();
                break;

            case R.id.item_all_select:
                for (int i = 0; i < ListCount; i++) {
                    getListView().setItemChecked(i, true);
                }
                break;

            case R.id.item_sort:
                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                Sort.groupSort(builder);
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
                ListCount = getListView().getCount();
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
    }


    public static void loadName() {
        nameList.clear();
        dbAdapter.open();
        Cursor c = dbAdapter.getAllNames();
        dbAdapter.getCursor(c);
        dbAdapter.close();
    }

    public static void selectGroup(String newText) throws IOException {
        if (TextUtils.isEmpty(newText)) {
            dbAdapter.picGroup(null,null);
        } else {
            dbAdapter.picGroup(newText,newText);
        }

    }

    public void DeleteSingleGroup(final int position, String group) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle(group);
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
                loadName();
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

    public void DeleteGroup() {
        Log.d("DeleteGroup is called.", "hooo");
        // アラートダイアログ表示
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(checkedCount + " " + getString(R.string.group) + getString(R.string.delete));
        builder.setMessage(R.string.Do_delete);
        // OKの時の処理
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SparseBooleanArray list = getListView().getCheckedItemPositions();

                dbAdapter.open();     // DBの読み込み(読み書きの方)
                for (int i = 0; i < ListCount; i++) {
                    boolean checked = list.get(i);
                    if (checked == true) {
                        // IDを取得する
                        listItem = nameList.get(i);
                        int listId = listItem.getId();
                        dbAdapter.selectDelete(String.valueOf(listId));     // DBから取得したIDが入っているデータを削除する
                        FragmentMember.DeleteBelongInfoAll(listId);
                    }
                }
                dbAdapter.close();    // DBを閉じる
                listAdapter.clearSelection();
                getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                loadName();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        getListView().setTextFilterEnabled(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////-------- ActionMode時の処理 ----------///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////


    private class Callback implements ListView.MultiChoiceModeListener {

        final SparseBooleanArray list = getListView().getCheckedItemPositions();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // アクションモード初期化処理
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.member_main_menu, menu);
            menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            parent.decision.setOnClickListener(decision_clicked);
            MenuItem searchIcon = menu.findItem(R.id.search_view);
            MenuItem deleteIcon = menu.findItem(R.id.item_delete);
            MenuItem itemfilter = menu.findItem(R.id.item_filter);
            itemfilter.setVisible(false);
            searchIcon.setVisible(false);
            deleteIcon.setVisible(parent.delete_icon_visible);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // アクションアイテム選択時
            switch (item.getItemId()) {
                case R.id.item_delete:
                    DeleteGroup();
                    break;

                case R.id.item_all_select:
                    for (int i = 0; i < ListCount; i++) {
                        getListView().setItemChecked(i, true);
                    }
                    break;

                case R.id.item_sort:
                    final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    Sort.groupSort(builder);
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                    ListCount = getListView().getCount();
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

                dbAdapter.open();     // DBの読み込み(読み書きの方)
                if (parent.kumiwake_select == false) {
                    for (int i = 0; i < ListCount; i++) {
                        boolean checked = list.get(i);
                        if (checked == true) {
                            listItem = nameList.get(i);
                            int myId = listItem.getId();

                            int newId = parent.groupId;
                            FragmentMember.addGroupByGroup(newId, myId);
                        }
                    }
                    parent.finish();
                } else {

                    for (int i = 0; i < ListCount; i++) {
                        boolean checked = list.get(i);
                        if (checked == true) {
                            listItem = nameList.get(i);
                            int myId = listItem.getId();
                            FragmentMember.createKumiwakeListByGroup(myId);
                        }
                    }
                    parent.moveKumiwake();
                }
                listAdapter.clearSelection();
                dbAdapter.close();
            }
        };

    }

}






