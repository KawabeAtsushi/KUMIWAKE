package com.pandatone.kumiwake

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.member.function.Group
import kotlin.math.abs


object PublicMethods {

    //広告の表示
    fun showAd(activity: Activity) {
        val mAdView = activity.findViewById<View>(R.id.adView) as AdView
        if (StatusHolder.adDeleated) {
            mAdView.visibility = View.GONE
        } else {
            MobileAds.initialize(activity, activity.getString(R.string.adApp_id))
            val adRequest = AdRequest.Builder()
                    .addTestDevice(activity.getString(R.string.device_id)).build()
            mAdView.loadAd(adRequest)
        }
    }

    //ステータスバーの色変更
    fun setStatusBarColor(activity: Activity, @ColorRes colorId: Int) {
        activity.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = PublicMethods.getColor(this, colorId)
        }
        when (colorId) {
            Theme.Kumiwake.primaryColor -> StatusHolder.nowTheme = R.style.AppTheme
            Theme.Sekigime.primaryColor -> StatusHolder.nowTheme = R.style.SekigimeTheme
            Theme.Member.primaryColor -> StatusHolder.nowTheme = R.style.MemberTheme
        }
    }

    //Kumiwake 初期グループ生成（人数均等）
    fun initialGroupArray(context: Context, groupNo: Int, memberNo: Int): ArrayList<Group> {
        val groupArray = ArrayList<Group>()
        val eachMemberNo = memberNo / groupNo
        val remain = memberNo % groupNo
        for (i in 0 until remain) {
            groupArray.add(Group(i, context.getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo + 1))
        }
        for (i in remain until groupNo) {
            groupArray.add(Group(i, context.getText(R.string.group).toString() + " " + (i + 1).toString(), "", eachMemberNo))
        }
        return groupArray
    }

    // view(TextView or Button)に合わせて文字が収まるようにリサイズ
    fun View.resizeText(setText: String = "") {
        val MIN_TEXT_SIZE = 10f //最小サイズを決める
        var text = setText
        if (text == "") {
            if (this is TextView) {
                text = this.text.toString()
            } else if (this is Button) {
                text = this.text.toString()
            }
        }
        val padding: Int = this.paddingLeft
        val paint = Paint()
        val viewWidth: Int = this.width - padding * 2 //Viewのコンテンツ領域を取得
        val viewHeight: Int = this.height - padding * 2 //パディングは上下左右同じとして
        var textSize = 200f //テキストサイズの初期値を適当に決める
        paint.textSize = textSize //テキストサイズをセット
        var fm: Paint.FontMetrics = paint.fontMetrics
        var textHeight: Float = abs(fm.top) + abs(fm.descent) //テキストの高さを取得
        var textWidth: Float = paint.measureText(text) //テキストの幅を取得
        while (viewWidth < textWidth || viewHeight < textHeight) { //ボタンに収まるまでループ
            if (MIN_TEXT_SIZE >= textSize) { //最小サイズを下回ったら最小サイズに設定
                textSize = MIN_TEXT_SIZE
                break
            }
            textSize -= 8f //テキストサイズをデクリメント（間隔は適当に）
            paint.textSize = textSize
            fm = paint.fontMetrics
            textHeight = abs(fm.top) + abs(fm.descent)
            textWidth = paint.measureText(text) //テキストの縦横サイズを再取得
        }
        if (this is TextView) {
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize) //収まるサイズに設定
        } else if (this is Button) {
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize) //収まるサイズに設定
        }
    }

    //getColor
    fun getColor(context: Context, @ColorRes colorId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.getColor(context, colorId)
        } else {
            context.resources.getColor(colorId)
        }
    }
}










