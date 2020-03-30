package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
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
    private var drawAll = true
    private var teamArrayMan: ArrayList<ArrayList<Member>> = ArrayList()
    private var teamArrayWoman: ArrayList<ArrayList<Member>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sekigime_result)
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
            //draw.reDraw()
        }
        findViewById<Button>(R.id.re_sekigime).setOnClickListener { onReSekigime() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
        findViewById<Button>(R.id.show_all).setOnClickListener { onShowAll() }
        findViewById<Button>(R.id.share_image).setOnClickListener { onShareImage() }
    }

    private fun drawView(position: Int) {
        DrawTableView.tableNo = position
        draw = DrawTableView(this)
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        resultLayout.removeAllViews()
        resultLayout.addView(draw)
    }

    private fun onReSekigime() {
        val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
        val title = getString(R.string.re_sekigime_title)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::reSekigime)
    }

    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun onShowAll() {
        findViewById<ScrollView>(R.id.result_scroller).fullScroll(ScrollView.FOCUS_UP)
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        resultLayout.removeAllViews()
        val dropdown = findViewById<TextInputLayout>(R.id.group_selector)
        val button = findViewById<Button>(R.id.show_all)
        val shareButton = findViewById<Button>(R.id.share_image)
        if (drawAll) {
            dropdown.visibility = View.GONE
            shareButton.visibility = View.VISIBLE
            button.text = getString(R.string.show_detail)
            for (group in groupArray!!.withIndex()) {
                val drawAll = DrawAllTable(this, group.index)
                val groupNameView = groupTextView(group.value)
                resultLayout.addView(groupNameView)
                resultLayout.addView(drawAll)
            }
        } else {
            dropdown.visibility = View.VISIBLE
            shareButton.visibility = View.GONE
            resultLayout.addView(draw)
            button.text = getString(R.string.show_all)
        }
        drawAll = !drawAll //描画モード切替
    }


    private fun onShareImage() {
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        ShareViewImage.shareView(this, resultLayout, getString(R.string.sekigime_result))
    }

    //再席決め
    private fun reSekigime() {
        for (i in 0 until groupNo) {
            teamArray[i].shuffle()
        }
        if (fmDeploy) {
            convertAlternatelyFmArray()
        }
        if (!drawAll) {
            drawAll = true
            onShowAll()
        } else {
            DrawTableView.point = 0
            draw.reDraw()
        }
        Toast.makeText(applicationContext, getText(R.string.re_sekigime_finished), Toast.LENGTH_SHORT).show()
    }

    //グループ名前のTextView生成
    private fun groupTextView(groupName: String): TextView {
        val groupNameView = TextView(this)
        groupNameView.text = groupName
        groupNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30.0f)
        groupNameView.background = getDrawable(R.drawable.table_name_background)
        groupNameView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val mlp = lp as MarginLayoutParams
        mlp.setMargins(70, 140, 70, 0)
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