package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MBListViewAdapter
import com.pandatone.kumiwake.customize.CustomDialog
import com.pandatone.kumiwake.member.Name
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.SelectTableType
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class NormalKumiwakeResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Name>
    private lateinit var manArray: ArrayList<Name>
    private lateinit var womanArray: ArrayList<Name>
    private lateinit var resultArray: ArrayList<Name>
    private lateinit var groupArray: ArrayList<GroupListAdapter.Group>
    private lateinit var arrayArray: ArrayList<ArrayList<Name>>
    internal var groupCount: Int = 0
    internal var memberSize: Int = 0
    internal var memberSum = 0
    internal var v = 0
    internal var nowGroupNo = 0
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
        if(i.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) != null) {
            memberArray = i.getSerializableExtra(NormalMode.NORMAL_MEMBER_ARRAY) as ArrayList<Name>
        }
        if(i.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) != null) {
            groupArray = i.getSerializableExtra(NormalMode.NORMAL_GROUP_ARRAY) as ArrayList<GroupListAdapter.Group>
        }
        even_fm_ratio = i.getBooleanExtra(KumiwakeCustom.EVEN_FM_RATIO, false)
        even_age_ratio = i.getBooleanExtra(KumiwakeCustom.EVEN_AGE_RATIO, false)
        groupCount = groupArray.size
        memberSize = memberArray.size

        startMethod()

        if (!KumiwakeSelectMode.sekigime) {
            timer = Timer()
            timerTask = MyTimerTask(this)
            timer!!.scheduleAtFixedRate(timerTask, 100, 100)
        } else {
            val groupNameArray = ArrayList<String>(groupCount)
            for (j in 0 until groupCount) {
                groupNameArray.add(groupArray[j].group)
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
            arrayArray.add(savedInstanceState.getSerializable("ARRAY$g") as ArrayList<Name>)
        }
    }


    private fun startMethod() {
        memberArray.shuffle()

        arrayArray = ArrayList(groupCount)
        for (g in 0 until groupCount) {
            arrayArray.add(ArrayList())
        }

        if (even_fm_ratio) {
            CreateFmArray()    //男女それぞれの配列を作成
        }

        if (!even_fm_ratio && !even_age_ratio) {
            kumiwakeAll()
        } else if (even_fm_ratio && !even_age_ratio) {
            EvenKumiwakeSetter(manArray, womanArray, "")
            EvenCreateGroup(manArray)
            EvenCreateGroup(womanArray)
        } else if (!even_fm_ratio && even_age_ratio) {
            EvenKumiwakeSetter(memberArray, null, "age")
            EvenCreateGroup(memberArray)
        }
    }


    fun addGroupView() {
        if (v < groupCount) {
            resultArray = arrayArray[v]
            Collections.sort(resultArray, KumiwakeLeaderComparator())
            addView(resultArray, v)
            v++
        }
        if (v == groupCount) {
            timer!!.cancel()
        }
    }

    @OnClick(R.id.re_kumiwake)
    internal fun onReKumiwake() {
        memberSum = 0
        v = 0
        nowGroupNo = 0
        timerTask = MyTimerTask(this)
        val title = getString(R.string.re_kumiwake_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        val customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        CustomDialog.mPositiveBtnListener = View.OnClickListener {
            val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
            scrollView.scrollTo(0, 0)
            startMethod()
            timer = Timer()
            timer!!.scheduleAtFixedRate(timerTask, 100, 100)
            customDialog.dismiss()
            Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
        }
        customDialog.show(supportFragmentManager, "Btn")
    }

    @OnClick(R.id.go_sekigime)
    internal fun onClicked() {
        val groupNameArray = ArrayList<String>(groupCount)
        for (j in 0 until groupCount) {
            groupNameArray.add(groupArray[j].group)
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

    fun kumiwakeAll() {
        setLeader(memberArray)

        for (i in 0 until groupCount) {
            val belongNo = groupArray[i].belongNo - arrayArray[i].size  //グループの規定人数－グループの現在数
            resultArray = kumiwakeCreateGroup(memberArray, belongNo)
            for (j in resultArray.indices) {
                arrayArray[i].add(resultArray[j])
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //　　　　　　　　　　　　　　　　　　　　　　　　各種処理メソッド                                                           ///
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun setLeader(array: ArrayList<Name>) {
        for (i in array.indices) {
            if (array[i].role.matches((".*" + getText(R.string.leader) + ".*").toRegex())) {
                val roleArray = array[i].role.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val list = ArrayList(Arrays.asList<String>(*roleArray))
                list.remove("")
                list.sort()
                val LDNo = Integer.parseInt(list[0].substring(2))
                arrayArray[LDNo - 1].add(array[i])
            }
        }
    }

    fun CreateFmArray() {
        manArray = ArrayList()
        womanArray = ArrayList()

        for (i in memberArray.indices) {
            if (memberArray[i].sex == getText(R.string.man)) {
                manArray.add(memberArray[i])
            }
        }

        for (j in memberArray.indices) {
            if (memberArray[j].sex == getText(R.string.woman)) {
                womanArray.add(memberArray[j])
            }
        }
    }

    fun EvenKumiwakeSetter(array1: ArrayList<Name>, array2: ArrayList<Name>?, sortCode: String) {

        if (sortCode == "age") {
            array1.shuffle()
            Collections.sort(array1, KumiwakeNumberComparator())
        }
        if (sortCode == "age" && array2 != null) {
            array2.shuffle()
            Collections.sort(array2, KumiwakeNumberComparator())
        }
        setLeader(array1)
        if (array2 != null) {
            setLeader(array2)
        }
    }

    private fun kumiwakeCreateGroup(array: ArrayList<Name>, belongNo: Int): ArrayList<Name> {
        val result = ArrayList<Name>()

        for (j in 0 until belongNo) {
            if (memberSum < array.size) {
                while (array[memberSum].role.matches((".*" + getText(R.string.leader) + ".*").toRegex())) {
                    memberSum++
                }
                while (memberSum != array.size && array[memberSum].role.matches(".*$.*".toRegex())) {
                    memberSum++
                }
                if (memberSum < array.size) {
                    result.add(array[memberSum])
                    memberSum++
                }
            }
        }
        return result
    }

    private fun EvenCreateGroup(array: ArrayList<Name>) {
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
        memberSum = 0
        nowGroupNo = 0
        nowGroupMemberCount = arrayArray[0].size

        while (memberSum < array.size) {

            while (memberSum < array.size && (array[memberSum].role.matches((".*" + getText(R.string.leader) + ".*").toRegex()) || array[memberSum].role.matches(".*$.*".toRegex()))) {
                memberSum++
            }

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
    private fun addView(resultArray: ArrayList<Name>, i: Int) {
        val groupName: TextView
        val arrayList: ListView
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        val v = layoutInflater.inflate(R.layout.result_parts, null)
        if (i == 0) {
            layout.removeAllViews()
        }
        layout.addView(v)
        groupName = v.findViewById<View>(R.id.result_group) as TextView
        groupName.text = groupArray[i].group
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = MBListViewAdapter(this, resultArray, 2000)
        arrayList.adapter = adapter
        setBackGround(v)
        MBListViewAdapter.setRowHeight(arrayList, adapter)
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

    companion object {
        internal var even_fm_ratio: Boolean = false
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

internal class KumiwakeLeaderComparator : Comparator<Name> {

    override fun compare(n1: Name, n2: Name): Int {
        var value = 0
        val leader = ".*" + R.string.leader.toString() + ".*"

        if (n1.role.matches(leader.toRegex()) && !n2.role.matches(leader.toRegex())) {
            value = -1
        } else if (n2.role.matches(leader.toRegex()) && !n1.role.matches(leader.toRegex())) {
            value = 1
        }

        if (n1.role.matches(leader.toRegex()) && n2.role.matches(leader.toRegex())) {
            val roleArray1 = n1.role.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val roleArray2 = n2.role.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val list1 = ArrayList(Arrays.asList<String>(*roleArray1))
            val list2 = ArrayList(Arrays.asList<String>(*roleArray2))
            list1.remove("")
            list2.remove("")
            list1.sort()
            list2.sort()
            val LDNo1 = Integer.parseInt(list1[0].substring(2))
            val LDNo2 = Integer.parseInt(list2[0].substring(2))
            if (LDNo1 < LDNo2) {
                value = -1
            } else {
                value = 1
            }
        }

        if (value == 0) {
            val n1_sex = n1.sex
            val n2_sex = n2.sex

            value = n2_sex.compareTo(n1_sex)
        }

        if (NormalKumiwakeResult.even_age_ratio) {
            if (value == 0) {
                val n1_age = n1.age
                val n2_age = n2.age
                if (n1_age < n2_age) {
                    value = -1
                } else if (n1_age > n2_age) {
                    value = 1
                } else {
                    value = 0
                }
            }
        }

        return value
    }
}

internal class KumiwakeNumberComparator : Comparator<Name> {

    override fun compare(n1: Name, n2: Name): Int {
        var value = 0

            val n1_age = n1.age
            val n2_age = n2.age
            value = when {
                n1_age < n2_age -> -1
                n1_age > n2_age -> 1
                else -> 0
            }

        return value
    }
}

