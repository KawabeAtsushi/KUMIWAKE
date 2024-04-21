package com.pandatone.kumiwake.others.role

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.KumiwakeCustomKeys
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ShareViewImage
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.kumiwake.function.KumiwakeComparator
import com.pandatone.kumiwake.kumiwake.function.KumiwakeMethods
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.util.Collections

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class RoleResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var resultArray: ArrayList<ArrayList<Member>>
    private lateinit var manArray: ArrayList<Member>
    private lateinit var womanArray: ArrayList<Member>
    private var groupCount: Int = 0
    private var evenFmRatio: Boolean = false
    private var evenAgeRatio: Boolean = false
    private var v = 0
    private lateinit var tabLayout: TabLayout
    private var resultTitle: String = ""

    //結果タイトル&コメント
    private var title = ""
    private var comment = ""
    private var includeTitle = false
    private var includeComment = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_result)
        val layout = findViewById<ConstraintLayout>(R.id.result_view)
        layout.background = ContextCompat.getDrawable(this, R.drawable.top_background)

        resultTitle = getString(R.string.role_decision) + getString(R.string.result)
        findViewById<TextView>(R.id.result_title).text = resultTitle

        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_group))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.by_member))
        tabLayout.addOnTabSelectedListener(tabItemSelectedListener)

        PublicMethods.showAd(this)
        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray =
                i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) != null) {
            groupArray =
                i.getSerializableExtra(KumiwakeArrayKeys.GROUP_LIST.key) as ArrayList<Group>
        }

        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        evenAgeRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)
        groupCount = groupArray.size

        startMethod()

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        val retryButton = findViewById<Button>(R.id.re_kumiwake)
        retryButton.text = getString(R.string.retry)
        retryButton.setOnClickListener { onRetry() }
        findViewById<ImageButton>(R.id.edit_result_title).setOnClickListener { editInfoDialog() }
        findViewById<Button>(R.id.share_result).setOnClickListener { shareResult() }
        findViewById<Button>(R.id.go_sekigime).visibility = View.GONE
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


    private fun startMethod() {
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
            KumiwakeMethods.evenManDistribute(memberArray.size, resultArray, manArray, groupArray)
            KumiwakeMethods.evenWomanDistribute(resultArray, womanArray, groupArray)
        } else if (evenFmRatio) {
            createFmArray()    //男女それぞれの配列を作成
            KumiwakeMethods.evenManDistribute(memberArray.size, resultArray, manArray, groupArray)
            KumiwakeMethods.evenWomanDistribute(resultArray, womanArray, groupArray)
        } else if (evenAgeRatio) {
            KumiwakeMethods.arrangeByAge(memberArray)
            KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, ArrayList())
        } else {
            KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, ArrayList())
        }

        Thread(DrawTask(this, groupCount)).start()
    }

    //男女配列作成
    private fun createFmArray() {
        for (member in memberArray) {
            if (PublicMethods.isMan(member.sex)) {
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
                    Thread(DrawTask(this@RoleResult, groupCount)).start()
                }

                1 -> {//メンバーごと
                    addResultViewByMember()
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
        val view = inflater.inflate(
            R.layout.edit_result_dialog_layout,
            findViewById<View>(R.id.info_layout) as ViewGroup?
        )
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        etTitle.hint = resultTitle
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
        } else {
            findViewById<TextView>(R.id.result_title).text = resultTitle
            findViewById<TextView>(R.id.inner_result_title).text = resultTitle
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
    private fun onRetry() {
        v = 0
        val title = getString(R.string.retry_title)
        val message =
            getString(R.string.re_assign_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(
            title,
            message,
            function = this::retry
        )
    }

    private fun retry() {
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod()
        Toast.makeText(applicationContext, getText(R.string.retry_finished), Toast.LENGTH_SHORT)
            .show()
    }

    private fun shareResult() {
        shareOptionDialog()
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
        val adapter = SmallMBListAdapter(this, resultArray[i])
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
        val adapter = SmallMBListAdapter(this, resultArrayByMember, nameIsSpanned = true)
        arrayList.adapter = adapter
        setBackGround(v, -1)
        adapter.setRowHeight(arrayList)
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
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
        val newName =
            member.name + " → <strong><font color='#" + colorStr + "'>" + groupArray[groupNo].name + "</font></strong>"
        return Member(
            member.id,
            newName,
            member.sex,
            member.age,
            member.belong,
            member.read,
            member.leader
        )
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                                   テキスト共有メソッド                                              ////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //option設定ダイアログ
    private fun shareOptionDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.edit_result_dialog_layout,
            findViewById<View>(R.id.info_layout) as ViewGroup?
        )
        val indexTitle = view.findViewById<TextView>(R.id.title_index)
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        indexTitle.visibility = View.GONE
        etTitle.visibility = View.GONE
        etTitle.hint = resultTitle
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
                KumiwakeMethods.shareResult(
                    this,
                    tvTitle,
                    tvComment,
                    this::shareText,
                    this::shareImage
                )
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    //テキストでシェア
    private fun shareText() {
        val articleTitle = if (includeTitle) {
            if (title != "") {
                "～$title～"
            } else {
                "～${getString(R.string.role)}～"
            }
        } else {
            ""
        }
        val articleComment = if (includeComment && comment != "") {
            "\n$comment"
        } else {
            ""
        }
        var sharedText = ""
        val resultTxt = StringBuilder()

        for ((i, array) in resultArray.withIndex()) {
            resultTxt.append("\n")
            resultTxt.append("《${groupArray[i].name}》\n")

            for (member in array) {
                when {
                    PublicMethods.isMan(member.sex) -> resultTxt.append("♠")
                    PublicMethods.isWoman(member.sex) -> resultTxt.append("♡")
                }
                resultTxt.append("${member.name}\n")
            }
        }

        sharedText = "$articleTitle$articleComment\n$resultTxt"
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
        ShareViewImage.shareView(this, shareLayout, getString(R.string.role))
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
                (context as RoleResult).addResultView(v)
            }
        }
    }

}

