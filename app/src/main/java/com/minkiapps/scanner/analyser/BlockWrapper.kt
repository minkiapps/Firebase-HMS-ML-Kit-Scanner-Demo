package com.minkiapps.scanner.analyser

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import com.huawei.hms.mlsdk.text.MLText

class BlockWrapper(val gmsTextBLock : Text.TextBlock? = null, val hmsTextBLock : MLText.Block? = null) {

        val boundingBox : Rect = gmsTextBLock?.boundingBox ?: hmsTextBLock!!.border
        val text : String = gmsTextBLock?.text ?: hmsTextBLock!!.stringValue
        val lines : List<String> = gmsTextBLock?.lines?.map { it.text } ?: hmsTextBLock!!.contents!!.map { it.stringValue }
    }