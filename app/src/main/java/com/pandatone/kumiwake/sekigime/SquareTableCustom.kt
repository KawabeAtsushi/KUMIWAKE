package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.pandatone.kumiwake.R
import kotlinx.android.synthetic.main.square_custom.*
import kotlin.math.ceil

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class SquareTableCustom : AppCompatActivity() {
    private var seatNo = 0
    private var minGroupNo = 1000
    private var doubleDeploy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.square_custom)
        findViewById<Button>(R.id.move_result).setOnClickListener { onNext() }

        val array = SekigimeResult.teamArray
        for (item in array) {
            val size = item.size
            if (size < minGroupNo) {
                minGroupNo = size
            }
        }

        square_seek_bar.max = minGroupNo / 4
        if (minGroupNo < 4) {
            square_seek_bar.isEnabled = false
        }
        doubleSquareType.setOnCheckedChangeListener { _, _ ->
            if (doubleSquareType.isChecked) {
                square_seek_bar.max = minGroupNo / 4
                if (minGroupNo < 4) {
                    square_seek_bar.isEnabled = false
                }
            } else {
                square_seek_bar.max = ceil(minGroupNo / 3.toDouble()).toInt()
                square_seek_bar.isEnabled = minGroupNo >= 3
            }
        }


        square_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(square_seek_bar: SeekBar) {}

            override fun onProgressChanged(square_seek_bar: SeekBar, progress: Int, fromTouch: Boolean) {
                seatNo = progress
                seat_number.text = seatNo.toString()
            }

            override fun onStopTrackingTouch(square_seek_bar: SeekBar) {}
        })
    }

    private fun onNext() {
        SekigimeResult.square_no = seatNo
        doubleDeploy = doubleSquareType.isChecked
        SekigimeResult.doubleDeploy = doubleDeploy
        val intent = Intent(applicationContext, SekigimeResult::class.java)
        startActivity(intent)
    }
}
