package com.pandatone.kumiwake.others.drawing

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.DrawingHistoryListAdapter
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/05/10.
 */
class DrawingResult : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private lateinit var tapHandler: Handler
    private lateinit var historyListView: ListView
    private lateinit var historyListViewAdapter: ArrayAdapter<String>
    private var soundShake = 0
    private var soundDraw = 0
    private var prepared = true
    private var v = 0
    private lateinit var shakeAnimator: Animator
    private lateinit var ticketAnimator: Animator
    private lateinit var tickets: ArrayList<String>
    private var pickedTickets: ArrayList<String> = ArrayList()
    private lateinit var ticketKinds: ArrayList<String>
    private var pickCount = 0
    private var historyArray: ArrayList<String> = ArrayList()
    private lateinit var countTextView: TextView
    private lateinit var remainTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_result)

        val i = intent
        if (i.getSerializableExtra("tickets") != null) {
            tickets = i.getSerializableExtra("tickets") as ArrayList<String>
        }
        ticketKinds = ArrayList(tickets.distinct())
        tickets.shuffle()

        setUpViews()
        updateViews()

        val ticket = findViewById<TextView>(R.id.ticket)
        val drawingAnim = findViewById<LottieAnimationView>(R.id.drawing_anim)
        val pleaseTap = findViewById<LottieAnimationView>(R.id.tap)
        //progress:0(start)~1(end)
        drawingAnim.setMinProgress(0.5f)
        setUpAnimators(drawingAnim, ticket)
        shakeAnimation(pleaseTap)

        drawingAnim.setOnClickListener {
            if (!prepared) {//振動開始
                ticket.visibility = View.INVISIBLE
                drawingAnim.progress = 0.5f
                shakeAnimation(pleaseTap)
                prepared = true
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
        val showResultButton = findViewById<Button>(R.id.show_result)
        showResultButton.text = getString(R.string.retry)
        showResultButton.setOnClickListener { onRetry() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    //ビューの初期化
    private fun setUpViews() {
        historyListView = findViewById(R.id.history_list)
        historyListViewAdapter = ArrayAdapter(this, R.layout.row_simple_drawing_history, historyArray)
        historyListView.adapter = historyListViewAdapter
        historyListView.emptyView = findViewById(R.id.emptyHistoryList)
        countTextView = findViewById(R.id.countTextView)
        remainTextView = findViewById(R.id.remainTextView)
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
        ticketAnimator.doOnEnd { prepared = false }
    }

    //待機状態のアニメーション
    private fun shakeAnimation(pleaseTap: LottieAnimationView) {
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
        pickedTickets.add(picked)
        pickCount++
        historyArray.add(0, "${pickCount}.${picked}")
        historyListViewAdapter.notifyDataSetChanged()
        updateViews()
        if (pickCount >= tickets.size) {
            tickets.shuffle()
            pickCount = 0
        }
    }

    private fun updateViews() {
        val countStr = "${pickCount + 1}${getString(R.string.th_time)}"
        countTextView.text = countStr
        val remainStr = "${getString(R.string.remain)} ${tickets.size - pickCount}${getString(R.string.ticket_unit)}"
        remainTextView.text = remainStr
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
    private fun onRetry() {
        v = 0
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_drawing_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::retry)
    }

    private fun retry() {
        Toast.makeText(applicationContext, getText(R.string.retry_finished), Toast.LENGTH_SHORT).show()
    }

    //ホームに戻る
    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PublicMethods.initialize()
        startActivity(intent)
    }

}

