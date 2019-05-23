package com.pandatone.kumiwake.sekigime;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.customize.CustomDialogSekigime;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by atsushi_2 on 2016/07/13.
 */
public class SelectTableType extends AppCompatActivity {

    LayoutInflater inflater;
    String title="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sekigime_type);
        ButterKnife.bind(this);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @OnClick(R.id.square_table)
    void onSquareClicked() {
        DrawTableView.tableType = "square";
        title = getString(R.string.square_table);
        ConfirmationDialog(title,1);
    }

    @OnClick(R.id.parallel_table)
    void onParallelClicked() {
        DrawTableView.tableType = "parallel";
        title = getString(R.string.parallel_table);
        ConfirmationDialog(title,2);
    }

    @OnClick(R.id.circle_table)
    void onCircleClicked() {
        DrawTableView.tableType = "circle";
        title = getString(R.string.circle_table);
        ConfirmationDialog(title,3);
    }

    @OnClick(R.id.counter_table)
    void onCounterClicked() {
        DrawTableView.tableType = "counter";
        title = getString(R.string.counter_table);
        ConfirmationDialog(title,4);
    }

    public void ConfirmationDialog(String title, int position) {
        CustomDialogSekigime customDialog = new CustomDialogSekigime();
        customDialog.setTitle(title);
        customDialog.setPosition(position);
        customDialog.show(getFragmentManager(), "Btn");
    }
}
