package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.customize.CustomDialogSekigime

/**
 * Created by atsushi_2 on 2016/07/13.
 */
class SelectTableType : AppCompatActivity() {

    private lateinit var inflater: LayoutInflater
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_sekigime_type)
        ButterKnife.bind(this)
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    @OnClick(R.id.square_table)
    internal fun onSquareClicked() {
        DrawTableView.tableType = "square"
        title = getString(R.string.square_table)
        confirmationDialog(title, 1)
    }

    @OnClick(R.id.parallel_table)
    internal fun onParallelClicked() {
        DrawTableView.tableType = "parallel"
        title = getString(R.string.parallel_table)
        confirmationDialog(title, 2)
    }

    @OnClick(R.id.circle_table)
    internal fun onCircleClicked() {
        DrawTableView.tableType = "circle"
        title = getString(R.string.circle_table)
        confirmationDialog(title, 3)
    }

    @OnClick(R.id.counter_table)
    internal fun onCounterClicked() {
        DrawTableView.tableType = "counter"
        title = getString(R.string.counter_table)
        confirmationDialog(title, 4)
    }

    private fun confirmationDialog(title: String, position: Int) {
        val customDialog = CustomDialogSekigime()
        customDialog.setTitle(title)
        customDialog.setPosition(position)
        customDialog.show(supportFragmentManager, "Btn")
    }
}
