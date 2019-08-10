package com.pandatone.kumiwake.sekigime

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2016/11/11.
 */

class CustomDialogSekigime : DialogFragment() {
    private var mTitle = ""
    private var mPosition = 1
    private var iv: ImageView? = null
    private var fmDeploy: CheckBox? = null

    //onClickリスナ
    private val mOnClickLisner = View.OnClickListener { dismiss() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(this.context)
        // タイトル非表示
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog.setContentView(R.layout.custom_dialog_layout_sekigime)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        fmDeploy = dialog.findViewById<View>(R.id.even_fm_deploy_check) as CheckBox
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = mTitle
        // 画像設定
        iv = dialog.findViewById<View>(R.id.table_img) as ImageView
        setImage()

        //カスタム表示
        val custom = dialog.findViewById<View>(R.id.be_custom) as TextView
        val positive_bt = dialog.findViewById<View>(R.id.positive_button) as Button
        if (mPosition == 1) {
            custom.visibility = View.VISIBLE
            positive_bt.text = getText(R.string.move_custom)
        }
        // OK ボタンのリスナ
        setOnPositiveClickListener()
        positive_bt.setOnClickListener(mPositiveBtnListener)

        // いいえボタンのリスナ
        dialog.findViewById<View>(R.id.negative_button).setOnClickListener(mOnClickLisner)
        return dialog
    }

    //タイトル
    fun setTitle(title: String) {
        mTitle = title
    }

    //メッセージ設定
    fun setPosition(position: Int) {
        mPosition = position
    }

    private fun setImage() {
        val width: Int
        val height: Int
        var widthDp = 0
        var heightDp = 0
        when (mPosition) {
            1 -> {
                iv!!.setImageResource(R.drawable.square_table)
                widthDp = 180
                heightDp = 250
            }
            2 -> {
                iv!!.setImageResource(R.drawable.parallel_table)
                widthDp = 180
                heightDp = 250
            }
            3 -> {
                iv!!.setImageResource(R.drawable.circle_table)
                widthDp = 200
                heightDp = 200
            }
            4 -> {
                iv!!.setImageResource(R.drawable.counter_table)
                widthDp = 80
                heightDp = 210
            }
        }
        val scale = resources.displayMetrics.density //画面のdensityを指定。
        width = (widthDp * scale + 0.5f).toInt()
        height = (heightDp * scale + 0.5f).toInt()
        val layoutParams = LinearLayout.LayoutParams(width, height)
        iv!!.layoutParams = layoutParams
    }

    private fun setOnPositiveClickListener() {
        mPositiveBtnListener = View.OnClickListener {
            SekigimeResult.fmDeploy = fmDeploy!!.isChecked
            if (mPosition == 1) {
                val intent = Intent(activity, SquareTableCustom::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(activity, SekigimeResult::class.java)
                startActivity(intent)
            }
            dismiss()
        }
    }

    companion object {
        var mPositiveBtnListener: View.OnClickListener? = null
    }
}
