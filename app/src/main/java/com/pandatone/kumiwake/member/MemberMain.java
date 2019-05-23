package com.pandatone.kumiwake.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.adapter.CustomPagerAdapter;
import com.pandatone.kumiwake.kumiwake.NormalMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by atsushi_2 on 2016/02/19.
 */
public class MemberMain extends AppCompatActivity implements SearchView.OnQueryTextListener {

    FragmentManager manager = getSupportFragmentManager();
    private static Context context;
    static Button decision;
    static boolean start_actionmode, delete_icon_visible, kumiwake_select;
    static int groupId, page;
    static ArrayList<Name> memberArray;
    static SearchView searchView;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.member_main);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setViews();
        setKeyboardListener();
        context = getApplicationContext();
        Intent i = getIntent();
        start_actionmode = i.getBooleanExtra("START_ACTIONMODE", false);
        groupId = i.getIntExtra("GROUP_ID", -1);
        delete_icon_visible = i.getBooleanExtra("delete_icon_visible", true);
        kumiwake_select = i.getBooleanExtra("kumiwake_select", false);
        memberArray = (ArrayList<Name>) i.getSerializableExtra("memberArray");
    }

    private void setViews() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(manager);
        viewPager.setAdapter(adapter);
        decision = (Button) findViewById(R.id.decisionBt);
    }

    private void visibleViews() {
        if (getIntent().getBooleanExtra("visible", false)) {
            decision.setVisibility(View.VISIBLE);
            FragmentGroup.adviceInFG.setVisibility(View.VISIBLE);
            FragmentMember.fab.hide();
            FragmentGroup.fab.hide();
            FragmentMember.fab.setEnabled(false);
            FragmentGroup.fab.setEnabled(false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.member_main_menu, menu);
        MenuItem delete = menu.findItem(R.id.item_delete);
        delete.setVisible(false);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                page = viewPager.getCurrentItem();
                MenuItem itemfilter = menu.findItem(R.id.item_filter);
                if (page == 1) {
                    itemfilter.setVisible(false);
                } else {
                    itemfilter.setVisible(true);
                }
            }
        });

        searchView =
                (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_view));
        searchView.setOnQueryTextListener(this);
        final SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        // ActionBarの検索アイコン
        ImageView searchIcon =
                (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchIcon.setImageResource(android.R.drawable.ic_menu_search);
        SpannableStringBuilder ssb = new SpannableStringBuilder("　");
        // ヒントテキスト
        ssb.append(getText(R.string.search_view));
        // ヒントアイコン
        Drawable searchHintIcon = getResources().getDrawable(android.R.drawable.ic_menu_search);
        int textSize = (int) (searchAutoComplete.getTextSize() * 1.25);
        searchHintIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchHintIcon), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchAutoComplete.setHint(ssb);
        // テキストカラー
        searchAutoComplete.setTextColor(Color.WHITE);
        // ヒントテキストカラー
        searchAutoComplete.setHintTextColor(Color.parseColor("#40000000"));
        // Remove button icon
        ImageView removeIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        removeIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchAutoComplete.getText().toString().equals("")) {
                    searchAutoComplete.setText("");
                } else {
                    searchView.onActionViewCollapsed();
                    FragmentMember.loadName();
                    FragmentGroup.loadName();
                }
            }
        });

        visibleViews();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(manager);
        adapter.findFragmentByPosition(viewPager, page).onOptionsItemSelected(item);

        return false;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        reload();
    }

    void moveMember() {
        Intent intent = new Intent(this, AddMember.class);
        startActivity(intent);
    }

    void moveGroup() {
        Intent intent = new Intent(this, AddGroup.class);
        startActivity(intent);
    }

    void moveKumiwake() {
        Set<Name> hs = new HashSet<>();
        hs.addAll(memberArray);
        memberArray.clear();
        memberArray.addAll(hs);
        Intent i = new Intent(this, NormalMode.class);
        i.putExtra("memberArray", memberArray);
        setResult(Activity.RESULT_OK, i);
        finish();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        try {
            if (page == 0) {
                FragmentMember.selectName(newText);
            } else {
                FragmentGroup.selectGroup(newText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void reload() {
        Intent i = new Intent(this, MemberMain.class);
        finish();
        startActivity(i);
    }

    public static Context getContext() {
        return context;
    }

    public final void setKeyboardListener() {
        final View activityRootView = (findViewById(R.id.member_layout));
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect r = new Rect();

            @Override
            public void onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r);
                // 画面の高さとビューの高さを比べる
                int heightDiff = activityRootView.getRootView().getHeight() - r.height();
                if (heightDiff > 100) {
                    FragmentMember.fab.setVisibility(View.INVISIBLE);
                    FragmentGroup.fab.setVisibility(View.INVISIBLE);
                } else {
                    FragmentMember.fab.setVisibility(View.VISIBLE);
                    FragmentGroup.fab.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}