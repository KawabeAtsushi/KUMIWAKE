package com.pandatone.kumiwake.kumiwake

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.sekigime.SekigimeResult

/**
 * Created by atsushi_2 on 2016/02/19.
 */
class KumiwakeSelectMode : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_select_mode)

        val toolbar = findViewById<View>(R.id.tool_bar_1) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(R.string.kumiwake)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val normalmode: TextView = findViewById<TextView>(R.id.descriptionForNormalMode)
        val quickmode: TextView = findViewById<TextView>(R.id.descriptionForQuickMode)
        if (sekigime) {
            toolbar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.green_title))
            val layout = findViewById<View>(R.id.select_mode_layout) as LinearLayout
            layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.green_background))
            normalmode.setText(R.string.description_of_normal_sekigime)
            quickmode.setText(R.string.description_of_quick_sekigime)
            supportActionBar!!.setTitle(R.string.select_mode)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
        ButterKnife.bind(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // いつものUPナビゲーションの処理
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.normal_mode_button)
    internal fun onNormalModeClicked() {
        SekigimeResult.Normalmode = true
        startActivity(Intent(this, NormalMode::class.java))
    }

    @OnClick(R.id.quick_mode_button)
    internal fun onQuickModeClicked() {
        SekigimeResult.Normalmode = false
        startActivity(Intent(this, QuickMode::class.java))
    }

    companion object {
        var sekigime = false
    }
}

