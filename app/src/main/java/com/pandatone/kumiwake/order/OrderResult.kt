package com.pandatone.kumiwake.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.kumiwake.function.KumiwakeMethods
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class OrderResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private var v = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_result)

        val resultTitle = getString(R.string.order)+getString(R.string.result)
        findViewById<TextView>(R.id.result_title).text = resultTitle
        findViewById<View>(R.id.tabLayout).visibility = View.GONE
        val layout = findViewById<ConstraintLayout>(R.id.result_view)
        layout.background = getDrawable(R.drawable.img_others_background)

        PublicMethods.showAd(this)
        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }

        //結果表示
        startMethod()

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        val retryButton = findViewById<Button>(R.id.re_kumiwake)
        retryButton.text = getString(R.string.retry)
        retryButton.setOnClickListener { onRetry() }
        findViewById<Button>(R.id.share_result).setOnClickListener { shareResult() }
        findViewById<Button>(R.id.go_sekigime).visibility = View.GONE
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    private fun startMethod() {
        memberArray.shuffle()
        //描画を別スレッドで行う
        Thread(DrawTask(this, memberArray.size)).start()
    }

    //以下、ボタンの処理
    private fun onRetry() {
        v = 0
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_order_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::retry)
    }

    private fun retry() {
        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod()
        Toast.makeText(applicationContext, getText(R.string.retry_finished), Toast.LENGTH_SHORT).show()
    }

    private fun shareResult() {
        val resultLayout = findViewById<LinearLayout>(R.id.result_layout)
        KumiwakeMethods.shareResult(this, this::share) { ShareViewImage.shareView(this, resultLayout, getString(R.string.kumiwake_result)) }
    }

    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PublicMethods.initialize()
        startActivity(intent)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////                                 描画メソッド                                             ////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun addResultView(i: Int) {
        val name: TextView
        val number: TextView
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        val v = layoutInflater.inflate(R.layout.order_result_parts, null)
        if (i == 0) {
            layout.removeAllViews()
        }
        layout.addView(v)
        number = v.findViewById(R.id.order_number_textView)
        number.text = (i + 1).toString()
        name = v.findViewById(R.id.order_name_textView)
        name.text = memberArray[i].name
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                                   テキスト共有メソッド                                              ////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //テキストでシェア
    private fun share() {
        val articleTitle = "～" + getString(R.string.order) + "～"
        var sharedText = ""
        val resultTxt = StringBuilder()

        for ((i, member) in memberArray.withIndex()) {
            resultTxt.append("\n" + (i + 1).toString())
            resultTxt.append(" ${member.name}")
        }

        sharedText = "$articleTitle$resultTxt"
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


internal class DrawTask(private val context: Context, private val memberCount: Int) : Runnable {
    private val handler: Handler = Handler()

    override fun run() {
        handler.post {
            for (v in 0 until memberCount) {
                (context as OrderResult).addResultView(v)
            }
        }
    }

}

