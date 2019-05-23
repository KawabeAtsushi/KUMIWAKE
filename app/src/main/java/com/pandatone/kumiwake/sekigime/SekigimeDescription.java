package com.pandatone.kumiwake.sekigime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.kumiwake.KumiwakeSelectMode;

/**
 * Created by atsushi_2 on 2016/07/15.
 */
public class SekigimeDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sekigime_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.sekigime);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        KumiwakeSelectMode.sekigime = true;
        Button start = (Button) findViewById(R.id.sekigime_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), KumiwakeSelectMode.class);
                startActivity(intent);
            }
        });
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
}
