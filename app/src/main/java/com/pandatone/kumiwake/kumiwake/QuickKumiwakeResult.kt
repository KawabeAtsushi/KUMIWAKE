package com.pandatone.kumiwake.kumiwake

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.customize.CustomDialog
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.SelectTableType
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
    private lateinit var arrayArray: ArrayList<ArrayList<String>>
    private lateinit var viewGroup: RelativeLayout

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
        memberArray = i.getStringArrayListExtra("QuickModeMemberList")
        manArray = i.getStringArrayListExtra("QuickModeManList")
        womanArray = i.getStringArrayListExtra("QuickModeWomanList")
        groupArray = i.getStringArrayListExtra("QuickModeGroupList")
        even_fm_ratio = i.getBooleanExtra("EvenFMRatio", false)
        even_person_ratio = i.getBooleanExtra("EvenPersonRatio", false)
        groupNo = groupArray.size
        viewGroup = findViewById<View>(R.id.result_view) as RelativeLayout
        viewGroup.background = ContextCompat.getDrawable(this, R.drawable.quick_img)

        startMethod()

        if (!KumiwakeSelectMode.sekigime) {
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
            arrayArray.add(savedInstanceState.getStringArrayList("ARRAY$g"))
        }
    }


    private fun startMethod() {
        arrayArray = ArrayList(groupNo)

        for (g in 0 until groupNo) {
            arrayArray.add(ArrayList())
        }

        if (!even_fm_ratio) {
            kumiwakeAll()
        } else if (!even_person_ratio) {
            kumiwakeFm(0)
        } else {
            kumiwakeFm(manArray.size % groupNo)
        }
    }

    @OnClick(R.id.re_kumiwake)
    internal fun onReKumiwake() {
        val title = getString(R.string.re_kumiwake_title)
        val message = getString(R.string.re_kumiwake_description) + getString(R.string.run_confirmation)
        val customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        CustomDialog.mPositiveBtnListener = View.OnClickListener {
            val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
            scrollView.scrollTo(0, 0)
            startMethod()
            for (i in 0 until groupNo) {
                resultArray = arrayArray[i]
                Collections.sort(resultArray, KumiwakeComparator())
                addView(resultArray, i)
            }
            customDialog.dismiss()
            Toast.makeText(applicationContext, getText(R.string.re_kumiwake_finished), Toast.LENGTH_SHORT).show()
        }
        customDialog.show(fragmentManager, "Btn")
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


    fun kumiwakeAll() {
        memberArray.shuffle()
        var remainder: Int

        for (i in memberArray.indices) {
            remainder = i % groupNo
            arrayArray[remainder].add(memberArray[i])
        }
    }

    fun kumiwakeFm(firstPos: Int) {
        manArray.shuffle()
        womanArray.shuffle()
        var manRemainder: Int
        var womanRemainder: Int

        for (i in manArray.indices) {
            manRemainder = i % groupNo
            arrayArray[manRemainder].add(manArray[i])
        }

        for (i in womanArray.indices) {
            womanRemainder = (i + firstPos) % groupNo
            arrayArray[womanRemainder].add(womanArray[i])
        }
    }


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
        v.setBackgroundDrawable(drawable)

    }

    companion object {
        internal var even_fm_ratio: Boolean = false
        internal var even_person_ratio: Boolean = false
    }
}


internal class KumiwakeComparator : Comparator<String> {
    override fun compare(s1: String, s2: String): Int {
        val s1_name = s1.replace("[0-9]".toRegex(), "") //文字列から文字のみ抜き出し
        val s2_name = s2.replace("[0-9]".toRegex(), "")

        var value = s1_name.compareTo(s2_name)

        if (value == 0) {
            val s1_no = Integer.parseInt(s1.replace("[^0-9]".toRegex(), ""))   //文字列から数値のみ抜き出し
            val s2_no = Integer.parseInt(s2.replace("[^0-9]".toRegex(), ""))
            if (s1_no < s2_no) {
                value = -1
            } else {
                value = 1
            }
        }


        return value
    }
}

