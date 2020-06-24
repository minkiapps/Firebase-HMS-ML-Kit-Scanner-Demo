package com.minkiapps.scanner.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import java.io.Serializable

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
        getInt(index, -1).let { if (it >= 0) enumValues<T>()[it] else default }

fun <T : Serializable> Activity.extraSerializableOrThrow(key : String) = lazy {
        intent?.getSerializableExtra(key) as T? ?: throw RuntimeException("no extra found for key $key in intent")
}

fun Context.isPortrait() : Boolean {
   val orientation = this.resources.configuration.orientation
   return orientation == Configuration.ORIENTATION_PORTRAIT
}
