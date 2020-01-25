package com.pandatone.kumiwake.ui

import android.view.View
import androidx.fragment.app.FragmentManager

class DialogWarehouse(private var fragmentManager: FragmentManager) {

    fun confirmationDialog(title: String, message: CharSequence) {
        val customDialog = CustomDialog(title, message)
        customDialog.mPositiveBtnListener = null
        val ft = this.fragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun decisionDialog(title: String, message: CharSequence, function: () -> Unit) {
        val customDialog = CustomDialog(title, message)
        customDialog.mPositiveBtnListener = View.OnClickListener {
            function()
            customDialog.dismiss()
        }
        val ft = fragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun importDialog(title: String, message: CharSequence, function: () -> Unit) {
        val customDialog = FileManagerDialog(title, message)
        customDialog.mPositiveBtnListener = View.OnClickListener {
            function()
            customDialog.dismiss()
        }
        val ft = fragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

}