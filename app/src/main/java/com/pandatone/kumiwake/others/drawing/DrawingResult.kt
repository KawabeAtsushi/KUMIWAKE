package com.pandatone.kumiwake.others.drawing

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.DrawingHistoryListAdapter
import com.pandatone.kumiwake.adapter.SmallDrawingHistoryListAdapter
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import kotlinx.android.synthetic.main.drawing_result.*
import kotlin.math.roundToInt


/**
 * Created by atsushi_2 on 2016/05/10.
 */
class DrawingResult : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private lateinit var tapHandler: Handler
    private lateinit var historyListView: ListView
    private lateinit var historyListViewAdapter: SmallDrawingHistoryListAdapter
    private var soundShake = 0
    private var soundDraw = 0
    private var shaking = true
    private var v = 0
    private lateinit var shakeAnimator: Animator
    private lateinit var ticketAnimator: Animator
    private lateinit var tickets: ArrayList<String>
    private var pickedTickets: ArrayList<String> = ArrayList()
    private lateinit var ticketKinds: ArrayList<String>
    private var historyArray: ArrayList<String> = ArrayList()
    private lateinit var countTextView: TextView
    private lateinit var remainTextView: TextView
    private lateinit var drawingAnim:LottieAnimationView
    private lateinit var pleaseTap:LottieAnimationView
    private lateinit var ticket:TextView
    private var pickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_result)

        val i = intent
        if (i.getSerializableExtra("tickets") != null) {
            tickets = i.getSerializableExtra("tickets") as ArrayList<String>
        }
        ticketKinds = ArrayList(tickets.distinct())

        setUpViews()

        setUpAnimators(drawingAnim, ticket)

        initializeViews()

        drawingAnim.setOnClickListener {
            if (!shaking) {//振動開始
                if (pickCount >= tickets.size) {//最終結果
                        animation_views.visibility = View.GONE
                        countTextView.text = getString(R.string.result)
                }else {
                    val countStr = "${pickCount + 1}${getString(R.string.th_time)}"
                    countTextView.text = countStr
                    shakeAnimation(pleaseTap)
                }
            } else if (!drawingAnim.isAnimating) {//pickup開始
                pick(ticket)
                ticket.visibility = View.VISIBLE
                pleaseTap.visibility = View.INVISIBLE
                pleaseTap.cancelAnimation()
                drawingAnim.playAnimation()
                drawingAnimation()
            } else {
                skipAnimation(drawingAnim)
            }
        }

        PublicMethods.showAd(this)

        val historyButton = findViewById<Button>(R.id.drawing_history)
        historyButton.setOnClickListener { onHistory() }
        val showResultButton = findViewById<Button>(R.id.retry_button)
        showResultButton.text = getString(R.string.retry)
        showResultButton.setOnClickListener { onRetry(drawingAnim) }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    //ビュー定義
    private fun setUpViews() {
        drawingAnim = findViewById(R.id.drawing_anim)
        pleaseTap = findViewById(R.id.tap)
        ticket = findViewById(R.id.ticket)
        //progress:0(start)~1(end)
        drawingAnim.setMinProgress(0.5f)
        historyListView = findViewById(R.id.history_list)
        historyListViewAdapter = SmallDrawingHistoryListAdapter(this, historyArray)
        historyListView.adapter = historyListViewAdapter
        historyListView.emptyView = findViewById(R.id.emptyHistoryList)
        countTextView = findViewById(R.id.countTextView)
        remainTextView = findViewById(R.id.remainTextView)
    }

    //ビューの初期化
    private fun initializeViews(){
        animation_views.visibility = View.VISIBLE
        val countStr = "1${getString(R.string.th_time)}"
        countTextView.text = countStr
        val remainStr = "${getString(R.string.remain)} ${tickets.size}${getString(R.string.ticket_unit)}"
        remainTextView.text = remainStr
        historyArray.clear()
        historyListViewAdapter.notifyDataSetChanged()
        pickedTickets.clear()
        tickets.shuffle()
        pickCount = 0
        shakeAnimation(pleaseTap)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// Sounds & Animations /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //サウンドプールのビルド
    override fun onResume() {
        super.onResume()
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_UNKNOWN)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes) // ストリーム数に応じて
                .setMaxStreams(1)
                .build()

        // sound.oggをロード
        soundShake = soundPool.load(this, R.raw.shake, 1)
        soundDraw = soundPool.load(this, R.raw.draw, 1)
    }

    //サウンドプールの解放
    override fun onPause() {
        super.onPause()
        soundPool.release()
    }

    //アニメーター初期化
    private fun setUpAnimators(boxView: LottieAnimationView, ticketView: View) {
        shakeAnimator = AnimatorInflater.loadAnimator(this, R.animator.box_shake_anim)
        shakeAnimator.setTarget(boxView)
        ticketAnimator = AnimatorInflater.loadAnimator(this, R.animator.drawing_anim)
        ticketAnimator.setTarget(ticketView)
        ticketAnimator.doOnEnd { shaking = false }
    }

    //待機状態のアニメーション
    private fun shakeAnimation(pleaseTap: LottieAnimationView) {
        ticket.visibility = View.INVISIBLE
        drawingAnim.progress = 0.5f
        shaking = true

        //繰り返し
        shakeAnimator.doOnEnd {
            shakeAnimator.start()
            Handler().postDelayed({
                // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
                soundPool.play(soundShake, 1.0f, 1.0f, 0, 2, 2.5f)
            }, 500)
        }
        //一回目の処理
        shakeAnimator.start()
        Handler().postDelayed({
            soundPool.play(soundShake, 1.0f, 1.0f, 0, 2, 2.5f)
        }, 500)

        //3秒待って進まなかったらタップを促す
        tapHandler = Handler()
        tapHandler.postDelayed({
            pleaseTap.visibility = View.VISIBLE
            pleaseTap.playAnimation()
        }, 3000)
    }

    //くじ引き開始
    private fun drawingAnimation() {
        //初期状態に戻す
        tapHandler.removeCallbacksAndMessages(null)
        soundPool.stop(soundShake)
        shakeAnimator.start()
        shakeAnimator.pause()
        ticketAnimator.start()
        soundPool.play(soundDraw, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    //アニメーションの途中から結果までスキップ
    private fun skipAnimation(drawingAnim: LottieAnimationView) {
        drawingAnim.progress = 1.0f
        ticketAnimator.end()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// くじ引き処理 //////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //以下、くじ引き処理
    private fun pick(ticketTextView: TextView) {
            val picked = tickets[pickCount]
            ticketTextView.text = picked
            val ticketCol = getTicketColorInt(picked)
            ticketTextView.setTextColor(ticketCol)
            ticketTextView.backgroundTintList = ColorStateList.valueOf(adjustAlpha(ticketCol,0.1f))
            pickedTickets.add(picked)
            pickCount++
            historyArray.add(0, "<font color='${getTicketColorHex(picked)}'>${picked}</font>")
            historyListViewAdapter.notifyDataSetChanged()
            val remainStr = "${getString(R.string.remain)} ${tickets.size - pickCount}${getString(R.string.ticket_unit)}"
            remainTextView.text = remainStr
    }

    //チケットの色取得(Int)
    private fun getTicketColorInt(ticket:String):Int{
        val colorList = TicketDefine.ticketColors
        val index = ticketKinds.indexOf(ticket)
        return colorList[index]
    }

    //チケットの色取得(Hex)
    private fun getTicketColorHex(ticket:String):String{
        val colorList = TicketDefine.ticketColors
        val index = ticketKinds.indexOf(ticket)
        val intColor = colorList[index]
        return String.format("#%06X", 0xFFFFFF and intColor)
    }

    //色にアルファ値設定
    @ColorInt
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////// ボタン処理 ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //履歴確認ボタン
    private fun onHistory() {
        val builder = AlertDialog.Builder(this)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.drawing_history, findViewById<View>(R.id.info_layout) as ViewGroup?)

        val historyList = view.findViewById<View>(R.id.historyList) as ListView
        val okBt = view.findViewById<View>(R.id.okBt) as Button

        builder.setTitle(R.string.history)
        builder.setView(view)

        val adapter = DrawingHistoryListAdapter(this, ticketKinds, tickets, pickedTickets)
        historyList.adapter = adapter

        val dialog = builder.create()
        dialog.show()
        okBt.setOnClickListener { dialog.dismiss() }
    }

    //初めから
    private fun onRetry(drawingAnim: LottieAnimationView) {
        v = 0
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_drawing_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::initializeViews)
    }

    //ホームに戻る
    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}

