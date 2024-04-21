package com.pandatone.kumiwake.extension

import android.content.Intent
import android.os.Build
import java.io.Serializable

inline fun <reified T : Serializable?> Intent.getSerializable(key: String): T? {
    @Suppress("DEPRECATION")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, T::class.java)
    else
        this.getSerializableExtra(key) as? T
}