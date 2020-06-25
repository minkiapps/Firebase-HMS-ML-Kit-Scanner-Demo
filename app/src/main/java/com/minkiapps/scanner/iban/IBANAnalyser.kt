package com.minkiapps.scanner.iban

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlay
import org.iban4j.IbanUtil
import timber.log.Timber
import java.util.regex.Pattern

class IBANAnalyser(scannerOverlay: ScannerOverlay) : BaseAnalyser<String>(scannerOverlay) {

    private val ibanRegex = Pattern.compile("^[A-Z]{2}[0-9]{2}[0-9A-Za-z]{11,28}\$")
    private val textRecognizer: TextRecognizer = TextRecognition.getClient()

    private val textRecognizedData = MutableLiveData<Boolean>()
    fun textRecognizedLiveData(): LiveData<Boolean> = textRecognizedData

    override fun onInputImagePrepared(inputImage: InputImage) {
        val result = Tasks.await(textRecognizer.process(inputImage))
        if(result.text.isNotBlank()) {
            Timber.d("Scanned raw text: ${result.text}")
        }
        val possibleLines = result.textBlocks
            .flatMap { it.lines
                .map{ l -> l.text}
                .filter { txt -> txt.length > MIN_IBAN_CHAR_LENGTH }
                .map { txt -> IBANTextPreProcessor.preProcess(txt) }
                .filter { txt -> ibanRegex.matcher(txt).matches() }
            }

        textRecognizedData.postValue(possibleLines.isNotEmpty())

        if(possibleLines.isNotEmpty()) {
            Timber.d("Possible IBAN lines: ${possibleLines.joinToString(separator = ", ")}")
        } else {
            postResult("")
        }

        possibleLines.forEach { text ->
                Timber.d("Possible IBAN: $text")
                try {
                    IbanUtil.validate(text)
                    postResult(text)
                    return
                } catch (e: Exception) {
                    Timber.e(e, "Invalid IBAN")
                }
            }
    }

    companion object {
        private const val MIN_IBAN_CHAR_LENGTH = 15
    }

    override fun close() {
        textRecognizer.close()
    }
}