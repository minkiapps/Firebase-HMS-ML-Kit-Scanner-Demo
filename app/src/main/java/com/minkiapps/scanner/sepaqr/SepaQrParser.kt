package com.minkiapps.scanner.sepaqr

object SepaQrParser {

    fun parse(raw : String) : SepaData {
        val delimiter = when {
            raw.startsWith("BCD\n00") -> "\n"
            raw.startsWith("BCD\r\n00") -> "\r\n"
            else -> throw ParseException("")
        }

        val lines = raw.split(delimiter)
        if(lines.size < 10) {
            throw ParseException("not enough sufficient lines")
        }

        val bic = lines.getOrNull(4) ?: ""
        val recipient = lines.getOrNull(5) ?: ""
        val iban = lines.getOrNull(6) ?: ""
        val amount = try {
            lines.getOrNull(7)?.replace("EUR","")?.toDoubleOrNull() ?: 0.0
        } catch (it: Throwable) {
            0.0
        }
        val usage = lines.getOrNull(9) ?: ""
        return SepaData(recipient, iban, bic, usage, amount)
    }

    class ParseException(message : String) : IllegalStateException(message)
}

data class SepaData(val recipient : String, val iban : String, val bic : String, val usage : String, val amount : Double)