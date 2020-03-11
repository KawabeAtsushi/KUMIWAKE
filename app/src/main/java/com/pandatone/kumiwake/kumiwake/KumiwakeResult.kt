package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
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
    private lateinit var resultArray: ArrayList<ArrayList<Member>>
    private lateinit var manArray: ArrayList<Member>
    private lateinit var womanArray: ArrayList<Member>
    private var groupCount: Int = 0
    private var memberSize: Int = 0
    private var evenFmRatio: Boolean = false
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
        if (i.getSerializableExtra(KumiwakeArrayKeys.LEADER_LIST.key) != null) {
            leaderArray = i.getSerializableExtra(KumiwakeArrayKeys.LEADER_LIST.key) as ArrayList<Member>
        }

        evenFmRatio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_FM_RATIO.key, false)
        even_age_ratio = i.getBooleanExtra(KumiwakeCustomKeys.EVEN_AGE_RATIO.key, false)
        groupCount = groupArray.size
        memberSize = memberArray.size

        startMethod()

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


    private fun startMethod() {
        memberArray.shuffle()

        resultArray = ArrayList(groupCount)
        manArray = ArrayList()
        womanArray = ArrayList()

        for (g in 0 until groupCount) {
            resultArray.add(ArrayList())
        }

        if (evenFmRatio) {
            createFmArray()    //男女それぞれの配列を作成
        }

        if (evenFmRatio && even_age_ratio) {
            evenKumiwakeSetter(manArray, womanArray, "age")
            evenCreateGroup(manArray)
            evenCreateGroup(womanArray)
        } else if (evenFmRatio) {
            evenKumiwakeSetter(manArray, womanArray, "")
            evenCreateGroup(manArray)
            evenCreateGroup(womanArray)
        } else if (even_age_ratio) {
            evenKumiwakeSetter(memberArray, null, "age")
            evenCreateGroup(memberArray)
        } else {
            kumiwakeAll()
        }
    }


    fun addGroupView() {
        if (v < groupCount) {
            addView(resultArray[v], v)
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
        startMethod()
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

    private fun kumiwakeAll() {
        setLeader(leaderArray)

        var sum = 0

        for (i in 0 until groupCount) {
            val addNo = groupArray[i].belongNo - resultArray[i].size  //グループの規定人数－グループの現在数
            resultArray[i].addAll(kumiwakeCreateGroup(memberArray, addNo, sum))
            sum += addNo
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //　　　　　　　　　　　　　　　　　　　　　　　　各種処理メソッド                                                           ///
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun setLeader(array: ArrayList<Member>) {
        val leaderNoList = KumiwakeCustom.leaderNoList

        for (leader in array) {
            val id = leader.id
            resultArray[leaderNoList.indexOf(id)].add(leader)
        }
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

    private fun evenKumiwakeSetter(array1: ArrayList<Member>, array2: ArrayList<Member>?, sortCode: String) {

        if (sortCode == "age") {
            array1.shuffle()
            Collections.sort(array1, KumiwakeAgeComparator())
        }
        if (sortCode == "age" && array2 != null) {
            array2.shuffle()
            Collections.sort(array2, KumiwakeAgeComparator())
        }

        setLeader(leaderArray)
    }

    private fun kumiwakeCreateGroup(array: ArrayList<Member>, addNo: Int, sum: Int): ArrayList<Member> {
        val result = ArrayList<Member>()

        for (i in 0 until addNo) {
            result.add(array[sum + i])
        }

        Collections.sort(result, KumiwakeViewComparator())
        return result
    }

    private fun evenCreateGroup(array: ArrayList<Member>) {
        val groupCapacity = IntArray(groupCount)
        var addGroupNo = 0
        var nowGroupMemberCount: Int

        if (array === manArray) {
            val groupCapacity0 = DoubleArray(groupCount)
            var max: Double
            var addSum = 0
            var maxJ = 0

            for (i in 0 until groupCount) {
                groupCapacity0[i] = groupArray[i].belongNo.toDouble() / memberArray.size * array.size
                groupCapacity[i] = groupCapacity0[i].toInt()    //型変換の際に小数点以下は切り捨てられる
                addSum += groupCapacity[i]
                groupCapacity0[i] = groupCapacity0[i] - groupCapacity[i]    //小数点以下の大きさの配列に直す
            }

            for (k in 0 until groupCount) {
                while (groupCapacity[k] < resultArray[k].size) {
                    groupCapacity[k]++  //すでにある要素数より許容格納数が小さいなら＋１する
                    addSum++
                }
                if (groupCapacity[k] == 0) {
                    groupCapacity[k]++  //許容格納数が０なら＋１する
                    addSum++
                }
            }

            while (addSum < array.size) {
                max = 0.0
                for (j in 0 until groupCount) {
                    if (groupCapacity0[j] > max) {
                        groupCapacity0[j] = max
                        maxJ = j
                    }
                }
                groupCapacity[maxJ]++  //小数点以下が大きい要素から順に許容格納数を＋１する
                groupCapacity0[maxJ]-- //同じ要素を連続でとらないように-１する
                addSum++
            }
        }

        val fullNo = BooleanArray(groupCount)  //要素数が許容格納数に達しているグループはtrue
        var memberSum = 0
        nowGroupNo = 0
        nowGroupMemberCount = resultArray[0].size

        while (memberSum < array.size) {

            if (array === manArray) {

                var roopCount = 0
                var min = 5000
                var minJ = 0

                while (memberSum < array.size && (groupCapacity[addGroupNo] == nowGroupMemberCount || fullNo[addGroupNo])) {
                    nowGroupNo++
                    addGroupNo = nowGroupNo % groupCount
                    nowGroupMemberCount = 0

                    if (!fullNo[addGroupNo]) {
                        for (i in 0 until resultArray[addGroupNo].size) {
                            if (resultArray[addGroupNo][i].sex == getText(R.string.man)) {
                                nowGroupMemberCount++
                            }
                        }

                        if (roopCount > groupCount) {  //一周してもループを抜けない場合
                            for (j in 0 until groupCount) {
                                if (!fullNo[j] && resultArray[j].size < min) {
                                    min = resultArray[j].size
                                    minJ = j
                                }
                            }
                            addGroupNo = minJ  //要素数が許容格納数に達していないグループに追加（groupCapacityは達している）
                            break             //ループを抜ける
                        }
                    }

                    if (groupArray[addGroupNo].belongNo == resultArray[addGroupNo].size) {
                        fullNo[addGroupNo] = true
                    }
                    roopCount++
                }
            } else {
                while (memberSum < array.size && groupArray[addGroupNo].belongNo == nowGroupMemberCount) {
                    nowGroupNo++
                    addGroupNo = nowGroupNo % groupCount
                    nowGroupMemberCount = resultArray[addGroupNo].size
                }
            }

            if (memberSum < array.size) {
                resultArray[addGroupNo].add(array[memberSum])
                memberSum++
                nowGroupNo++
                addGroupNo = nowGroupNo % groupCount
                nowGroupMemberCount = resultArray[addGroupNo].size
            }
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////                                 描画メソッド                                             ////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("InflateParams")
    private fun addView(resultArray: ArrayList<Member>, i: Int) {
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
        val adapter = SmallMBListAdapter(this, resultArray, true, showLeaderNo = false)
        arrayList.adapter = adapter
        setBackGround(v)
        adapter.setRowHeight(arrayList)
    }

    private fun setMargin(leftDp: Int, topDp: Int, rightDp: Int, bottomDp: Int): LinearLayout.LayoutParams {
        val scale = resources.displayMetrics.density //画面のdensityを指定。
        val left = (leftDp * scale + 0.5f).toInt()
        val top = (topDp * scale + 0.5f).toInt()
        val right = (rightDp * scale + 0.5f).toInt()
        val bottom = (bottomDp * scale + 0.5f).toInt()
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(left, top, right, bottom)
        return layoutParams
    }


    private fun setBackGround(v: View) {
        val drawable = GradientDrawable()
        drawable.mutate()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 25f
        var R = 0
        var G = 0
        var B = 0
        while (R < 200 && G < 200 && B < 200) {
            R = ((Math.random() * 0.5 + 0.5) * 256).toInt()
            G = ((Math.random() * 0.5 + 0.5) * 256).toInt()
            B = ((Math.random() * 0.5 + 0.5) * 256).toInt()
        }
        drawable.setColor(Color.argb(150, R, G, B))

        v.layoutParams = setMargin(4, 6, 4, 6)
        v.background = drawable
    }

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

    companion object {
        internal var even_age_ratio: Boolean = false
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

// ソート（性別→年齢→ID）
internal class KumiwakeViewComparator : Comparator<Member> {
    override fun compare(n1: Member, n2: Member): Int {
        var value = comparedValue(n2.sex, n1.sex)
        if (value == 0) {
            value = compareValues(n2.age, n1.age)
        }
        if (value == 0) {
            value = compareValues(n1.id, n2.id)
        }
        return value
    }
}

internal class KumiwakeAgeComparator : Comparator<Member> {
    override fun compare(n1: Member, n2: Member): Int {
        return compareValues(n2.age, n1.age)
    }
}

internal class KumiwakeLeaderComparator : Comparator<Member> {
    override fun compare(n1: Member, n2: Member): Int {
        return comparedValue(n1.role, n2.role)
    }
}

//ジェネリクス（宣言時に引数の型定義ができるもの。引数の型推定もできる）
fun <T> comparedValue(n1: T, n2: T): Int {
    var value = 0
    if (n1 is String && n2 is String) {
        //n1が大きい場合には正の値、小さければ負の値
        value = n1.compareTo(n2)
    } else if (n1 is Int && n2 is Int) {
        value = when {
            n1 > n2 -> 1
            n1 < n2 -> -1
            else -> 0
        }
    }
    return value
}

