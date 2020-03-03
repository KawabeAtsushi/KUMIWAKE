package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.member.Member
import com.pandatone.kumiwake.ui.DialogWarehouse

/**
 * Created by atsushi_2 on 2016/07/16.
 */
class SekigimeResult : AppCompatActivity() {

    private lateinit var draw:DrawTableView
    private var groupNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scale = resources.displayMetrics.density
        val reLayout = RelativeLayout(this)
        val param1 = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        reLayout.layoutParams = param1
        val param2 = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        param2.addRule(RelativeLayout.CENTER_IN_PARENT)
        param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        param2.setMargins(param2.leftMargin, param2.topMargin, param2.rightMargin, (12 * scale).toInt())
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-2315101868638564~1560987130"
        adView.adSize = AdSize.BANNER
        val adRequest = AdRequest.Builder()
                .addTestDevice("8124DDB5C185E5CA87E826BAB5D4AA10").build()
        adView.loadAd(adRequest)

        groupNo = groupArray!!.size
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        draw = DrawTableView(this)
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val disp = wm.defaultDisplay
        val size = Point()
        disp.getSize(size)
        val groupDropdown = Spinner(this)
        val layout = LinearLayout(this)
        val reSekigime = Button(this)
        val goHome = Button(this)
        val buttonWidth = (180 * scale).toInt()
        val buttonHeight = (50 * scale).toInt()
        val centerX = ((size.x - buttonWidth) / 2).toFloat()
        reSekigime.text = getText(R.string.re_sekigime)
        goHome.text = getText(R.string.go_home)
        val llp1 = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        val llp2 = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        llp1.setMargins(llp1.leftMargin, (10 * scale).toInt(), llp1.rightMargin, (10 * scale).toInt())
        reSekigime.layoutParams = llp1
        reSekigime.translationX = centerX
        llp2.setMargins(llp2.leftMargin, (10 * scale).toInt(), llp2.rightMargin, (90 * scale).toInt())
        goHome.layoutParams = llp2
        goHome.translationX = centerX
        reSekigime.background = ContextCompat.getDrawable(this, R.drawable.simple_orange_button)
        goHome.background = ContextCompat.getDrawable(this, R.drawable.simple_green_button)
        //groupDropdown.background = ContextCompat.getDrawable(this, R.drawable.Dropdown_button)
        reSekigime.setOnClickListener {
            val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
            val title = getString(R.string.re_sekigime_title)
            DialogWarehouse(supportFragmentManager).decisionDialog(title,message, this::reSekigime)
        }
        goHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        layout.orientation = LinearLayout.VERTICAL
        layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_img)

        val adapter = ArrayAdapter<String>(this, R.layout.dropdown_item_layout)
        val list = ArrayList<String>() // 新インスタンスを生成
        for (group in groupArray!!) {
            list.add(group)
        }
        adapter.addAll(list)
        groupDropdown.adapter = adapter
        val lp = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        groupDropdown.layoutParams = lp
        groupDropdown.translationY = 15 * scale
        groupDropdown.translationX = (centerX * 1.1).toFloat()
        groupDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int,
                                        id: Long) {
                DrawTableView.point = 0
                DrawTableView.position = position
                draw.reDraw()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}

        }

        val scrollView = ScrollView(this)
        reLayout.addView(scrollView)
        reLayout.addView(adView, param2)
        layout.addView(groupDropdown)
        layout.addView(draw)
        layout.addView(reSekigime)
        layout.addView(goHome)
        scrollView.addView(layout)
        setContentView(reLayout)
    }

    private fun reSekigime(){
        for (i in 0 until groupNo) {
            if (StatusHolder.normalMode) {
                arrayArrayNormal[i].shuffle()
            } else {
                arrayArrayQuick[i].shuffle()
            }
        }
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        DrawTableView.point = 0
        draw.reDraw()
        Toast.makeText(applicationContext, getText(R.string.re_sekigime_finished), Toast.LENGTH_SHORT).show()
    }

    private fun convertAlternatelyFmArray() {
        var manArrayNo: Float
        var womanArrayNo: Float
        var bigger: Float
        var smaller: Float
        var addNo: Float
        var remainder: Float
        var i: Int
        var j: Int
        var k: Int
        var a: Int
        var memberSum: Int
        if (StatusHolder.normalMode) {
            createFmArrayForNormal()
            var smallerArray: ArrayList<ArrayList<Member>>
            var biggerArray: ArrayList<ArrayList<Member>>
            arrayArrayNormal = ArrayList(groupNo)
            for (g in 0 until groupNo) {
                arrayArrayNormal.add(ArrayList())
            }
            i = 0
            while (i < groupNo) {
                manArrayNo = arrayArrayNormalMan[i].size.toFloat()
                womanArrayNo = arrayArrayNormalWoman[i].size.toFloat()
                if (manArrayNo < womanArrayNo) {
                    bigger = womanArrayNo
                    smaller = manArrayNo
                    biggerArray = arrayArrayNormalWoman
                    smallerArray = arrayArrayNormalMan
                } else {
                    bigger = manArrayNo
                    smaller = womanArrayNo
                    biggerArray = arrayArrayNormalMan
                    smallerArray = arrayArrayNormalWoman
                }
                if (smaller != 0f) {
                    addNo = bigger / smaller
                    remainder = bigger % smaller - 1
                    memberSum = 0
                    a = 0
                    while (a < addNo / 2) {
                        arrayArrayNormal[i].add(biggerArray[i][memberSum])
                        memberSum++
                        a++
                    }
                    arrayArrayNormal[i].add(smallerArray[i][0])
                    j = 1
                    while (j < smaller) {
                        k = 0
                        while (k < addNo - 1) {
                            arrayArrayNormal[i].add(biggerArray[i][memberSum])
                            memberSum++
                            k++
                        }
                        if (remainder != 0f) {
                            arrayArrayNormal[i].add(biggerArray[i][memberSum])
                            memberSum++
                            remainder--
                        }
                        arrayArrayNormal[i].add(smallerArray[i][j])
                        j++
                    }
                    while (a < addNo) {
                        arrayArrayNormal[i].add(biggerArray[i][memberSum])
                        memberSum++
                        a++
                    }
                } else {
                    k = 0
                    while (k < bigger) {
                        arrayArrayNormal[i].add(biggerArray[i][k])
                        k++
                    }
                }
                i++

            }
        } else {
            createFmArrayForQuick()
            var smallerArray: ArrayList<ArrayList<String>>
            var biggerArray: ArrayList<ArrayList<String>>
            arrayArrayQuick = ArrayList(groupNo)
            for (g in 0 until groupNo) {
                arrayArrayQuick.add(ArrayList())
            }
            i = 0
            while (i < groupNo) {
                manArrayNo = arrayArrayQuickMan[i].size.toFloat()
                womanArrayNo = arrayArrayQuickWoman[i].size.toFloat()
                if (manArrayNo < womanArrayNo) {
                    bigger = womanArrayNo
                    smaller = manArrayNo
                    biggerArray = arrayArrayQuickWoman
                    smallerArray = arrayArrayQuickMan
                } else {
                    bigger = manArrayNo
                    smaller = womanArrayNo
                    biggerArray = arrayArrayQuickMan
                    smallerArray = arrayArrayQuickWoman
                }
                if (smaller != 0f) {
                    addNo = bigger / smaller
                    remainder = bigger % smaller - 1
                    memberSum = 0
                    a = 0
                    while (a < addNo / 2) {
                        arrayArrayQuick[i].add(biggerArray[i][memberSum])
                        memberSum++
                        a++
                    }
                    arrayArrayQuick[i].add(smallerArray[i][0])
                    j = 1
                    while (j < smaller) {
                        k = 0
                        while (k < addNo - 1) {
                            arrayArrayQuick[i].add(biggerArray[i][memberSum])
                            memberSum++
                            k++
                        }
                        if (remainder != 0f) {
                            arrayArrayQuick[i].add(biggerArray[i][memberSum])
                            memberSum++
                            remainder--
                        }
                        arrayArrayQuick[i].add(smallerArray[i][j])
                        j++
                    }
                    while (a < addNo) {
                        arrayArrayQuick[i].add(biggerArray[i][memberSum])
                        memberSum++
                        a++
                    }

                } else {
                    k = 0
                    while (k < bigger) {
                        arrayArrayQuick[i].add(biggerArray[i][k])
                        k++
                    }
                }
                i++
            }

        }
    }

    private fun createFmArrayForNormal() {
        var item: Member
        arrayArrayNormalMan = ArrayList(groupNo)
        arrayArrayNormalWoman = ArrayList(groupNo)
        for (g in 0 until groupNo) {
            arrayArrayNormalMan.add(ArrayList())
            arrayArrayNormalWoman.add(ArrayList())
        }
        for (i in arrayArrayNormal.indices) {
            for (j in 0 until arrayArrayNormal[i].size) {
                item = arrayArrayNormal[i][j]
                if (item.sex == getText(R.string.man)) {
                    arrayArrayNormalMan[i].add(item)
                } else {
                    arrayArrayNormalWoman[i].add(item)
                }
            }
        }
    }

    private fun createFmArrayForQuick() {
        var item: String
        arrayArrayQuickMan = ArrayList(groupNo)
        arrayArrayQuickWoman = ArrayList(groupNo)
        for (g in 0 until groupNo) {
            arrayArrayQuickMan.add(ArrayList())
            arrayArrayQuickWoman.add(ArrayList())
        }
        for (i in arrayArrayQuick.indices) {
            for (j in 0 until arrayArrayQuick[i].size) {
                item = arrayArrayQuick[i][j]
                if (item.matches((".*" + "♠" + ".*").toRegex())) {
                    arrayArrayQuickMan[i].add(item)
                } else {
                    arrayArrayQuickWoman[i].add(item)
                }
            }
        }
    }

    companion object {

        var arrayArrayNormal: ArrayList<ArrayList<Member>> = ArrayList()
        var arrayArrayNormalMan: ArrayList<ArrayList<Member>> = ArrayList()
        var arrayArrayNormalWoman: ArrayList<ArrayList<Member>> = ArrayList()
        var arrayArrayQuick: ArrayList<ArrayList<String>> = ArrayList()
        var arrayArrayQuickMan: ArrayList<ArrayList<String>> = ArrayList()
        var arrayArrayQuickWoman: ArrayList<ArrayList<String>> = ArrayList()
        var groupArray: ArrayList<String>? = null
        var doubleDeploy: Boolean = false
        var fmDeploy: Boolean = false
        var square_no: Int = 0
    }
}