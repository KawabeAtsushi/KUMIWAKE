package com.pandatone.kumiwake.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.pandatone.kumiwake.R

/**
 * Created by atsushi_2 on 2016/11/11.
 */

class CustomDialog(private var mTitle: String, private var mMessage: CharSequence) : DialogFragment() {

    //onClickリスナ(Positive)
    var mPositiveBtnListener: View.OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let { Dialog(it) }
        // タイトル非表示
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        // フルスクリーン
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // タイトル設定
        (dialog.findViewById<View>(R.id.dialog_title) as TextView).text = mTitle
        // メッセージ設定
        (dialog.findViewById<View>(R.id.dialog_message) as TextView).text = mMessage
        // OK ボタンのリスナ
        if (mPositiveBtnListener == null) {
            dialog.findViewById<View>(R.id.negative_button).visibility = View.GONE
            dialog.findViewById<View>(R.id.positive_button).setOnClickListener { dismiss() }
        } else {
            dialog.findViewById<View>(R.id.positive_button).setOnClickListener(mPositiveBtnListener)
            // いいえボタンのリスナ
            dialog.findViewById<View>(R.id.negative_button).setOnClickListener{ dismiss() }
        }

        return dialog
    }
}
