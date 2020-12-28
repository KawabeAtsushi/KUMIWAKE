package com.pandatone.kumiwake.sekigime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.history.HistoryAdapter
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.sekigime.function.DrawAllTable
import com.pandatone.kumiwake.sekigime.function.DrawTableView
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import kotlin.math.round


/**
 * Created by atsushi_2 on 2016/07/16.
 */
class SekigimeResult : AppCompatActivity() {

    private lateinit var draw: DrawTableView
    private var groupNo: Int = 0
    private var teamArrayMan: ArrayList<ArrayList<Member>> = ArrayList()
    private var teamArrayWoman: ArrayList<ArrayList<Member>> = ArrayList()
    private lateinit var tabLayout: TabLayout

    //結果タイトル&コメント
    private var title = ""
    private var comment = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sekigime_result)

        tabLayout = findViewById(R.id.tabLayout)
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

        findViewById<ImageButton>(R.id.edit_result_title).setOnClickListener { editInfoDialog() }
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
    private fun drawAll(resultLayout: LinearLayout) {
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

    //情報変更ダイアログ
    private fun editInfoDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edit_result_dialog_layout, findViewById<View>(R.id.info_layout) as ViewGroup?)
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        etTitle.hint = getString(R.string.sekigime_result)
        if (title != "") etTitle.setText(this.title)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        if (comment != "") etComment.setText(this.comment)
        view.findViewById<CheckBox>(R.id.include_title_check).visibility = View.GONE
        view.findViewById<CheckBox>(R.id.include_comment_check).visibility = View.GONE
        builder.setTitle(getString(R.string.add_info))
                .setView(view)
                .setPositiveButton(R.string.change) { _, _ ->
                    changeInfo(etTitle.text.toString(), etComment.text.toString())
                    val tvTitle = findViewById<TextView>(R.id.inner_result_title)
                    if (title != "") {
                        tvTitle.visibility = View.VISIBLE
                        if (StatusHolder.normalMode) {
                            val hsAdapter = HistoryAdapter(this)
                            hsAdapter.updateHistoryState(hsAdapter.latestHistory, title, false)
                        }
                    } else {
                        tvTitle.visibility = View.GONE
                    }
                    val tvComment = findViewById<TextView>(R.id.comment_view)
                    if (comment != "") {
                        tvComment.visibility = View.VISIBLE
                    } else {
                        tvComment.visibility = View.GONE
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //情報変更
    private fun changeInfo(newTitle: String, newComment: String) {
        title = newTitle
        val tvTitle = findViewById<TextView>(R.id.inner_result_title)
        if (title != "") {
            tvTitle.text = title
        } else {
            tvTitle.text = getString(R.string.sekigime_result)
        }
        comment = newComment
        val tvComment = findViewById<TextView>(R.id.comment_view)
        if (comment != "") {
            tvComment.text = comment
            tvComment.visibility = View.VISIBLE
        } else {
            tvComment.visibility = View.GONE
        }
    }

    //ボタンの処理
    //再席決め
    private fun onReSekigime() {
        val message = getString(R.string.re_sekigime_description) + getString(R.string.run_confirmation)
        val title = getString(R.string.retry_title)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, function = this::reSekigime)
    }

    //ホームに戻る
    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    //結果画像共有
    private fun onShareImage() {
        shareOptionDialog()
    }

    //option設定ダイアログ
    private fun shareOptionDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edit_result_dialog_layout, findViewById<View>(R.id.info_layout) as ViewGroup?)
        val indexTitle = view.findViewById<TextView>(R.id.title_index)
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        indexTitle.visibility = View.GONE
        etTitle.visibility = View.GONE
        etTitle.hint = getString(R.string.sekigime_result)
        if (title != "") etTitle.setText(this.title)
        val indexComment = view.findViewById<TextView>(R.id.comment_index)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        indexComment.visibility = View.GONE
        etComment.visibility = View.GONE
        if (comment != "") etComment.setText(this.comment)

        val includeTitleCheck = view.findViewById<CheckBox>(R.id.include_title_check)
        val includeCommentCheck = view.findViewById<CheckBox>(R.id.include_comment_check)
        val tvTitle = findViewById<TextView>(R.id.inner_result_title)
        tvTitle.visibility = View.GONE
        val tvComment = findViewById<TextView>(R.id.comment_view)
        tvComment.visibility = View.GONE

        includeTitleCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                indexTitle.visibility = View.VISIBLE
                etTitle.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
            } else {
                indexTitle.visibility = View.GONE
                etTitle.visibility = View.GONE
                tvTitle.visibility = View.GONE
            }
        }
        includeCommentCheck.setOnCheckedChangeListener { _, checked ->

            if (checked) {
                indexComment.visibility = View.VISIBLE
                etComment.visibility = View.VISIBLE
                if (tvComment.text != "") {
                    tvComment.visibility = View.VISIBLE
                }

            } else {
                indexComment.visibility = View.GONE
                etComment.visibility = View.GONE
                tvComment.visibility = View.GONE
            }
        }

        etTitle.doAfterTextChanged { changeInfo(etTitle.text.toString(), etComment.text.toString()) }
        etComment.doAfterTextChanged { changeInfo(etTitle.text.toString(), etComment.text.toString()) }

        builder.setTitle(getString(R.string.share_option))
                .setView(view)
                .setPositiveButton(R.string.share) { _, _ ->
                    if (StatusHolder.normalMode) {
                        val hsAdapter = HistoryAdapter(this)
                        hsAdapter.updateHistoryState(hsAdapter.latestHistory, title, false)
                    }
                    shareImage()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    tvTitle.visibility = View.GONE
                    if (tvComment.text != "") {
                        tvComment.visibility = View.VISIBLE
                    }
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //画像でシェア
    private fun shareImage() {
        val shareLayout = findViewById<LinearLayout>(R.id.whole_result_layout)
        ShareViewImage.shareView(this, shareLayout, getString(R.string.sekigime_result))
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
    private fun groupTextView(groupName: String, i: Int): TextView {
        val groupNameView = TextView(this)
        groupNameView.text = groupName
        groupNameView.setTextColor(Color.DKGRAY)
        groupNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
        groupNameView.background = ContextCompat.getDrawable(this, R.drawable.table_name_background)
        groupNameView.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val mlp = lp as MarginLayoutParams
        if (i == 0) {//最初だけTopマージンなし
            mlp.setMargins(70, 30, 70, 0)
        } else {
            mlp.setMargins(70, 140, 70, 0)
        }
        groupNameView.layoutParams = mlp
        return groupNameView
    }

    //男女が交互になるように配列を変換
    private fun convertAlternatelyFmArray() {
        createFmArray()
        var smallerArray: ArrayList<Member>
        var biggerArray: ArrayList<Member>
        teamArray = ArrayList()

        for (i in 0 until groupNo) {
            val seatArray: ArrayList<Member> = ArrayList()

            //多い性別と少ない性別を判断
            if (teamArrayMan[i].size < teamArrayWoman[i].size) {
                biggerArray = teamArrayWoman[i]
                smallerArray = teamArrayMan[i]
            } else {
                biggerArray = teamArrayMan[i]
                smallerArray = teamArrayWoman[i]
            }
            val bigger = biggerArray.size.toFloat()
            val smaller = smallerArray.size.toFloat()

            if (smaller != 0f) {//両方の性別がいる場合
                val tempMember = Member(0, "temp", "temp", 0, "", "", -1)
                biggerArray.forEach {
                    seatArray.add(tempMember)
                    seatArray.add(it)
                }

                val ratio = bigger / smaller //多い方：少ない方　の比
                for (j in 0 until smaller.toInt()) {
                    val index = round(ratio * j).toInt() * 2
                    seatArray[index] = smallerArray[j]
                }
                seatArray.removeAll { it.sex == "temp" }
            } else {
                seatArray.addAll(biggerArray)
            }

            teamArray.add(seatArray)
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
            for (j in teamArray[i].indices) {
                item = teamArray[i][j]
                if (PublicMethods.isMan(item.sex)) {
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