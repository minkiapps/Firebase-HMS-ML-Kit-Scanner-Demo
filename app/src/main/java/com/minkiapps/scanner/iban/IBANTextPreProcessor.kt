package com.minkiapps.scanner.iban

import com.minkiapps.scanner.util.replaceWithinRange
import java.util.*

object IBANTextPreProcessor {

    fun preProcess(raw : String) : String {
        return raw
            .toUpperCase(Locale.ENGLISH)
            .replace(" ", "")
            .replaceWithinRange(2, 4,"O", "0")
    }
}