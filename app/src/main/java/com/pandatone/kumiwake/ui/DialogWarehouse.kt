package com.pandatone.kumiwake.ui

import androidx.fragment.app.FragmentManager

class DialogWarehouse(private var fragmentManager: FragmentManager) {

    fun confirmationDialog(title: String, message: CharSequence) {
        val customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        val ft = this.fragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }

    fun decisionDialog(title: String, message: CharSequence, code: Int) {
        val customDialog = CustomDialog()
        customDialog.setTitle(title)
        customDialog.setMessage(message)
        customDialog.setOnPositiveClickListener(code)
        val ft = this.fragmentManager.beginTransaction()
        ft.add(customDialog, null)
        ft.commitAllowingStateLoss()
    }
}