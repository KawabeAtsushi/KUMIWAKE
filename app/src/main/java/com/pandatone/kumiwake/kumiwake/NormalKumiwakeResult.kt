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
import androidx.core.app.ShareCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.adapter.SmallMBListAdapter
import com.pandatone.kumiwake.member.Group
import com.pandatone.kumiwake.member.Member
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.SelectTableType
import com.pandatone.kumiwake.ui.DialogWarehouse
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class NormalKumiwakeResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private lateinit var leaderArray: ArrayList<Member>
    private lateinit var groupArray: ArrayList<Group>
    private lateinit var arrayArray: ArrayList<ArrayList<Member>>
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
        ButterKnife.bind(this)
        MobileAds.initialize(applicationContext, "ca-app-pub-2315101868638564/8665451539")
        val mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build()
        mAdView.loadAd(adRequest)
        val i = intent
        if (i.getSerializableExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key) != null) {
            memberArray = i.getSerializableExtra(ArrayKeys.NORMAL_MEMBER_ARRAY.key) as ArrayList<Member>
        }
        if (i.getSerializableExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key) != null) {
            groupArray = i.getSerializableExtra(ArrayKeys.NORMAL_GROUP_ARRAY.key) as ArrayList<Group>
        }
        if (i.getSerializableExtra(ArrayKeys.LEADER_ARRAY.key) != null) {
            leaderArray = i.getSerializableExtra(ArrayKeys.LEADER_ARRAY.key) as ArrayList<Member>
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
                arrayArray[j].shuffle()
            }
            val intent = Intent(this, SelectTableType::class.java)
            SekigimeResult.groupArray = groupNameArray
            SekigimeResult.arrayArrayNormal = arrayArray
            startActivity(intent)
            finish()
        }

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        for (i in 0 until groupCount) {
            outState.putSerializable("ARRAY$i", arrayArray[i])
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        arrayArray = ArrayList(groupCount)
        for (g in 0 until groupCount) {
            arrayArray.add(savedInstanceState.getSerializable("ARRAY$g") as ArrayList<Member>)
        }
    }


    private fun startMethod() {
        memberArray.shuffle()

        arrayArray = ArrayList(groupCount)
        manArray = ArrayList()
        womanArray = ArrayList()

        for (g in 0 until groupCount) {
            arrayArray.add(ArrayList())
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
            addView(arrayArray[v], v)
            v++
        }
        if (v == groupCount) {
            timer!!.cancel()
        }
    }

    @OnClick(R.id.re_kumiwake)
    internal fun onReKumiwake() {
        v = 0
        nowGroupNo = 0
        timerTask = MyTimerTask(this)
        val title = getString(R.string.re_kumiwake_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title,message, this::reKumiwake)
    }

    private fun reKumiwake() {
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timerTask, 100, 100)
        Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.share_result)
    internal fun shareResult() {
        share()
    }

    @OnClick(R.id.go_sekigime)
    internal fun onClicked() {
        val groupNameArray = ArrayList<String>(groupCount)
        for (j in 0 until groupCount) {
            groupNameArray.add(groupArray[j].name)
            arrayArray[j].shuffle()
        }
        val intent = Intent(this, SelectTableType::class.java)
        SekigimeResult.groupArray = groupNameArray
        SekigimeResult.arrayArrayNormal = arrayArray
        startActivity(intent)
    }

    @OnClick(R.id.go_home)
    internal fun onClickedHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun kumiwakeAll() {
        setLeader(leaderArray)

        var sum = 0

        for (i in 0 until groupCount) {
            val addNo = groupArray[i].belongNo - arrayArray[i].size  //グループの規定人数－グループの現在数
            arrayArray[i].addAll(kumiwakeCreateGroup(memberArray, addNo, sum))
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
            arrayArray[leaderNoList.indexOf(id)].add(leader)
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
                while (groupCapacity[k] < arrayArray[k].size) {
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
        nowGroupMemberCount = arrayArray[0].size

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
                        for (i in 0 until arrayArray[addGroupNo].size) {
                            if (arrayArray[addGroupNo][i].sex == getText(R.string.man)) {
                                nowGroupMemberCount++
                            }
                        }

                        if (roopCount > groupCount) {  //一周してもループを抜けない場合
                            for (j in 0 until groupCount) {
                                if (!fullNo[j] && arrayArray[j].size < min) {
                                    min = arrayArray[j].size
                                    minJ = j
                                }
                            }
                            addGroupNo = minJ  //要素数が許容格納数に達していないグループに追加（groupCapacityは達している）
                            break             //ループを抜ける
                        }
                    }

                    if (groupArray[addGroupNo].belongNo == arrayArray[addGroupNo].size) {
                        fullNo[addGroupNo] = true
                    }
                    roopCount++
                }
            } else {
                while (memberSum < array.size && groupArray[addGroupNo].belongNo == nowGroupMemberCount) {
                    nowGroupNo++
                    addGroupNo = nowGroupNo % groupCount
                    nowGroupMemberCount = arrayArray[addGroupNo].size
                }
            }

            if (memberSum < array.size) {
                arrayArray[addGroupNo].add(array[memberSum])
                memberSum++
                nowGroupNo++
                addGroupNo = nowGroupNo % groupCount
                nowGroupMemberCount = arrayArray[addGroupNo].size
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
        SmallMBListAdapter.setRowHeight(arrayList, adapter)
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

        v.layoutParams = setMargin(10, 12, 10, 0)
        v.background = drawable
    }

    private fun share() {
        var setLeader = false
        val articleTitle = "～" + getString(R.string.kumiwake_result) + "～"
        val descriptionLeader = "\n☆:" + getString(R.string.leader) + "\n"
        var sharedText = ""
        val resultTxt = StringBuilder()

        for ((i, array) in arrayArray.withIndex()) {
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
        handler.post { (context as NormalKumiwakeResult).addGroupView() }
    }

}

internal class KumiwakeViewComparator : Comparator<Member> {

    override fun compare(n1: Member, n2: Member): Int {
        var value = 0

        val n1_sex = n1.sex
        val n2_sex = n2.sex

        value = n2_sex.compareTo(n1_sex)

        if (NormalKumiwakeResult.even_age_ratio) {
            if (value == 0) {
                val n1Age = n1.age
                val n2Age = n2.age
                value = when {
                    n1Age < n2Age -> -1
                    n1Age > n2Age -> 1
                    else -> 0
                }
            }
        }

        return value
    }
}

internal class KumiwakeAgeComparator : Comparator<Member> {

    override fun compare(n1: Member, n2: Member): Int {
        var value = 0

        val n1Age = n1.age
        val n2Age = n2.age
        value = when {
            n1Age < n2Age -> -1
            n1Age > n2Age -> 1
            else -> 0
        }

        return value
    }
}

internal class KumiwakeLeaderComparator : Comparator<Member> {

    override fun compare(n1: Member, n2: Member): Int {
        var value = 0

        val n1_ld = n1.role
        val n2_ld = n2.role
        value = n1_ld.compareTo(n2_ld)

        return value
    }
}

