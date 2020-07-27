package com.minkiapps.scanner.iban

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlay
import org.iban4j.IbanUtil
import timber.log.Timber
import java.util.regex.Pattern

class IBANAnalyser(scannerOverlay: ScannerOverlay,
                   mlService: MLService
) : BaseAnalyser<String>(scannerOverlay, mlService) {

    private val ibanRegex = Pattern.compile("^[A-Z]{2}[0-9]{2}[0-9A-Za-z]{11,28}\$")

    private val gmsTextRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient()
    }

    private val hmsTextRecognizer : MLTextAnalyzer by lazy {
        val setting = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
            .setLanguage("en")
            .create()
        MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
    }

    private val textRecognizedData = MutableLiveData<Boolean>()
    fun textRecognizedLiveData(): LiveData<Boolean> = textRecognizedData

    override fun onBitmapPrepared(bitmap: Bitmap) {
        val lines = detectLines(bitmap)
        val possibleLines = lines
                .filter { txt -> txt.length > MIN_IBAN_CHAR_LENGTH }
                .map { txt -> IBANTextPreProcessor.preProcess(txt) }
                .filter { txt -> ibanRegex.matcher(txt).matches() }

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

    private fun detectLines(bitmap: Bitmap) : List<String> {
        return when(mlService) {
            MLService.GMS -> {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val result = Tasks.await(gmsTextRecognizer.process(inputImage))
                if(result.text.isNotBlank()) {
                    Timber.d("GMS scanned raw text: ${result.text}")
                }
                result.textBlocks.flatMap { it.lines.map { l -> l.text} }
            }
            MLService.HMS -> {
                val result = com.huawei.hmf.tasks.Tasks.await(hmsTextRecognizer.asyncAnalyseFrame(MLFrame.fromBitmap(bitmap)))
                if(result.stringValue.isNotBlank()) {
                    Timber.d("HMS scanned raw text: ${result.stringValue}")
                }
                result.blocks.flatMap { it.contents.map { l -> l.stringValue } }
            }
        }
    }

    override fun close() {
        when(mlService) {
            MLService.GMS -> gmsTextRecognizer.close()
            MLService.HMS -> hmsTextRecognizer.close()
        }
    }

    companion object {
        private const val MIN_IBAN_CHAR_LENGTH = 15
    }
}