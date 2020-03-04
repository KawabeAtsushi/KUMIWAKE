package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.QuickModeKeys
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.SelectTableType
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import java.util.*

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class QuickKumiwakeResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<String>
    private lateinit var manArray: ArrayList<String>
    private lateinit var womanArray: ArrayList<String>
    private lateinit var groupArray: ArrayList<String>
    private lateinit var resultArray: ArrayList<String>
    private var groupNo: Int = 0
    private var evenFmRatio: Boolean = false
    private lateinit var arrayArray: ArrayList<ArrayList<String>>
    private lateinit var viewGroup: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_result)
        ButterKnife.bind(this)
        MobileAds.initialize(applicationContext, "ca-app-pub-2315101868638564/8665451539")
        val mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice("BB707E3F7B5413908B2DD12063887489").build()
        mAdView.loadAd(adRequest)
        val i = intent.also {
            memberArray = it.getStringArrayListExtra(QuickModeKeys.MEMBER_LIST.key)
            manArray = it.getStringArrayListExtra(QuickModeKeys.MAN_LIST.key)
            womanArray = it.getStringArrayListExtra(QuickModeKeys.WOMAN_LIST.key)
            groupArray = it.getStringArrayListExtra(QuickModeKeys.GROUP_LIST.key)
            evenFmRatio = it.getBooleanExtra(QuickModeKeys.EVEN_FM_RATIO.key, false)
        }
        groupNo = groupArray.size

        viewGroup = findViewById<View>(R.id.result_view) as ConstraintLayout
        viewGroup.background = ContextCompat.getDrawable(this, R.drawable.quick_img)

        startMethod()

        if (!StatusHolder.sekigime) {
            for (v in 0 until groupNo) {
                resultArray = arrayArray[v]
                Collections.sort(resultArray, KumiwakeComparator())
                addView(resultArray, v)
            }
        } else {
            for (v in 0 until groupNo) {
                resultArray = arrayArray[v]
                resultArray.shuffle()
            }
            val intent = Intent(this, SelectTableType::class.java)
            SekigimeResult.groupArray = groupArray
            SekigimeResult.arrayArrayQuick = arrayArray
            startActivity(intent)
            finish()
        }

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        for (i in 0 until groupNo) {
            outState.putStringArrayList("ARRAY$i", arrayArray[i])
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        arrayArray = ArrayList(groupNo)
        for (g in 0 until groupNo) {
            savedInstanceState.getStringArrayList("ARRAY$g")?.let { arrayArray.add(it) }
        }
    }


    private fun startMethod() {
        arrayArray = ArrayList(groupNo)

        for (g in 0 until groupNo) {
            arrayArray.add(ArrayList())
        }

        if (evenFmRatio) {
            kumiwakeFm(manArray.size % groupNo)
        } else {
            kumiwakeAll()
        }
    }

    @OnClick(R.id.re_kumiwake)
    internal fun onReKumiwake() {
        val title = getString(R.string.re_kumiwake_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title,message, this::reKumiwake)
    }

    private fun reKumiwake(){
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod()
        for (i in 0 until groupNo) {
            resultArray = arrayArray[i]
            Collections.sort(resultArray, KumiwakeComparator())
            addView(resultArray, i)
        }
        Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.share_result)
    internal fun shareResult() {
        share()
    }

    @OnClick(R.id.go_sekigime)
    internal fun onClicked() {
        for (v in 0 until groupNo) {
            resultArray = arrayArray[v]
            resultArray.shuffle()
        }
        val intent = Intent(this, SelectTableType::class.java)
        SekigimeResult.groupArray = groupArray
        SekigimeResult.arrayArrayQuick = arrayArray
        startActivity(intent)
    }

    @OnClick(R.id.go_home)
    internal fun onClickedHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    private fun kumiwakeAll() {
        memberArray.shuffle()
        var targetGroupNo: Int

        for (i in memberArray.indices) {
            //メンバーを追加するグループを回す(0-groupNo)
            targetGroupNo = i % groupNo
            arrayArray[targetGroupNo].add(memberArray[i])
        }
    }

    private fun kumiwakeFm(firstPos: Int) {
        manArray.shuffle()
        womanArray.shuffle()
        var manTargetGroupNo: Int
        var womanTargetGroupNo: Int

        for (i in manArray.indices) {
            //メンバーを追加するグループを回す(0-groupNo)
            manTargetGroupNo = i % groupNo
            arrayArray[manTargetGroupNo].add(manArray[i])
        }

        for (i in womanArray.indices) {
            //メンバーを追加するグループを回す(0-groupNo)
            womanTargetGroupNo = (i + firstPos) % groupNo
            arrayArray[womanTargetGroupNo].add(womanArray[i])
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////  View  ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("SetTextI18n", "InflateParams")
    fun addView(resultArray: ArrayList<String>, i: Int) {
        val groupName: TextView
        val arrayList: ListView
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        val v = layoutInflater.inflate(R.layout.result_parts, null)
        if (i == 0) {
            layout.removeAllViews()
        }
        layout.addView(v)
        groupName = v.findViewById<View>(R.id.result_group) as TextView
        groupName.text = getText(R.string.group).toString() + " " + (i + 1).toString()
        arrayList = v.findViewById<View>(R.id.result_member_listView) as ListView
        val adapter = MemberArrayAdapter(this, R.layout.mini_row_member, resultArray, false)
        arrayList.adapter = adapter
        setBackGround(v)
        QuickKumiwakeConfirmation.setRowHeight(arrayList, adapter)
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
        val articleTitle = "～" + getString(R.string.kumiwake_result) + "～"
        val resultTxt = StringBuilder()
        for ((i, array) in arrayArray.withIndex()) {
            resultTxt.append("\n")
            resultTxt.append("《${groupArray[i]}》\n")

            for (member in array) {
                resultTxt.append("$member\n")
            }
        }

        val sharedText = "$articleTitle\n$resultTxt"

        // builderの生成　ShareCompat.IntentBuilder.from(Context context);
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


internal class KumiwakeComparator : Comparator<String> {
    override fun compare(s1: String, s2: String): Int {
        val s1Name = s1.replace("[0-9]".toRegex(), "") //文字列から文字のみ抜き出し
        val s2Name = s2.replace("[0-9]".toRegex(), "")

        var value = s1Name.compareTo(s2Name)

        if (value == 0) {
            val s1No = Integer.parseInt(s1.replace("[^0-9]".toRegex(), ""))   //文字列から数値のみ抜き出し
            val s2No = Integer.parseInt(s2.replace("[^0-9]".toRegex(), ""))
            value = if (s1No < s2No) {
                -1
            } else {
                1
            }
        }


        return value
    }
}

