package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.customize.CustomDialog
import com.pandatone.kumiwake.kumiwake.MainActivity
import com.pandatone.kumiwake.member.Name
import java.util.*

/**
 * Created by atsushi_2 on 2016/07/16.
 */
class SekigimeResult : AppCompatActivity() {

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
        adView.adUnitId = "ca-app-pub-2315101868638564/8665451539"
        adView.adSize = AdSize.BANNER
        val adRequest = AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build()
        adView.loadAd(adRequest)

        groupNo = groupArray!!.size
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        val draw = DrawTableView(this)
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val disp = wm.defaultDisplay
        val size = Point()
        disp.getSize(size)
        val group_spinner = Spinner(this)
        val layout = LinearLayout(this)
        val re_sekigime = Button(this)
        val go_home = Button(this)
        val buttonWidth = (180 * scale).toInt()
        val buttonHeight = (50 * scale).toInt()
        val centerX = ((size.x - buttonWidth) / 2).toFloat()
        re_sekigime.text = getText(R.string.re_sekigime)
        go_home.text = getText(R.string.go_home)
        val llp1 = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        val llp2 = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        llp1.setMargins(llp1.leftMargin, (10 * scale).toInt(), llp1.rightMargin, (10 * scale).toInt())
        re_sekigime.layoutParams = llp1
        re_sekigime.translationX = centerX
        llp2.setMargins(llp2.leftMargin, (10 * scale).toInt(), llp2.rightMargin, (90 * scale).toInt())
        go_home.layoutParams = llp2
        go_home.translationX = centerX
        re_sekigime.background = ContextCompat.getDrawable(this, R.drawable.simple_orange_button)
        go_home.background = ContextCompat.getDrawable(this, R.drawable.simple_green_button)
        re_sekigime.setOnClickListener {
            val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
            val title = getString(R.string.re_sekigime_title)
            val customDialog = CustomDialog()
            customDialog.setTitle(title)
            customDialog.setMessage(message)
            CustomDialog.mPositiveBtnListener = View.OnClickListener {
                for (i in 0 until groupNo) {
                    if (Normalmode) {
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
                customDialog.dismiss()
            }
            customDialog.show(getSupportFragmentManager(), "Btn")
        }
        go_home.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        layout.orientation = LinearLayout.VERTICAL
        layout.background = ContextCompat.getDrawable(this, R.drawable.sekigime_img)

        val adapter = ArrayAdapter<String>(this, R.layout.spinner_layout)
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        )
        val list = ArrayList<String>() // 新インスタンスを生成
        for (j in groupArray!!.indices) {
            list.add(groupArray!![j])
        }
        adapter.addAll(list)
        group_spinner.adapter = adapter
        val lp = LinearLayout.LayoutParams(buttonWidth, buttonHeight)
        group_spinner.layoutParams = lp
        group_spinner.translationY = 15 * scale
        group_spinner.translationX = (centerX * 1.1).toFloat()
        group_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        layout.addView(group_spinner)
        layout.addView(draw)
        layout.addView(re_sekigime)
        layout.addView(go_home)
        scrollView.addView(layout)
        setContentView(reLayout)
    }

    fun convertAlternatelyFmArray() {
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
        if (Normalmode) {
            createFmArrayForNormal()
            var smallerArray: ArrayList<ArrayList<Name>>
            var biggerArray: ArrayList<ArrayList<Name>>
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

    fun createFmArrayForNormal() {
        var item: Name
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

    fun createFmArrayForQuick() {
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

        lateinit var arrayArrayNormal: ArrayList<ArrayList<Name>>
        lateinit var arrayArrayNormalMan: ArrayList<ArrayList<Name>>
        lateinit var arrayArrayNormalWoman: ArrayList<ArrayList<Name>>
        lateinit var arrayArrayQuick: ArrayList<ArrayList<String>>
        lateinit var arrayArrayQuickMan: ArrayList<ArrayList<String>>
        lateinit var arrayArrayQuickWoman: ArrayList<ArrayList<String>>
        var groupArray: ArrayList<String>? = null
        var Normalmode: Boolean = false
        var doubleDeploy: Boolean = false
        var fmDeploy: Boolean = false
        var square_no: Int = 0
        var groupNo: Int = 0
    }
}