package com.pandatone.kumiwake

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.ui.dialogs.DialogWarehouse
import kotlin.math.abs


object PublicMethods {

    //初期状態を設定
    fun initialize(){
        StatusHolder.nowTheme = R.style.AppTheme
        StatusHolder.normalMode = true
        StatusHolder.sekigime = false
        StatusHolder.notDuplicate = false
    }

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
            window.statusBarColor = getColor(this, colorId)
        }
        when (colorId) {
            Theme.Kumiwake.primaryColor -> StatusHolder.nowTheme = R.style.AppTheme
            Theme.Sekigime.primaryColor -> StatusHolder.nowTheme = R.style.SekigimeTheme
            Theme.Others.primaryColor -> StatusHolder.nowTheme = R.style.OthersTheme
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

    //webサイトへ
    fun toWebSite(context: Context, fragmentManager: FragmentManager) {
        DialogWarehouse(fragmentManager).decisionDialog(context.getString(R.string.move_kumiwake_site), context.getString(R.string.move_kumiwake_site_discription)) {
            val uri = Uri.parse(context.getString(R.string.url_homepage))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }

    //Link設定
    fun getLinkChar(url: String, text: String): CharSequence {
        val siteCharHtml = "<a href=$url>$text</a>"
        return HtmlCompat.fromHtml(siteCharHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    //Viewにマージンを設定
    fun setMargin(c: Context, leftDp: Int, topDp: Int, rightDp: Int, bottomDp: Int): LinearLayout.LayoutParams {
        val scale = c.resources.displayMetrics.density //画面のdensityを指定。
        val left = (leftDp * scale + 0.5f).toInt()
        val top = (topDp * scale + 0.5f).toInt()
        val right = (rightDp * scale + 0.5f).toInt()
        val bottom = (bottomDp * scale + 0.5f).toInt()
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(left, top, right, bottom)
        return layoutParams
    }
}

class MyGestureListener(private val imm: InputMethodManager, val et: EditText) : GestureDetector.SimpleOnGestureListener() {

    //ダブルタップイベント
    override fun onDoubleTap(event: MotionEvent): Boolean {
        et.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
        return true
    }
}










