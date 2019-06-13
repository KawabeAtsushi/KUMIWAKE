package com.pandatone.kumiwake.sekigime

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button

import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.kumiwake.KumiwakeSelectMode

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
