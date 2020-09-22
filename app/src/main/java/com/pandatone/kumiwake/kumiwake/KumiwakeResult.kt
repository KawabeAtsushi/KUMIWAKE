package com.pandatone.kumiwake.kumiwake

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import com.google.android.material.tabs.TabLayout
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.history.HistoryAdapter
import com.pandatone.kumiwake.history.HistoryMethods
import com.pandatone.kumiwake.history.HistoryMethods.avoidDuplicate
import com.pandatone.kumiwake.kumiwake.function.KumiwakeComparator
import com.pandatone.kumiwake.kumiwake.function.KumiwakeMethods
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.SelectTableType
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class KumiwakeResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private lateinit var leaderArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var leaderNoList: Array<Int?>
    private lateinit var resultArray: ArrayList<ArrayList<Member>>
    private lateinit var manArray: ArrayList<Member>
    private lateinit var womanArray: ArrayList<Member>
    private var groupCount: Int = 0
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false
    private var v = 0
    private lateinit var tabLayout: TabLayout

    //結果タイトル&コメント
    private var title = ""
    private var comment = ""
    private var includeTitle = false
    private var includeComment = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(StatusHolder.nowTheme)
        setContentView(R.layout.kumiwake_result)
        if (StatusHolder.sekigime) {
            val layout = findViewById<ConstraintLayout>(R.id.result_view)
            layout.background = getDrawable(R.drawable.sekigime_background)
        }

        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_group))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_member))
        tabLayout.addOnTabSelectedListener(tabItemSelectedListener)

        PublicMethods.showAd(this)
        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray = i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.LEADER_NO_LIST.key) != null) {
            leaderNoList = i.getSerializableExtra(KumiwakeArrayKeys.LEADER_NO_LIST.key) as Array<Int?>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.LEADER_LIST.key) != null) {
            leaderArray = i.getSerializableExtra(KumiwakeArrayKeys.LEADER_LIST.key) as ArrayList<Member>
        }

        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)
        groupCount = groupArray.size

        startMethod(false)

        if (!StatusHolder.sekigime) {
            //描画を別スレッドで行う
            Thread(DrawTask(this, groupCount)).start()
            val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
        } else {
            val groupNameArray = ArrayList<String>(groupCount)
            for (j in 0 until groupCount) {
                groupNameArray.add(groupArray[j].name)
                resultArray[j].shuffle()
            }
            val intent = Intent(this, SelectTableType::class.java)
            SekigimeResult.groupArray = groupNameArray
            SekigimeResult.teamArray = resultArray
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.edit_result_title).setOnClickListener { editInfoDialog() }
        findViewById<Button>(R.id.re_kumiwake).setOnClickListener { onReKumiwake() }
        findViewById<Button>(R.id.share_result).setOnClickListener { shareResult() }
        findViewById<Button>(R.id.go_sekigime).setOnClickListener { onGoSekigime() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        for (i in 0 until groupCount) {
            outState.putSerializable("ARRAY$i", resultArray[i])
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        resultArray = ArrayList(groupCount)
        for (g in 0 until groupCount) {
            resultArray.add(savedInstanceState.getSerializable("ARRAY$g") as ArrayList<Member>)
        }
    }


    private fun startMethod(again: Boolean) {
        tabLayout.selectTab(tabLayout.getTabAt(0))

        memberArray.shuffle()

        resultArray = ArrayList(groupCount)
        manArray = ArrayList()
        womanArray = ArrayList()

        for (g in 0 until groupCount) {
            resultArray.add(ArrayList())
        }

        if (evenFmRatio && evenAgeRatio) {
            createFmArray()    //男女それぞれの配列を作成
            KumiwakeMethods.arrangeByAge(manArray)
            KumiwakeMethods.arrangeByAge(womanArray)
            KumiwakeMethods.setLeader(resultArray, leaderArray, leaderNoList)
            KumiwakeMethods.evenManDistribute(memberArray.size, resultArray, manArray, groupArray, getString(R.string.man))
            KumiwakeMethods.evenWomanDistribute(resultArray, womanArray, groupArray)
        } else if (evenFmRatio) {
            createFmArray()    //男女それぞれの配列を作成
            KumiwakeMethods.setLeader(resultArray, leaderArray, leaderNoList)
            KumiwakeMethods.evenManDistribute(memberArray.size, resultArray, manArray, groupArray, getString(R.string.man))
            KumiwakeMethods.evenWomanDistribute(resultArray, womanArray, groupArray)
        } else if (evenAgeRatio) {
            KumiwakeMethods.arrangeByAge(memberArray)
            KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, leaderArray, leaderNoList)
        } else {
            if (StatusHolder.normalMode) {
                KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, leaderArray, leaderNoList)
            } else {
                KumiwakeMethods.kumiwakeAllQuick(resultArray, memberArray, groupArray)
            }
        }

        //かぶりが出ないように調整
        if (StatusHolder.notDuplicate) {
            resultArray.avoidDuplicate(memberArray.size.toFloat())
        }

        //並べ替え
        resultArray.forEach {
            Collections.sort(it, KumiwakeComparator.ViewComparator())
        }

        //履歴に保存
        if (StatusHolder.normalMode) {
            if (StatusHolder.sekigime) {
                HistoryMethods.saveResultToHistory(this, resultArray, groupArray, 1, false)
            } else {
                HistoryMethods.saveResultToHistory(this, resultArray, groupArray, 0, again)
            }
        }
    }

    //男女配列作成
    private fun createFmArray() {
        for (member in memberArray) {
            if (member.sex == getText(R.string.man)) {
                manArray.add(member)
            } else {
                womanArray.add(member)
            }
        }
    }

    //表示形式切り替え
    private val tabItemSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            when (tab.position) {
                0 -> {//グループごと
                    v = 0
                    Thread(DrawTask(this@KumiwakeResult, groupCount)).start()
                }
                1 -> {//メンバーごと
                    addResultViewByMember()
                }
            }
            val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
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
        if (title != "") etTitle.setText(this.title)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        if (comment != "") etComment.setText(this.comment)
        view.findViewById<CheckBox>(R.id.include_title_check).visibility = View.GONE
        view.findViewById<CheckBox>(R.id.include_comment_check).visibility = View.GONE
        builder.setTitle(getString(R.string.add_info))
                .setView(view)
                .setPositiveButton(R.string.change) { _, _ ->
                    changeInfo(etTitle.text.toString(), etComment.text.toString())
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
        if (title != "") {
            findViewById<TextView>(R.id.result_title).text = title
            findViewById<TextView>(R.id.inner_result_title).text = title
            if (StatusHolder.normalMode) {
                val hsAdapter = HistoryAdapter(this)
                hsAdapter.updateHistoryState(hsAdapter.latestHistory, title, false)
            }
        } else {
            findViewById<TextView>(R.id.result_title).text = getString(R.string.kumiwake_result)
            findViewById<TextView>(R.id.inner_result_title).text = getString(R.string.kumiwake_result)
        }
        comment = newComment
        val tvComment = findViewById<TextView>(R.id.comment_view)
        if (comment != "") {
            tvComment.visibility = View.VISIBLE
            tvComment.text = comment
        } else {
            tvComment.visibility = View.GONE
            tvComment.text = ""
        }
    }

    //以下、ボタンの処理

    private fun onReKumiwake() {
        v = 0
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, function = this::reKumiwake)
    }

    private fun reKumiwake() {
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod(true)
        Thread(DrawTask(this, groupCount)).start()
        Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
    }

    private fun shareResult() {
        shareOptionDialog()
    }

    private fun onGoSekigime() {
        val groupNameArray = ArrayList<String>(groupCount)
        for (j in 0 until groupCount) {
            groupNameArray.add(groupArray[j].name)
            resultArray[j].shuffle()
        }
        val intent = Intent(this, SelectTableType::class.java)
        SekigimeResult.groupArray = groupNameArray
        SekigimeResult.teamArray = resultArray
        startActivity(intent)
    }

    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////                                 描画メソッド                                             ////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun addResultView(i: Int) {
        val groupName: TextView
        val arrayList: ListView
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        val v = layoutInflater.inflate(R.layout.result_parts, null)
        if (i == 0) {
            layout.removeAllViews()
        }
        layout.addView(v)
        groupName = v.findViewById<View>(R.id.result_group) as TextView
        groupName.text = groupArray[i].name
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = SmallMBListAdapter(this, resultArray[i], leaderNoList = leaderNoList)
        arrayList.adapter = adapter
        setBackGround(v, i)
        adapter.setRowHeight(arrayList)
    }

    //背景
    private fun setBackGround(v: View, i: Int) {
        val drawable = GradientDrawable()
        drawable.mutate()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 25f
        if (i < 0) {//メンバー毎表示
            drawable.setColor(Color.parseColor("#AAFFFFFF"))
        } else {//グループごと表示
            val colorStr = KumiwakeMethods.getResultColorStr(i, groupArray.size)
            drawable.setColor(Color.parseColor("#AA$colorStr"))
        }
        v.layoutParams = PublicMethods.setMargin(this, 4, 6, 4, 6)
        v.background = drawable
    }

    //メンバーごとの結果描画
    private fun addResultViewByMember() {
        val arrayList: ListView
        val resultArrayByMember: ArrayList<Member> = ArrayList()
        //すべてのメンバーをグループ込みの名前に変更
        memberArray.forEach {
            resultArrayByMember.add(memberIncludeGroups(it))
        }
        leaderArray.forEach {
            resultArrayByMember.add(memberIncludeGroups(it))
        }
        //描画
        Collections.sort(resultArrayByMember, KumiwakeComparator.ViewComparator())
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        layout.removeAllViews()
        val v = layoutInflater.inflate(R.layout.result_parts, null)
        layout.addView(v)
        val annotation = v.findViewById<View>(R.id.result_group) as TextView
        annotation.text = getText(R.string.kumiwake_order_annotation)
        annotation.textSize = 15F
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = SmallMBListAdapter(this, resultArrayByMember, leaderNoList = leaderNoList, nameIsSpanned = true)
        arrayList.adapter = adapter
        setBackGround(v, -1)
        adapter.setRowHeight(arrayList)
    }

    //グループも含んだMemberArrayを作成
    private fun memberIncludeGroups(member: Member): Member {
        var groupNo = 0
        for ((i, result) in resultArray.withIndex()) {
            if (result.contains(member)) {
                groupNo = i
                break
            }
        }
        val colorStr = KumiwakeMethods.getResultColorStr(groupNo, groupArray.size)
        val newName = member.name + " → <strong><font color='#" + colorStr + "'>" + groupArray[groupNo].name + "</font></strong>"
        return Member(member.id, newName, member.sex, member.age, member.belong, member.read, member.leader)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                                   結果共有メソッド                                              ////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //option設定ダイアログ
    private fun shareOptionDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edit_result_dialog_layout, findViewById<View>(R.id.info_layout) as ViewGroup?)
        val indexTitle = view.findViewById<TextView>(R.id.title_index)
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        indexTitle.visibility = View.GONE
        etTitle.visibility = View.GONE
        if (title != "") etTitle.setText(this.title)
        val indexComment = view.findViewById<TextView>(R.id.comment_index)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        indexComment.visibility = View.GONE
        etComment.visibility = View.GONE
        if (comment != "") etComment.setText(this.comment)

        val includeTitleCheck = view.findViewById<CheckBox>(R.id.include_title_check)
        val includeCommentCheck = view.findViewById<CheckBox>(R.id.include_comment_check)

        includeTitleCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                indexTitle.visibility = View.VISIBLE
                etTitle.visibility = View.VISIBLE
            } else {
                indexTitle.visibility = View.GONE
                etTitle.visibility = View.GONE
            }
        }
        includeCommentCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                indexComment.visibility = View.VISIBLE
                etComment.visibility = View.VISIBLE
            } else {
                indexComment.visibility = View.GONE
                etComment.visibility = View.GONE
            }
        }

        builder.setTitle(getString(R.string.share_option))
                .setView(view)
                .setPositiveButton(R.string.share) { _, _ ->
                    changeInfo(etTitle.text.toString(), etComment.text.toString())
                    val tvTitle = findViewById<TextView>(R.id.inner_result_title)
                    val tvComment = findViewById<TextView>(R.id.comment_view)
                    includeTitle = includeTitleCheck.isChecked
                    includeComment = includeCommentCheck.isChecked
                    if (includeTitle) {
                        tvTitle.visibility = View.VISIBLE
                    } else {
                        tvTitle.visibility = View.GONE
                    }
                    if (includeComment && comment != "") {
                        tvComment.visibility = View.VISIBLE
                    } else {
                        tvComment.visibility = View.GONE
                    }
                    KumiwakeMethods.shareResult(this, tvTitle, tvComment, this::shareText, this::shareImage)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //テキストでシェア
    private fun shareText() {
        var setLeader = false
        val articleTitle = if (includeTitle) {
            if (title != "") {
                "～$title～"
            } else {
                "～${getString(R.string.kumiwake_result)}～"
            }
        } else {
            ""
        }
        val articleComment = if (includeComment && comment != "") {
            "\n$comment"
        } else {
            ""
        }
        val descriptionLeader = "\n☆:" + getString(R.string.leader) + "\n"
        var sharedText = ""
        val resultTxt = StringBuilder()

        for ((i, array) in resultArray.withIndex()) {
            resultTxt.append("\n")
            resultTxt.append("《${groupArray[i].name}》\n")

            for (member in array) {
                when {
                    leaderArray.contains(member) -> {
                        setLeader = true
                        resultTxt.append("☆")
                    }
                    member.sex == getString(R.string.man) -> resultTxt.append("♠")
                    else -> resultTxt.append("♡")
                }
                resultTxt.append("${member.name}\n")
            }
        }

        sharedText = if (setLeader) {
            "$articleTitle$articleComment\n$descriptionLeader$resultTxt"
        } else {
            "$articleTitle$articleComment\n$resultTxt"
        }
        // builderの生成
        val builder = ShareCompat.IntentBuilder.from(this)
        // アプリ一覧が表示されるDialogのタイトルの設定
        builder.setChooserTitle(R.string.choose_app)
        // シェアするタイトル
        builder.setSubject(articleTitle)
        // シェアするテキスト
        builder.setText(sharedText)
        // シェアするタイプ（他にもいっぱいあるよ）
        builder.setType("text/plain")
        // Shareアプリ一覧のDialogの表示
        builder.startChooser()
    }

    //画像でシェア
    private fun shareImage() {
        val shareLayout = findViewById<LinearLayout>(R.id.whole_result_layout)
        ShareViewImage.shareView(this, shareLayout, getString(R.string.kumiwake_result))
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
////                                   補助処理メソッド                                              ////
/////////////////////////////////////////////////////////////////////////////////////////////////////////


internal class DrawTask(private val context: Context, private val groupCount: Int) : Runnable {
    private val handler: Handler = Handler()

    override fun run() {
        handler.post {
            for (v in 0 until groupCount) {
                (context as KumiwakeResult).addResultView(v)
            }
        }
    }

}

