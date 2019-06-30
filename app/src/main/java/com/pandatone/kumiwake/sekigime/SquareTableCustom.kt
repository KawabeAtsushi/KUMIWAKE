package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import kotlinx.android.synthetic.main.square_custom.*

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class SquareTableCustom : AppCompatActivity() {
    internal var seatNo = 0
    internal var mingroupNo = 1000
    internal var doubleDeploy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.square_custom)
        ButterKnife.bind(this)

        if (SekigimeResult.Normalmode) {
            val array = SekigimeResult.arrayArrayNormal
            for (i in array.indices) {
                if (array[i].size < mingroupNo) {
                    mingroupNo = array[i].size
                }
            }
        } else {
            val array = SekigimeResult.arrayArrayQuick
            for (i in array.indices) {
                if (array[i].size < mingroupNo) {
                    mingroupNo = array[i].size
                }
            }
        }
        square_seek_bar.max = mingroupNo / 4
        if (mingroupNo < 4) {
            square_seek_bar.isEnabled = false
        }
        doubleSquareType.setOnCheckedChangeListener { _, _ ->
            if (doubleSquareType.isChecked) {
                square_seek_bar.max = mingroupNo / 4
                if (mingroupNo < 4) {
                    square_seek_bar.isEnabled = false
                }
            } else {
                square_seek_bar.max = mingroupNo / 3
                square_seek_bar.isEnabled = mingroupNo >= 3
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

    @OnClick(R.id.move_result)
    internal fun onClicked() {
        SekigimeResult.square_no = seatNo
        doubleDeploy = doubleSquareType.isChecked
        SekigimeResult.doubleDeploy = doubleDeploy
        val intent = Intent(applicationContext, SekigimeResult::class.java)
        startActivity(intent)
    }
}
