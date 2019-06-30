package com.pandatone.kumiwake

import android.app.Application
import android.content.Context


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {

        var context: Context? = null
            private set
    }
}