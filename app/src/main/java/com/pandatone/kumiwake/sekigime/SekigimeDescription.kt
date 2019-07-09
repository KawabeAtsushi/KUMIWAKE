package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.kumiwake.KumiwakeSelectMode
import com.pandatone.kumiwake.kumiwake.MainActivity
import kotlinx.android.synthetic.main.sekigime_description.*


/**
 * Created by atsushi_2 on 2016/07/15.
 */
class SekigimeDescription : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sekigime_description)
        val toolbar = findViewById<View>(R.id.tool_bar_3) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.sekigime)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        KumiwakeSelectMode.sekigime = true
        val start = findViewById<View>(R.id.sekigime_start) as Button
        start.setOnClickListener {
            val intent = Intent(applicationContext, KumiwakeSelectMode::class.java)
            startActivity(intent)
        }

        MainActivity().colorChanger(sekigime_description_layout,R.color.red_background,R.color.red)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // いつものUPナビゲーションの処理
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
