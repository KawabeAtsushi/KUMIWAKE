package com.pandatone.kumiwake.member;

import android.content.Context;
import android.content.DialogInterface;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.MemberListAdapter;
import com.pandatone.kumiwake.adapter.NameListAdapter;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by atsushi_2 on 2016/03/30.
 */
public class Sort {

    public static final String ASC = "ASC";   //昇順 (1.2.3....)
    public static final String DESC = "DESC"; //降順 (3.2.1....)
    static MemberListAdapter dbAdapter = new MemberListAdapter(name_getContext());
    static GroupListAdapter gpdbAdapter = new GroupListAdapter(name_getContext());
    public static String NS;
    static int initial = 2;

    public static void memberSort(android.support.v7.app.AlertDialog.Builder builder) {

        final String[] Items =
                {name_getContext().getString(R.string.name_ascending),
                        name_getContext().getString(R.string.name_descending),
                        name_getContext().getString(R.string.registration_ascending),
                        name_getContext().getString(R.string.registration_descending),
                        name_getContext().getString(R.string.age_ascending),
                        name_getContext().getString(R.string.age_descending),
                        name_getContext().getString(R.string.grade_ascending),
                        name_getContext().getString(R.string.grade_descending),};
        builder.setTitle(R.string.sorting);
        builder.setSingleChoiceItems(Items, initial, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initial = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (initial) {
                    case 0:
                        NS="NAME";
                        dbAdapter.sortNames(dbAdapter.MB_NAME_READ, ASC);
                        break;
                    case 1:
                        NS="NAME";
                        dbAdapter.sortNames(dbAdapter.MB_NAME_READ, DESC);
                        break;
                    case 2:
                        NS="ID";
                        dbAdapter.sortNames(dbAdapter.MB_ID, ASC);
                        break;
                    case 3:
                        NS="ID";
                        dbAdapter.sortNames(dbAdapter.MB_ID, DESC);
                        break;
                    case 4:
                        NS="AGE";
                        dbAdapter.sortNames(dbAdapter.MB_AGE, ASC);
                        break;
                    case 5:
                        NS="AGE";
                        dbAdapter.sortNames(dbAdapter.MB_AGE, DESC);
                        break;
                    case 6:
                        NS="GRADE";
                        dbAdapter.sortNames(dbAdapter.MB_GRADE, ASC);
                        break;
                    case 7:
                        NS="GRADE";
                        dbAdapter.sortNames(dbAdapter.MB_GRADE, DESC);
                        break;
                }
                NameListAdapter.nowSort=NS;
                dbAdapter.notifyDataSetChanged();

            }
        });
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

// back keyを使用不可に設定
        builder.setCancelable(false);

    }

    public static void groupSort(android.support.v7.app.AlertDialog.Builder builder) {

        final String[] Items =
                {name_getContext().getString(R.string.name_ascending),
                        name_getContext().getString(R.string.name_descending),
                        name_getContext().getString(R.string.registration_ascending),
                        name_getContext().getString(R.string.registration_descending),
                        name_getContext().getString(R.string.member_ascending),
                        name_getContext().getString(R.string.member_descending)};
        builder.setTitle(R.string.sorting);
        builder.setSingleChoiceItems(Items, initial, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                initial = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (initial) {
                    case 0:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_NAME, ASC);
                        break;
                    case 1:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_NAME, DESC);
                        break;
                    case 2:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_ID, ASC);
                        break;
                    case 3:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_ID, DESC);
                        break;
                    case 4:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_BELONG, ASC);
                        break;
                    case 5:
                        gpdbAdapter.sortGroups(gpdbAdapter.GP_BELONG, DESC);
                        break;
                }

                gpdbAdapter.notifyDataSetChanged();

            }
        });
        // アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

// back keyを使用不可に設定
        builder.setCancelable(false);

    }

    /*
    public static void NameSort(String sortType) {

        if (sortType.equals(ASC)) {
            Collections.sort(FragmentMember.nameList, new Comparator<Name>() {
                @Override
                public int compare(Name n1, Name n2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(n1.getName(), n2.getName());
                }
            });
        }

        if (sortType.equals(DESC)) {
            Collections.sort(FragmentMember.nameList, new Comparator<Name>() {
                @Override
                public int compare(Name n1, Name n2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(n2.getName(), n1.getName());
                }
            });
        }

        FragmentMember.listAdapter.notifyDataSetChanged();
    }

    public static void GroupSort(String sortType) {

        if (sortType.equals(ASC)) {
            Collections.sort(FragmentGroup.nameList, new Comparator<GroupListAdapter.Group>() {
                @Override
                public int compare(GroupListAdapter.Group g1, GroupListAdapter.Group g2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(g1.getGroup(), g2.getGroup());
                }
            });
        }

        if (sortType.equals(DESC)) {
            Collections.sort(FragmentGroup.nameList, new Comparator<GroupListAdapter.Group>() {
                @Override
                public int compare(GroupListAdapter.Group g1, GroupListAdapter.Group g2) {
                    Collator collator = Collator.getInstance(Locale.JAPANESE);
                    return collator.compare(g2.getGroup(), g1.getGroup());
                }
            });
        }

        FragmentMember.listAdapter.notifyDataSetChanged();
    }
    */

    public static Context name_getContext() {
        return MemberMain.getContext();
    }
}
