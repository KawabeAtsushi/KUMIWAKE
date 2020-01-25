package com.pandatone.kumiwake.ui

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FMDialogViewModel : ViewModel() {

    var path = MutableLiveData<String>()
    var showDialog = MutableLiveData<Boolean>()

    init {
        path.value = ""
        showDialog.value = true
    }

}