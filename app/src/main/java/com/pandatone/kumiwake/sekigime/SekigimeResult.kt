package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.sekigime.function.DrawTableView
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.marginStart
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.sekigime.function.DrawAllTable
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.sekigime_result.*


/**
 * Created by atsushi_2 on 2016/07/16.
 */
class SekigimeResult : AppCompatActivity() {

    private lateinit var draw: DrawTableView
    private var groupNo: Int = 0
    private var drawAll = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sekigime_result)
        ButterKnife.bind(this)
        MobileAds.initialize(applicationContext, "ca-app-pub-2315101868638564/8665451539")
        val mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build()
        mAdView.loadAd(adRequest)

        DrawTableView.point = 0 //フォーカスする席番号
        DrawTableView.tableNo = 0 //表示するテーブル番号

        groupNo = groupArray!!.size
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        draw = DrawTableView(this)

        val groupDropdown = findViewById<View>(com.pandatone.kumiwake.R.id.group_dropdown) as AutoCompleteTextView
        groupDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(groupDropdown.windowToken, 0)
        }
        val adapter = ArrayAdapter<String>(this, com.pandatone.kumiwake.R.layout.dropdown_item_layout)
        val list = ArrayList<String>() // 新インスタンスを生成
        for (group in groupArray!!) {
            list.add(group)
        }
        adapter.addAll(list)
        groupDropdown.setAdapter(adapter)
        groupDropdown.hint = list[0]
        groupDropdown.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            DrawTableView.point = 0
            DrawTableView.tableNo = position
            draw.reDraw()
        }
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        resultLayout.addView(draw)
    }

    @OnClick(R.id.re_sekigime)
    fun onReSekigime() {
        val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
        val title = getString(R.string.re_sekigime_title)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::reSekigime)
    }

    @OnClick(R.id.go_home)
    fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    @OnClick(R.id.show_all)
    fun onShowAll() {
        findViewById<ScrollView>(R.id.result_scroller).fullScroll(ScrollView.FOCUS_UP)
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        resultLayout.removeAllViews()
        val dropdown = findViewById<TextInputLayout>(R.id.group_selector)
        val button = findViewById<Button>(R.id.show_all)
        if (drawAll) {
            dropdown.visibility = View.GONE
            val groupNameView = groupTextView()
            val drawAll = DrawAllTable(this)
            resultLayout.addView(groupNameView)
            resultLayout.addView(drawAll)
            button.text = getString(R.string.show_detail)
        } else {
            dropdown.visibility = View.VISIBLE
            resultLayout.addView(draw)
            button.text = getString(R.string.show_all)
        }
        drawAll = !drawAll //描画モード切替
    }

    //再席決め
    private fun reSekigime() {
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

    //グループ名前のTextView生成
    private fun groupTextView(): TextView {
        val groupNameView = TextView(this)
        groupNameView.text = "AAAAAAAA"
        groupNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30.0f)
        groupNameView.background = getDrawable(R.drawable.index_white_background)
        groupNameView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val mlp = lp as MarginLayoutParams
        mlp.setMargins(70, 30, 70, 0)
        groupNameView.layoutParams = mlp
        return groupNameView
    }

    //男女が交互になるように配列を変換
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