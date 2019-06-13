package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class SquareTableCustom : AppCompatActivity() {
    internal var squareRadioGroup: RadioGroup
    internal var doubleDeployButton: RadioButton
    internal var seekBar: SeekBar
    internal var noText: TextView
    internal var seatNo = 0
    internal var mingroupNo = 1000
    internal var doubleDeploy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.square_custom)
        ButterKnife.bind(this)
        squareRadioGroup = findViewById<View>(R.id.squareGroup)
        doubleDeployButton = findViewById<View>(R.id.doubleSquareType)
        seekBar = findViewById<View>(R.id.square_seek_bar)
        noText = findViewById<View>(R.id.seat_number)

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
        seekBar.max = mingroupNo / 4
        if (mingroupNo < 4) {
            seekBar.isEnabled = false
        }
        doubleDeployButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (doubleDeployButton.isChecked == true) {
                seekBar.max = mingroupNo / 4
                if (mingroupNo < 4) {
                    seekBar.isEnabled = false
                }
            } else {
                seekBar.max = mingroupNo / 3
                seekBar.isEnabled = mingroupNo >= 3
            }
        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                seatNo = progress
                noText.text = seatNo.toString()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @OnClick(R.id.move_result)
    internal fun onClicked() {
        SekigimeResult.square_no = seatNo
        doubleDeploy = doubleDeployButton.isChecked
        SekigimeResult.doubleDeploy = doubleDeploy
        val intent = Intent(applicationContext, SekigimeResult::class.java)
        startActivity(intent)
    }
}
