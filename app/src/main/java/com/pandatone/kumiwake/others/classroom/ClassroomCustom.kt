package com.pandatone.kumiwake.others.classroom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.pandatone.kumiwake.ClassroomCustomKeys
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.function.Member
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class ClassroomCustom : AppCompatActivity() {
    private var seatNo = 0
    private var columnCount = 0
    private var rowCount = 0
    private lateinit var memberArray: ArrayList<Member>
    private lateinit var attachCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classroom_custom)
        findViewById<Button>(R.id.move_result).setOnClickListener { onNext() }

        if (intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = intent.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }

        seatNo = memberArray.size

        attachCheckBox = findViewById(R.id.attach_seat_check)

        val rowSeekBar = findViewById<SeekBar>(R.id.row_seat_seek_bar)
        rowSeekBar.max = seatNo
        rowSeekBar.progress = sqrt(seatNo.toDouble()).toInt()

        val seatCountTv = findViewById<TextView>(R.id.row_seat_number)
        columnCount = rowSeekBar.progress
        rowCount = ceil(seatNo.toDouble() / columnCount.toDouble()).toInt()
        seatCountTv.text = columnCount.toString()
        drawPreview(attachCheckBox.isChecked)

        rowSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(rowSeekBar: SeekBar) {}

            override fun onProgressChanged(rowSeekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                if (progress == 0) {
                    rowSeekBar.progress = 1
                } else {
                    columnCount = progress
                    rowCount = ceil(seatNo.toDouble() / columnCount.toDouble()).toInt()
                    seatCountTv.text = columnCount.toString()
                }
            }

            override fun onStopTrackingTouch(rowSeekBar: SeekBar) {
                drawPreview(attachCheckBox.isChecked)
            }
        })

        attachCheckBox.setOnCheckedChangeListener { _, isChecked -> drawPreview(isChecked) }

    }

    private fun drawPreview(attachSeat: Boolean) {
        val gridLayout = findViewById<GridLayout>(R.id.seats_preview_layout)
        gridLayout.removeAllViews()
        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount
        var seatCount = 0

        for (row in 0 until gridLayout.rowCount) {
            for (column in 0 until gridLayout.columnCount) {
                val seat = layoutInflater.inflate(R.layout.classroom_seats_preview, null)
                val params: GridLayout.LayoutParams = GridLayout.LayoutParams()
                params.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
                params.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
                seat.layoutParams = params
                if (attachSeat) {
                    if (column % 2 == 0) {
                        seat.updatePadding(right = 0)
                        if (column == ClassroomResult.columnCount - 1) {
                            seat.updatePadding(right = 6)
                        }
                    } else {
                        seat.updatePadding(left = 0)
                    }

                }
                gridLayout.addView(seat)
                seatCount++
                if (seatCount == seatNo) break
            }
            if (seatCount == seatNo) break
        }
    }

    private fun onNext() {
        val alterCheckBox = findViewById<CheckBox>(R.id.alter_fm_check)
        ClassroomResult.columnCount = columnCount
        ClassroomResult.rowCount = rowCount
        val intent = Intent(applicationContext, ClassroomResult::class.java)
        intent.putExtra(KumiwakeArrayKeys.MEMBER_LIST.key, memberArray)
        intent.putExtra(ClassroomCustomKeys.ALTER_FM_SEAT.key, alterCheckBox.isChecked)
        intent.putExtra(ClassroomCustomKeys.ATTACH_SEAT.key, attachCheckBox.isChecked)
        startActivity(intent)
        overridePendingTransition(R.anim.in_right, R.anim.out_left)
    }
}
