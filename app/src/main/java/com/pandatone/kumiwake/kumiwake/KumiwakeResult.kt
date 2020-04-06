package com.pandatone.kumiwake.kumiwake

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.history.HistoryMethods
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
    private var even_age_ratio: Boolean = false
    private var v = 0
    private var nowGroupNo = 0
    private var timer: Timer? = null
    private lateinit var timerTask: TimerTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_result)
        if (!StatusHolder.normalMode) {
            val layout = findViewById<ConstraintLayout>(R.id.result_view)
            layout.background = getDrawable(R.drawable.quick_img)
        }

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
        even_age_ratio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)
        groupCount = groupArray.size

        startMethod(false)

        if (!StatusHolder.sekigime) {
            timer = Timer()
            timerTask = MyTimerTask(this)
            timer!!.scheduleAtFixedRate(timerTask, 100, 100)
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

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

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
        memberArray.shuffle()

        resultArray = ArrayList(groupCount)
        manArray = ArrayList()
        womanArray = ArrayList()

        for (g in 0 until groupCount) {
            resultArray.add(ArrayList())
        }

        if (evenFmRatio && even_age_ratio) {
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
        } else if (even_age_ratio) {
            KumiwakeMethods.arrangeByAge(memberArray)
            KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, leaderArray, leaderNoList)
        } else {
            if (StatusHolder.normalMode) {
                KumiwakeMethods.kumiwakeAll(resultArray, memberArray, groupArray, leaderArray, leaderNoList)
            } else {
                KumiwakeMethods.kumiwakeAllQuick(resultArray, memberArray, groupArray)
            }
        }

        //履歴に保存
        if (StatusHolder.normalMode) {
            if (StatusHolder.sekigime) {
                HistoryMethods.saveResultToHistory(this, resultArray, 1, false)
            } else {
                HistoryMethods.saveResultToHistory(this, resultArray, 0, again)
            }
        }
    }


    fun addGroupView() {
        if (v < groupCount) {
            addResultView(resultArray[v], v)
            v++
        }
        if (v == groupCount) {
            timer!!.cancel()
        }
    }

    private fun onReKumiwake() {
        v = 0
        nowGroupNo = 0
        timerTask = MyTimerTask(this)
        val title = getString(R.string.re_kumiwake_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::reKumiwake)
    }

    private fun reKumiwake() {
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod(true)
        timer = Timer()
        timer!!.scheduleAtFixedRate(timerTask, 100, 100)
        Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
    }

    private fun shareResult() {
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        KumiwakeMethods.shareResult(this, this::share) { ShareViewImage.shareView(this, resultLayout, getString(R.string.kumiwake_result)) }
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

    private fun createFmArray() {

        for (member in memberArray) {
            if (member.sex == getText(R.string.man)) {
                manArray.add(member)
            } else {
                womanArray.add(member)
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////                                 描画メソッド                                             ////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun addResultView(resultArray: ArrayList<Member>, i: Int) {
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
        val adapter = SmallMBListAdapter(this, resultArray, true, showLeaderNo = false, leaderNoList = leaderNoList)
        arrayList.adapter = adapter
        setBackGround(v, i)
        adapter.setRowHeight(arrayList)
    }


    private fun setBackGround(v: View, i: Int) {
        val drawable = GradientDrawable()
        drawable.mutate()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 25f
        drawable.setColor(KumiwakeMethods.getResultColor(i, groupArray.size))

        v.layoutParams = PublicMethods.setMargin(this, 4, 6, 4, 6)
        v.background = drawable
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                                   テキスト共有メソッド                                              ////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //テキストでシェア
    private fun share() {
        var setLeader = false
        val articleTitle = "～" + getString(R.string.kumiwake_result) + "～"
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
            "$articleTitle\n$descriptionLeader$resultTxt"
        } else {
            "$articleTitle\n$resultTxt"
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
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
////                                   補助処理メソッド                                              ////
/////////////////////////////////////////////////////////////////////////////////////////////////////////

internal class MyTimerTask(private val context: Context) : TimerTask() {
    private val handler: Handler = Handler()

    override fun run() {
        handler.post { (context as KumiwakeResult).addGroupView() }
    }

}

