package com.pandatone.kumiwake.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MembersViewModel2 : ViewModel() {

    private val _members = MutableLiveData<String>().apply {
        value = "This is settings Fragment"
    }
    //val text: LiveData<String> = _text
}