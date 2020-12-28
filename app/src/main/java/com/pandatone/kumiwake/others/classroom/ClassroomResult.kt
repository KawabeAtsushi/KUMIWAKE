package com.pandatone.kumiwake.others.classroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.pandatone.kumiwake.*
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse

/**
 * Created by atsushi_2 on 2016/05/10.
 */
class ClassroomResult : AppCompatActivity() {

    private lateinit var memberArray: ArrayList<Member>
    private var resultTitle: String = ""
    private var alterFm = false
    private var attachSeat = false
    private var memberNo = 0

    //結果タイトル&コメント
    private var title = ""
    private var comment = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classroom_result)

        resultTitle = getString(R.string.classroom_result)
        val layout = findViewById<ConstraintLayout>(R.id.result_view)

        PublicMethods.showAd(this)
        val i = intent
        if (i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) != null) {
            memberArray = i.getSerializableExtra(KumiwakeArrayKeys.MEMBER_LIST.key) as ArrayList<Member>
        }
        alterFm = i.getBooleanExtra(ClassroomCustomKeys.ALTER_FM_SEAT.key, false)
        attachSeat = i.getBooleanExtra(ClassroomCustomKeys.ATTACH_SEAT.key, false)

        memberNo = memberArray.size

        //結果表示
        startMethod()

        val scrollView = findViewById<View>(R.id.classroom_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        val retryButton = findViewById<Button>(R.id.re_kumiwake)
        retryButton.text = getString(R.string.retry)
        retryButton.setOnClickListener { onRetry() }
        findViewById<ImageButton>(R.id.edit_result_title).setOnClickListener { editInfoDialog() }
        findViewById<Button>(R.id.share_result).setOnClickListener { onShareImage() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    private fun startMethod() {
        memberArray.shuffle()
        if (alterFm) {
            createAlterArray()
        }
        //描画を別スレッドで行う
        Thread(DrawTask(this)).start()
    }

    //男女となり
    private fun createAlterArray() {
        val manArray = memberArray.filter { PublicMethods.isMan(it.sex) }
        val womanArray = memberArray.filter { !PublicMethods.isMan(it.sex) }
        memberArray.clear()
        var count = 0
        var manCount = 0
        var womanCount = 0
        while (count < memberNo) {
            if (manCount < manArray.size) {
                memberArray.add(manArray[manCount])
                manCount++
                count++
            }
            if (womanCount < womanArray.size) {
                memberArray.add(womanArray[womanCount])
                womanCount++
                count++
            }
        }
    }

    //情報変更ダイアログ
    private fun editInfoDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.edit_result_dialog_layout, findViewById<View>(R.id.info_layout) as ViewGroup?)
        val etTitle = view.findViewById<EditText>(R.id.edit_title)
        etTitle.hint = getString(R.string.classroom_hint)
        if (title != "") etTitle.setText(this.title)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        if (comment != "") etComment.setText(this.comment)
        view.findViewById<CheckBox>(R.id.include_title_check).visibility = View.GONE
        view.findViewById<CheckBox>(R.id.include_comment_check).visibility = View.GONE
        builder.setTitle(getString(R.string.add_info))
                .setView(view)
                .setPositiveButton(R.string.change) { _, _ ->
                    changeInfo(etTitle.text.toString(), etComment.text.toString(), false)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //情報変更
    private fun changeInfo(newTitle: String, newComment: String, share: Boolean) {
        title = newTitle
        val tvTitle = findViewById<TextView>(R.id.result_title)
        val tvInnerTitle = findViewById<TextView>(R.id.inner_result_title)
        if (title != "") {
            tvTitle.text = title
            tvInnerTitle.text = title
        } else {
            tvTitle.text = getString(R.string.classroom_result)
            tvInnerTitle.text = getString(R.string.classroom_result)
        }
        comment = newComment
        val tvComment = findViewById<TextView>(R.id.comment_view)
        val tvInnerComment = findViewById<TextView>(R.id.inner_comment_view)
        if (comment != "") {
            tvComment.text = comment
            tvComment.visibility = View.VISIBLE
            tvInnerComment.text = comment
            if (share) tvInnerComment.visibility = View.VISIBLE
        } else {
            tvComment.visibility = View.GONE
            tvInnerComment.visibility = View.GONE
        }
    }

    //以下、ボタンの処理
    private fun onRetry() {
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_classroom_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, function = this::retry)
    }

    private fun retry() {
        val scrollView = findViewById<View>(R.id.classroom_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        startMethod()
        Toast.makeText(applicationContext, getText(R.string.retry_finished), Toast.LENGTH_SHORT).show()
    }

    //結果画像共有
    private fun onShareImage() {
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

    fun setResultView() {

        val layout = findViewById<View>(R.id.result_layout) as LinearLayout

        // TableLayoutの生成
        val tableLayout = TableLayout(this).also { tableLayout ->
            var total = 0
            for (row in 0 until rowCount) {
                // 行
                TableRow(this).also { tableRow ->
                    for (column in 0 until columnCount) {
                        val seat = layoutInflater.inflate(R.layout.classroom_seats, null)
                        if (attachSeat) {
                            if (column % 2 == 0) {
                                seat.updatePadding(right = 0)
                            } else {
                                seat.updatePadding(left = 0)
                            }
                        }
                        val seatNameTv = seat.findViewById<TextView>(R.id.seat_name)
                        val member = memberArray[total]
                        seatNameTv.text = member.name
                        if (PublicMethods.isMan(member.sex)) {
                            seatNameTv.setTextColor(PublicMethods.getColor(this, R.color.blue_title))
                        } else if (PublicMethods.isMan(member.sex)) {
                            seatNameTv.setTextColor(PublicMethods.getColor(this, R.color.woman))
                        }
                        tableRow.addView(seat)
                        total++
                        if (total == memberNo) break
                    }
                    // TableLayoutに各行を設定
                    tableLayout.addView(tableRow)
                }
                if (total == memberNo) break
            }
        }

        layout.removeAllViews()
        layout.addView(tableLayout)
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
        etTitle.hint = getString(R.string.classroom_hint)
        if (title != "") etTitle.setText(this.title)
        val indexComment = view.findViewById<TextView>(R.id.comment_index)
        val etComment = view.findViewById<EditText>(R.id.edit_comment)
        indexComment.visibility = View.GONE
        etComment.visibility = View.GONE
        if (comment != "") etComment.setText(this.comment)

        val includeTitleCheck = view.findViewById<CheckBox>(R.id.include_title_check)
        val includeCommentCheck = view.findViewById<CheckBox>(R.id.include_comment_check)
        val tvTitle = findViewById<TextView>(R.id.inner_result_title)
        tvTitle.visibility = View.GONE
        val tvComment = findViewById<TextView>(R.id.inner_comment_view)
        tvComment.visibility = View.GONE

        includeTitleCheck.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                indexTitle.visibility = View.VISIBLE
                etTitle.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
            } else {
                indexTitle.visibility = View.GONE
                etTitle.visibility = View.GONE
                tvTitle.visibility = View.GONE
            }
        }
        includeCommentCheck.setOnCheckedChangeListener { _, checked ->

            if (checked) {
                indexComment.visibility = View.VISIBLE
                etComment.visibility = View.VISIBLE
                if (tvComment.text != "") {
                    tvComment.visibility = View.VISIBLE
                }

            } else {
                indexComment.visibility = View.GONE
                etComment.visibility = View.GONE
                tvComment.visibility = View.GONE
            }
        }

        etTitle.doAfterTextChanged { changeInfo(etTitle.text.toString(), etComment.text.toString(), true) }
        etComment.doAfterTextChanged { changeInfo(etTitle.text.toString(), etComment.text.toString(), true) }

        builder.setTitle(getString(R.string.share_option))
                .setView(view)
                .setPositiveButton(R.string.share) { _, _ ->
                    shareImage()
                    tvTitle.visibility = View.GONE
                    tvComment.visibility = View.GONE
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    tvTitle.visibility = View.GONE
                    tvComment.visibility = View.GONE
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.show()
    }

    //画像でシェア
    private fun shareImage() {
        val shareLayout = findViewById<LinearLayout>(R.id.whole_result_layout)
        ShareViewImage.shareView(this, shareLayout, getString(R.string.classroom_result))
    }

    companion object {
        var rowCount = 0
        var columnCount = 0
    }

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
////                                   補助処理メソッド                                              ////
/////////////////////////////////////////////////////////////////////////////////////////////////////////


internal class DrawTask(private val context: Context) : Runnable {
    private val handler: Handler = Handler()

    override fun run() {
        handler.post {
            (context as ClassroomResult).setResultView()
        }
    }

}

