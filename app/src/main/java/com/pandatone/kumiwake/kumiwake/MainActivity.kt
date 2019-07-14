package com.pandatone.kumiwake.kumiwake

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.MyApplication
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.setting.SettingHelp
import java.util.*


class MainActivity : Activity(), View.OnClickListener {
    private lateinit var mCategories: List<Category>
    private var kumiwake: Category? = null
    private var member: Category? = null
    private var sekigime: Category? = null
    private var setting: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Explode()
        }
        setContentView(R.layout.activity_main)
        setViews()

        MobileAds.initialize(this, "ca-app-pub-2315101868638564~1560987130")
        //MobileAds.initialize(getApplicationContext(), "ca-app-pub-2315101868638564/8665451539");
        val mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build()
        mAdView.loadAd(adRequest)

    }

    override fun onClick(v: View) {
        val category: Category = when (v.id) {
            R.id.category_item1 -> mCategories[0]
            R.id.category_item2 -> mCategories[1]
            R.id.category_item3 -> mCategories[2]
            else -> mCategories[3]
        }
        val res = resources
        val mPackageName = packageName
        val title: TextView
        title = v.findViewById<View>(res.getIdentifier("category_title" + category.id, "id", mPackageName)) as TextView
        startActivityWithTransition(category.id, title)
    }

    private fun setViews() {
        var v: FrameLayout
        var icon: ImageView
        var title: TextView

        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y
        val halfScreenWidth = (screenWidth * 0.5).toInt()
        val miniScreenHeight = (screenHeight * 0.34).toInt()

        val res = resources
        val mPackageName = packageName
        kumiwake = Category(getString(R.string.kumiwake), "1", Theme.red)
        member = Category(getString(R.string.member), "2", Theme.blue)
        sekigime = Category(getString(R.string.sekigime), "3", Theme.green)
        setting = Category(getString(R.string.setting_help), "4", Theme.yellow)
        mCategories = Arrays.asList<Category>(kumiwake, member, sekigime, setting)

        for (i in 0..3) {
            val category = mCategories[i]
            v = findViewById<View>(res.getIdentifier("category_item" + category.id!!, "id", mPackageName)) as FrameLayout
            icon = v.findViewById<View>(res.getIdentifier("category_icon" + category.id, "id", mPackageName)) as ImageView
            title = v.findViewById<View>(res.getIdentifier("category_title" + category.id, "id", mPackageName)) as TextView

            v.layoutParams.width = halfScreenWidth
            v.layoutParams.height = miniScreenHeight
            v.setBackgroundColor(ContextCompat.getColor(applicationContext, category.theme.windowBackgroundColor))
            icon.setImageResource(res.getIdentifier("icon_" + category.id, "drawable", mPackageName))
            title.text = category.name
            title.setTextColor(ContextCompat.getColor(applicationContext, category.theme.textPrimaryColor))
            title.setBackgroundColor(ContextCompat.getColor(applicationContext, category.theme.primaryColor))
            v.setOnClickListener(this)
        }
    }

    private fun startActivityWithTransition(id: String?, tv: TextView) {
        val intent: Intent

        when (id) {
            "1" -> {
                KumiwakeSelectMode.sekigime = false
                intent = Intent(this, KumiwakeSelectMode::class.java)
            }
            "2" -> intent = Intent(this, MemberMain::class.java)
            "3" -> {
                KumiwakeSelectMode.sekigime = true
                intent = Intent(this, KumiwakeSelectMode::class.java)
            }
            else -> intent = Intent(this, SettingHelp::class.java)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, tv, "explodeTv").toBundle())
        } else {
            startActivity(intent)
        }
    }

    fun colorChanger(layout: LinearLayout, color1_id: Int, color2_id: Int) {
        val color1 = ContextCompat.getColor(MyApplication.context!!, color1_id)
        val color2 = ContextCompat.getColor(MyApplication.context!!, color2_id)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), color1, color2)
        colorAnimation.addUpdateListener { animator -> layout.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.duration = 10000
        colorAnimation.interpolator = LinearInterpolator()
        colorAnimation.repeatMode = ValueAnimator.REVERSE
        colorAnimation.repeatCount = ValueAnimator.INFINITE
        colorAnimation.start()
    }

}
