package com.minkiapps.scanner.util

import android.util.SparseArray
import androidx.core.util.forEach

fun <T> SparseArray<T>.values(): List<T> {
    val list = ArrayList<T>()
    forEach { _, value ->
        list.add(value)
    }
    return list.toList()
}