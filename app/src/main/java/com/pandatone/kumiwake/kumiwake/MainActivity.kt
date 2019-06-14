package com.pandatone.kumiwake.kumiwake

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.member.MemberMain
import com.pandatone.kumiwake.sekigime.SekigimeDescription
import com.pandatone.kumiwake.setting.SettingHelp
import java.util.*


class MainActivity : Activity(), View.OnClickListener {
    lateinit var mCategories: List<Category>
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
        val category: Category
        val res = resources
        val mPackageName = packageName
        val icon: ImageView
        val title: TextView
        when (v.id) {
            R.id.category_item1 -> category = mCategories[0]
            R.id.category_item2 -> category = mCategories[1]
            R.id.category_item3 -> category = mCategories[2]
            else -> category = mCategories[3]
        }
        icon = v.findViewById<View>(res.getIdentifier(
                "category_icon" + category.id!!, "id", mPackageName)) as ImageView
        title = v.findViewById<View>(res.getIdentifier(
                "category_title" + category.id, "id", mPackageName)) as TextView
        startActivityWithTransition(category.id, icon, title)
    }

    fun setViews() {
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
            v = findViewById<View>(res.getIdentifier(
                    "category_item" + category.id!!, "id", mPackageName)) as FrameLayout
            icon = v.findViewById<View>(res.getIdentifier(
                    "category_icon" + category.id, "id", mPackageName)) as ImageView
            title = v.findViewById<View>(res.getIdentifier(
                    "category_title" + category.id, "id", mPackageName)) as TextView

            v.layoutParams.width = halfScreenWidth
            v.layoutParams.height = miniScreenHeight
            v.setBackgroundColor(resources.getColor(category.theme.windowBackgroundColor))
            icon.setImageResource(res.getIdentifier(
                    "icon_" + category.id, "drawable", mPackageName))
            title.text = category.name
            title.setTextColor(resources.getColor(category.theme.textPrimaryColor))
            title.setBackgroundColor(resources.getColor(category.theme.primaryColor))
            v.setOnClickListener(this)
        }
    }

    private fun startActivityWithTransition(id: String?, iv: ImageView, tv: TextView) {
        val intent: Intent

        when (id) {
            "1" -> {
                intent = Intent(this, KumiwakeSelectMode::class.java)
                KumiwakeSelectMode.sekigime = false
            }
            "2" -> intent = Intent(this, MemberMain::class.java)
            "3" -> intent = Intent(this, SekigimeDescription::class.java)
            else -> intent = Intent(this, SettingHelp::class.java)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, tv, "explodeTv").toBundle())
        } else {
            startActivity(intent)
        }
    }

    companion object {

        val context: Context?
            get() = MemberMain().applicationContext
    }

}
