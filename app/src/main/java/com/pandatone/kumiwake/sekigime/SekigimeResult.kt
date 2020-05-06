package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ShareViewImage
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.sekigime.function.DrawAllTable
import com.pandatone.kumiwake.sekigime.function.DrawTableView
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/07/16.
 */
class SekigimeResult : AppCompatActivity() {

    private lateinit var draw: DrawTableView
    private var groupNo: Int = 0
    private var teamArrayMan: ArrayList<ArrayList<Member>> = ArrayList()
    private var teamArrayWoman: ArrayList<ArrayList<Member>> = ArrayList()
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sekigime_result)

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.show_detail))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.show_all))
        tabLayout.addOnTabSelectedListener(tabItemSelectedListener)

        PublicMethods.showAd(this)
        DrawTableView.tableNo = 0 //表示するテーブル番号

        groupNo = groupArray!!.size
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        drawView(0)
        val groupDropdown = findViewById<View>(R.id.group_dropdown) as AutoCompleteTextView
        groupDropdown.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(groupDropdown.windowToken, 0)
        }
        val adapter = ArrayAdapter<String>(this, R.layout.dropdown_item_layout)
        val list = ArrayList<String>() // 新インスタンスを生成
        for (group in groupArray!!) {
            list.add(group)
        }
        adapter.addAll(list)
        groupDropdown.setAdapter(adapter)
        groupDropdown.hint = list[0]
        groupDropdown.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            drawView(position)
        }
        findViewById<Button>(R.id.re_sekigime).setOnClickListener { onReSekigime() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
        findViewById<Button>(R.id.share_image).setOnClickListener { onShareImage() }
    }

    //選択表示描画
    private fun drawView(position: Int) {
        DrawTableView.tableNo = position
        draw = DrawTableView(this)
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        resultLayout.removeAllViews()
        resultLayout.addView(draw)
    }

    //全表示
    private fun drawAll(resultLayout:LinearLayout){
        for (group in groupArray!!.withIndex()) {
            val drawAll = DrawAllTable(this@SekigimeResult, group.index)
            val groupNameView = groupTextView(group.value, group.index)
            resultLayout.addView(groupNameView)
            resultLayout.addView(drawAll)
        }
    }

    //表示形式切り替え
    private val tabItemSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            findViewById<ScrollView>(R.id.result_scroller).fullScroll(ScrollView.FOCUS_UP)
            val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
            resultLayout.removeAllViews()
            val dropdown = findViewById<TextInputLayout>(R.id.group_selector)
            val shareButton = findViewById<Button>(R.id.share_image)

            when (tab.position) {
                0 -> {//テーブルごと
                    dropdown.visibility = View.VISIBLE
                    shareButton.visibility = View.GONE
                    resultLayout.addView(draw)
                }
                1 -> {//全表示
                    dropdown.visibility = View.GONE
                    shareButton.visibility = View.VISIBLE
                    drawAll(resultLayout)
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    //ボタンの処理
    //再席決め
    private fun onReSekigime() {
        val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
        val title = getString(R.string.re_sekigime_title)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::reSekigime)
    }

    //ホームに戻る
    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PublicMethods.initialize()
        startActivity(intent)
    }

    //結果画像共有
    private fun onShareImage() {
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        ShareViewImage.shareView(this, resultLayout, getString(R.string.sekigime_result))
    }

    //再席決め処理
    private fun reSekigime() {
        for (i in 0 until groupNo) {
            teamArray[i].shuffle()
        }
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        if (tabLayout.selectedTabPosition == 1) {//全表示状態
            findViewById<ScrollView>(R.id.result_scroller).fullScroll(ScrollView.FOCUS_UP)
            val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
            resultLayout.removeAllViews()
            drawAll(resultLayout)
        } else {//選択表示状態
            DrawTableView.point = 0
            draw.reDraw()
        }
        Toast.makeText(applicationContext, getText(R.string.re_sekigime_finished), Toast.LENGTH_SHORT).show()
    }

    //グループ名前のTextView生成
    private fun groupTextView(groupName: String, i:Int): TextView {
        val groupNameView = TextView(this)
        groupNameView.text = groupName
        groupNameView.setTextColor(Color.DKGRAY)
        groupNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
        groupNameView.background = getDrawable(R.drawable.table_name_background)
        groupNameView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val mlp = lp as MarginLayoutParams
        if (i == 0) {//最初だけTopマージンなし
            mlp.setMargins(70, 30, 70, 0)
        }else{
            mlp.setMargins(70, 140, 70, 0)
        }
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
        var i: Int = 0
        var j: Int
        var k: Int
        var a: Int
        var memberSum: Int
        createFmArray()
        var smallerArray: ArrayList<ArrayList<Member>>
        var biggerArray: ArrayList<ArrayList<Member>>
        teamArray = ArrayList(groupNo)
        for (g in 0 until groupNo) {
            teamArray.add(ArrayList())
        }
        while (i < groupNo) {
            manArrayNo = teamArrayMan[i].size.toFloat()
            womanArrayNo = teamArrayWoman[i].size.toFloat()
            if (manArrayNo < womanArrayNo) {
                bigger = womanArrayNo
                smaller = manArrayNo
                biggerArray = teamArrayWoman
                smallerArray = teamArrayMan
            } else {
                bigger = manArrayNo
                smaller = womanArrayNo
                biggerArray = teamArrayMan
                smallerArray = teamArrayWoman
            }
            if (smaller != 0f) {
                addNo = bigger / smaller
                remainder = bigger % smaller - 1
                memberSum = 0
                a = 0
                while (a < addNo / 2) {
                    teamArray[i].add(biggerArray[i][memberSum])
                    memberSum++
                    a++
                }
                teamArray[i].add(smallerArray[i][0])
                j = 1
                while (j < smaller) {
                    k = 0
                    while (k < addNo - 1) {
                        teamArray[i].add(biggerArray[i][memberSum])
                        memberSum++
                        k++
                    }
                    if (remainder != 0f) {
                        teamArray[i].add(biggerArray[i][memberSum])
                        memberSum++
                        remainder--
                    }
                    teamArray[i].add(smallerArray[i][j])
                    j++
                }
                while (a < addNo) {
                    teamArray[i].add(biggerArray[i][memberSum])
                    memberSum++
                    a++
                }
            } else {
                k = 0
                while (k < bigger) {
                    teamArray[i].add(biggerArray[i][k])
                    k++
                }
            }
            i++

        }
    }

    private fun createFmArray() {
        var item: Member
        teamArrayMan = ArrayList(groupNo)
        teamArrayWoman = ArrayList(groupNo)
        for (g in 0 until groupNo) {
            teamArrayMan.add(ArrayList())
            teamArrayWoman.add(ArrayList())
        }
        for (i in teamArray.indices) {
            for (j in 0 until teamArray[i].size) {
                item = teamArray[i][j]
                if (item.sex == getText(R.string.man)) {
                    teamArrayMan[i].add(item)
                } else {
                    teamArrayWoman[i].add(item)
                }
            }
        }
    }

    companion object {
        var teamArray: ArrayList<ArrayList<Member>> = ArrayList()
        var groupArray: ArrayList<String>? = null
        var doubleDeploy: Boolean = false
        var fmDeploy: Boolean = false
        var square_no: Int = 0
    }
}