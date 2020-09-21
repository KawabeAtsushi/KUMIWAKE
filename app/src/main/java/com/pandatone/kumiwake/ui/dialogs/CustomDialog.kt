package com.pandatone.kumiwake.ui.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.pandatone.kumiwake.R


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class CustomDialog(private var mTitle: String, private var mMessage: CharSequence, private val linkChar: CharSequence = "") : DialogFragment() {

    //onClickリスナ(Positive)
    var mPositiveBtnListener: View.OnClickListener? = null
    var positiveTxt = ""
    var negativeTxt = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val dialog = activity?.let { AppCompatDialog(it) }
        // タイトル非表示
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // タイトル設定
        dialog.findViewById<TextView>(R.id.dialog_title)!!.text = mTitle
        // メッセージ設定
        dialog.findViewById<TextView>(R.id.dialog_message)!!.text = mMessage
        // リンク設定
        if (linkChar != "") {
            val linkTextView = dialog.findViewById<TextView>(R.id.dialog_link)
            linkTextView!!.visibility = View.VISIBLE
            linkTextView.text = linkChar
            val mMethod = LinkMovementMethod.getInstance()
            linkTextView.movementMethod = mMethod
        }
        // ボタンの設定
        val positiveButton = dialog.findViewById<MaterialButton>(R.id.positive_button)!!
        val negativeButton = dialog.findViewById<MaterialButton>(R.id.negative_button)!!
        if (positiveTxt != "") {
            positiveButton.text = positiveTxt
        }
        if (negativeTxt != "") {
            negativeButton.text = negativeTxt
        }
        // OK ボタンのリスナ
        if (mPositiveBtnListener == null) {
            negativeButton.visibility = View.GONE
            positiveButton.setOnClickListener { dismiss() }
        } else {
            positiveButton.setOnClickListener(mPositiveBtnListener)
            // いいえボタンのリスナ
            negativeButton.setOnClickListener { dismiss() }
        }

        return dialog
    }
}
