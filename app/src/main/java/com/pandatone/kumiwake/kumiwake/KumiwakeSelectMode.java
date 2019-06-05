package com.pandatone.kumiwake.kumiwake;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.sekigime.SekigimeResult;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/02/19.
 */
public class KumiwakeSelectMode extends AppCompatActivity {
    public static boolean sekigime = false;
    TextView normalmode,quickmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kumiwake_select_mode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_1);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.kumiwake);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        normalmode=(TextView)findViewById(R.id.descriptionForNormalMode);
        quickmode=(TextView)findViewById(R.id.descriptionForQuickMode);
        if(sekigime){
            toolbar.setBackgroundColor(getResources().getColor(R.color.green_title));
            LinearLayout layout=(LinearLayout) findViewById(R.id.select_mode_layout);
            layout.setBackgroundColor(getResources().getColor(R.color.green_background));
            normalmode.setText(R.string.description_of_normal_sekigime);
            quickmode.setText(R.string.description_of_quick_sekigime);
            getSupportActionBar().setTitle(R.string.select_mode);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        ButterKnife.bind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // いつものUPナビゲーションの処理
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.normal_mode_button) void onNormalModeClicked() {
        SekigimeResult.Normalmode = true;
        startActivity(new Intent(this, NormalMode.class));
    }

    @OnClick(R.id.quick_mode_button) void onQuickModeClicked() {
        SekigimeResult.Normalmode = false;
        startActivity(new Intent(this, QuickMode.class));
    }
}

