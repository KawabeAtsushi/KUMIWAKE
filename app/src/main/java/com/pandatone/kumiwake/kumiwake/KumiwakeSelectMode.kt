package com.pandatone.kumiwake.kumiwake

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.sekigime.SekigimeResult
import kotlinx.android.synthetic.main.add_member.*
import kotlinx.android.synthetic.main.kumiwake_select_mode.*

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

        val layout = findViewById<View>(R.id.select_mode_layout) as LinearLayout

        if (sekigime) {
            toolbar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.green_title))
            layout.background = getDrawable(R.drawable.gradient_anim_sekigime)
            descriptionForNormalMode.setText(R.string.description_of_normal_sekigime)
            descriptionForQuickMode.setText(R.string.description_of_quick_sekigime)
            supportActionBar!!.setTitle(R.string.select_mode)
        }else{
            layout.background = getDrawable(R.drawable.gradient_anim_kumiwake)
        }
        val animDrawable = layout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(resources.getInteger(R.integer.anim_duration))
        animDrawable.setExitFadeDuration(resources.getInteger(R.integer.anim_duration))
        animDrawable.start()

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

