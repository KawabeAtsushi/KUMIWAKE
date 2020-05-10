package com.pandatone.kumiwake.others.drawing

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.pandatone.kumiwake.MainActivity
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse


/**
 * Created by atsushi_2 on 2016/05/10.
 */
class DrawingResult : AppCompatActivity() {

    private var prepared = true
    private var v = 0
    private lateinit var shakeAnimator: Animator
    private lateinit var ticketAnimator: Animator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_result)

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
                ticket.visibility = View.VISIBLE
                pleaseTap.visibility = View.INVISIBLE
                pleaseTap.cancelAnimation()
                drawingAnim.playAnimation()
                drawingAnimation()
            } else {
                skipAnimation(drawingAnim)
            }
        }

        val layout = findViewById<ConstraintLayout>(R.id.result_view)
        layout.background = getDrawable(R.drawable.img_others_background)

        PublicMethods.showAd(this)

        val scrollView = findViewById<View>(R.id.drawing_scroll) as ScrollView
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }

        val showResultButton = findViewById<Button>(R.id.show_result)
        showResultButton.text = getString(R.string.retry)
        showResultButton.setOnClickListener { onRetry() }
        findViewById<Button>(R.id.go_home).setOnClickListener { onGoHome() }
    }

    private fun setUpAnimators(boxView: LottieAnimationView, ticketView: View) {
        shakeAnimator = AnimatorInflater.loadAnimator(this, R.animator.box_shake_anim)
        shakeAnimator.setTarget(boxView)
        ticketAnimator = AnimatorInflater.loadAnimator(this, R.animator.drawing_anim)
        ticketAnimator.setTarget(ticketView)
        ticketAnimator.doOnEnd { prepared = false }
    }

    private fun shakeAnimation(pleaseTap:LottieAnimationView) {
        //振動
        shakeAnimator.doOnEnd { shakeAnimator.start() }
        shakeAnimator.start()
        //3秒待って進まなかったらタップを促す
        Handler().postDelayed(Runnable {
            pleaseTap.visibility = View.VISIBLE
            pleaseTap.playAnimation()
        }, 3000)
    }

    private fun drawingAnimation() {
        //初期状態に戻す
        shakeAnimator.start()
        shakeAnimator.pause()
        ticketAnimator.start()
    }

    private fun skipAnimation(drawingAnim: LottieAnimationView) {
        drawingAnim.progress = 1.0f
        ticketAnimator.end()
    }

    //以下、ボタンの処理
    private fun onRetry() {
        v = 0
        val title = getString(R.string.retry_title)
        val message = getString(R.string.re_order_description) + getString(R.string.run_confirmation)
        DialogWarehouse(supportFragmentManager).decisionDialog(title, message, this::retry)
    }

    private fun retry() {
        val scrollView = findViewById<View>(R.id.drawing_scroll) as ScrollView
        scrollView.scrollTo(0, 0)
        Toast.makeText(applicationContext, getText(R.string.retry_finished), Toast.LENGTH_SHORT).show()
    }

    private fun onGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PublicMethods.initialize()
        startActivity(intent)
    }

}

