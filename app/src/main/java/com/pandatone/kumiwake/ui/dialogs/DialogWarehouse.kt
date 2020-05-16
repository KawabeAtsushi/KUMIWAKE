package com.pandatone.kumiwake.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.others.drawing.TicketDefine

class DialogWarehouse(private var fragmentManager: FragmentManager?) {

    fun confirmationDialog(title: String, message: CharSequence, showLink: CharSequence = "") {
        val customDialog = CustomDialog(title, message, showLink)
        customDialog.mPositiveBtnListener = null
        val ft = this.fragmentManager!!.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun decisionDialog(title: String, message: CharSequence, function: () -> Unit) {
        val customDialog = CustomDialog(title, message)
        customDialog.mPositiveBtnListener = View.OnClickListener {
            function()
            customDialog.dismiss()
        }
        val ft = fragmentManager!!.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun fmDialog(title: String, message: CharSequence, backup: Boolean) {
        val customDialog = FileManagerDialog(title, message, backup)
        val ft = fragmentManager!!.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun colorPickerDialog(context: Context, position: Int, icon: ImageView) {
        var initialColor = Color.WHITE
        TicketDefine.ticketColors[position].let {
            //変更済みの場合はそれを初期色に
            if (it != Color.parseColor("#6b6b6b")) {
                initialColor = it
            }
        }
        ColorPickerDialogBuilder
                .with(context)
                .setTitle(context.getString(R.string.choose_color))
                .lightnessSliderOnly()
                .initialColor(initialColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("OK") { _, selectedColor, _ ->
                    TicketDefine.ticketColors[position] = selectedColor
                    icon.setColorFilter(selectedColor)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
                .build()
                .show()
    }

}