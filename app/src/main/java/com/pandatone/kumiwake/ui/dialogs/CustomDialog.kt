package com.pandatone.kumiwake.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R


/**
 * Created by atsushi_2 on 2016/11/11.
 */

class CustomDialog(private var mTitle: String, private var mMessage: CharSequence,private val linkChar: CharSequence = "") : DialogFragment() {

    //onClickリスナ(Positive)
    var mPositiveBtnListener: View.OnClickListener? = null

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
        if(linkChar != "") {
            val linkTextView = dialog.findViewById<TextView>(R.id.dialog_link)
            linkTextView!!.visibility = View.VISIBLE
            linkTextView.text = linkChar
            val mMethod = LinkMovementMethod.getInstance();
            linkTextView.movementMethod = mMethod;
        }
        // OK ボタンのリスナ
        if (mPositiveBtnListener == null) {
            dialog.findViewById<View>(R.id.negative_button)!!.visibility = View.GONE
            dialog.findViewById<View>(R.id.positive_button)!!.setOnClickListener { dismiss() }
        } else {
            dialog.findViewById<View>(R.id.positive_button)!!.setOnClickListener(mPositiveBtnListener)
            // いいえボタンのリスナ
            dialog.findViewById<View>(R.id.negative_button)!!.setOnClickListener { dismiss() }
        }

        return dialog
    }
}
