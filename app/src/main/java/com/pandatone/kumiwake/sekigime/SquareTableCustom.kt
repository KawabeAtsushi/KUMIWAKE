package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.databinding.SquareCustomBinding
import kotlin.math.ceil

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class SquareTableCustom : AppCompatActivity() {
    private lateinit var binding: SquareCustomBinding

    private var seatNo = 0
    private var minGroupNo = 1000
    private var doubleDeploy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SquareCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViewById<Button>(R.id.move_result).setOnClickListener { onNext() }

        val array = SekigimeResult.teamArray
        for (item in array) {
            val size = item.size
            if (size < minGroupNo) {
                minGroupNo = size
            }
        }

        binding.squareSeekBar.max = minGroupNo / 4
        if (minGroupNo < 4) {
            binding.squareSeekBar.isEnabled = false
        }
        binding.doubleSquareType.setOnCheckedChangeListener { _, _ ->
            if (binding.doubleSquareType.isChecked) {
                binding.squareSeekBar.max = minGroupNo / 4
                if (minGroupNo < 4) {
                    binding.squareSeekBar.isEnabled = false
                }
            } else {
                binding.squareSeekBar.max = ceil(minGroupNo / 3.toDouble()).toInt()
                binding.squareSeekBar.isEnabled = minGroupNo >= 3
            }
        }


        binding.squareSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(squareSeekBar: SeekBar) {}

            override fun onProgressChanged(
                squareSeekBar: SeekBar,
                progress: Int,
                fromTouch: Boolean
            ) {
                seatNo = progress
                binding.seatNumber.text = seatNo.toString()
            }

            override fun onStopTrackingTouch(squareSeekBar: SeekBar) {}
        })
    }

    private fun onNext() {
        SekigimeResult.square_no = seatNo
        doubleDeploy = binding.doubleSquareType.isChecked
        SekigimeResult.doubleDeploy = doubleDeploy
        val intent = Intent(applicationContext, SekigimeResult::class.java)
        startActivity(intent)
    }
}
