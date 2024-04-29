package com.pandatone.kumiwake.others.order

import android.content.Context
import android.content.Intent
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.KumiwakeArrayKeys
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ShareViewImage
import com.pandatone.kumiwake.extension.getSerializable
import com.pandatone.kumiwake.kumiwake.function.KumiwakeMethods
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class OrderResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private var v = 0
    private var resultTitle: String = ""

    //結果タイトル&コメント
    private var title = ""
    private var comment = ""
    private var includeTitle = false
    private var includeComment = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kumiwake_result)

        resultTitle = getString(R.string.order) + getString(R.string.result)
        findViewById<TextView>(R.id.result_title).text = resultTitle
        findViewById<View>(R.id.custom_display_style).visibility = View.GONE
        val layout = findViewById<ConstraintLayout>(R.id.result_view)
        layout.background = ContextCompat.getDrawable(this, R.drawable.top_background)

        PublicMethods.showAd(this)

        intent.getSerializable<ArrayList<Member>>(KumiwakeArrayKeys.MEMBER_LIST.key)
            ?.let { memberArray = it }

        //結果表示
        startMethod()

        val scrollView = findViewById<View>(R.id.kumiwake_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        val retryButton = findViewById<Button>(R.id.re_kumiwake)
        retryButton.text = getString(R.string.retry)
        retryButton.setOnClickListener { onRetry() }
        findViewById<ImageButton>(R.id.edit_result_title).setOnClickListener { editInfoDialog() }
        findViewById<Button>(R.id.share_result).setOnClickListener { shareResult() }
        findViewById<Button>(R.id.re_use_members).visibility = View.GONE
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    private fun startMethod() {
        memberArray.shuffle()
        //描画を別スレッドで行う
        Thread(DrawTask(this, memberArray.size)).start()
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
            getString(R.string.re_order_description) + getString(R.string.run_confirmation)
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
        val name: TextView
        val number: TextView
        val layout = findViewById<View>(R.id.result_layout) as LinearLayout
        val v = layoutInflater.inflate(R.layout.order_result_parts, null)
        if (i == 0) {
            layout.removeAllViews()
        }
        layout.addView(v)
        number = v.findViewById(R.id.order_number_textView)
        (i + 1).toString().also { number.text = it }
        name = v.findViewById(R.id.order_name_textView)
        name.text = memberArray[i].name
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
        val titleContainer = view.findViewById<View>(R.id.title_container)
        val etTitle = titleContainer.findViewById<EditText>(R.id.edit_title)
        etTitle.hint = resultTitle
        if (title != "") etTitle.setText(this.title)
        val commentContainer = view.findViewById<View>(R.id.comment_container)
        val etComment = commentContainer.findViewById<EditText>(R.id.edit_comment)
        if (comment != "") etComment.setText(this.comment)

        val includeTitleCheck = view.findViewById<CheckBox>(R.id.include_title_check)
        val includeCommentCheck = view.findViewById<CheckBox>(R.id.include_comment_check)

        includeTitleCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                titleContainer.visibility = View.VISIBLE
            } else {
                titleContainer.visibility = View.GONE
            }
        }
        includeCommentCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                commentContainer.visibility = View.VISIBLE
            } else {
                commentContainer.visibility = View.GONE
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
                "～${getString(R.string.order)}～"
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

        for ((i, member) in memberArray.withIndex()) {
            resultTxt.append("\n" + (i + 1).toString())
            resultTxt.append(" ${member.name}")
        }

        sharedText = "$articleTitle$articleComment\n$resultTxt"
        // builderの生成
        val builder = ShareCompat.IntentBuilder.from(this)
        // アプリ一覧が表示されるDialogのタイトルの設定
        builder.setChooserTitle(R.string.choose_app)
        // シェアするタイトル
        builder.setSubject(getText(R.string.kumiwake_result).toString())
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
        ShareViewImage.shareView(this, shareLayout, getString(R.string.order))
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

