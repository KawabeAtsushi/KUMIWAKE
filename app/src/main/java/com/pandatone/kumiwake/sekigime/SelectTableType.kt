package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.sekigime.function.CustomDialogSekigime
import com.pandatone.kumiwake.sekigime.function.DrawTableView

/**
 * Created by atsushi_2 on 2016/07/13.
 */
class SelectTableType : AppCompatActivity() {

    private lateinit var inflater: LayoutInflater
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_sekigime_type)

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        findViewById<ImageButton>(R.id.square_table).setOnClickListener { onSquareClicked() }
        findViewById<ImageButton>(R.id.parallel_table).setOnClickListener { onParallelClicked() }
        findViewById<ImageButton>(R.id.circle_table).setOnClickListener { onCircleClicked() }
        findViewById<ImageButton>(R.id.counter_table).setOnClickListener { onCounterClicked() }
    }

    private fun onSquareClicked() {
        DrawTableView.tableType = "square"
        title = getString(R.string.square_table)
        confirmationDialog(title, 1)
    }

    private fun onParallelClicked() {
        DrawTableView.tableType = "parallel"
        title = getString(R.string.parallel_table)
        confirmationDialog(title, 2)
    }

    private fun onCircleClicked() {
        DrawTableView.tableType = "circle"
        title = getString(R.string.circle_table)
        confirmationDialog(title, 3)
    }

    private fun onCounterClicked() {
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
