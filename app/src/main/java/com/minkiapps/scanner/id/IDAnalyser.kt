package com.minkiapps.scanner.id

import android.graphics.Bitmap
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.innovatrics.mrz.MrzParser
import com.innovatrics.mrz.types.MrzDate
import com.innovatrics.mrz.types.MrzSex
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.analyser.BlockWrapper
import com.minkiapps.scanner.id.processor.MrzTextPreProcessor
import com.minkiapps.scanner.id.processor.MrzTextPreProcessor.MIN_POSSIBLE_CHAR_LENGTH_PER_LINE
import com.minkiapps.scanner.overlay.ScannerOverlay
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import timber.log.Timber
import java.time.LocalDate
import java.util.*

class IDAnalyser(scannerOverlay: ScannerOverlay,
                          mlService: MLService
) : BaseAnalyser<IDResult>(scannerOverlay, mlService) {

    private val gmsTextRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient()
    }

    private val hmsTextRecognizer : MLTextAnalyzer by lazy {
        MLAnalyzerFactory.getInstance().localTextAnalyzer
    }

    private var addressText : String? = null

    private val mrzBlockMutableLiveData : MutableLiveData<ScannerOverlayImpl.GraphicBlock> = MutableLiveData()
    val mrzBlockLiveData : LiveData<ScannerOverlayImpl.GraphicBlock> = mrzBlockMutableLiveData

    override fun onBitmapPrepared(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val textBlocks = detectTextBlocks(bitmap)

        var detectedPossibleMrzBlock = false
        textBlocks.forEach { block ->
            detectPossibleAddressText(block)

            if(isPossibleMrzBlock(block.text)) {
                detectedPossibleMrzBlock = true
                mrzBlockMutableLiveData.postValue(ScannerOverlayImpl.GraphicBlock(block, Size(inputImage.width, inputImage.height)))

                MrzTextPreProcessor.process(block.text)?.let { processed ->
                    try {
                        val parsed = parseMrz(processed)
                        if(parsed) {
                            return@forEach
                        }
                    } catch (e : Exception) {
                        Timber.e(e, "Parsing MRZ failed")
                    }
                }
            }
        }
        if(!detectedPossibleMrzBlock)
            postResult(null)
    }

    private fun detectTextBlocks(bitmap: Bitmap) : List<BlockWrapper> {
        return when(mlService) {
            MLService.GMS -> {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val result = Tasks.await(gmsTextRecognizer.process(inputImage))
                if(result.text.isNotBlank()) {
                    Timber.d("GMS scanned raw text: ${result.text}")
                }
                result.textBlocks.map { BlockWrapper(gmsTextBLock = it) }
            }
            MLService.HMS -> {
                val result = com.huawei.hmf.tasks.Tasks.await(hmsTextRecognizer.asyncAnalyseFrame(
                    MLFrame.fromBitmap(bitmap)))
                if(result.stringValue.isNotBlank()) {
                    Timber.d("HMS scanned raw text: ${result.stringValue}")
                }
                result.blocks.map { BlockWrapper(hmsTextBLock = it) }
            }
        }
    }

    private fun detectPossibleAddressText(block: BlockWrapper) {
        val lines = block.lines

        if(lines.size > 1
                && lines[0].contains("Anschrift")
                || lines[0].contains("Adresse")
                || lines[0].contains("Address")) {
            val addressText = block.lines.subList(1, lines.size).joinToString(separator = ", ") { it }
            if(addressText.length > this.addressText?.length ?: 0) {
                this.addressText = addressText
            }
        }
    }

    private fun isPossibleMrzBlock(txt : String) : Boolean {
        return (txt.filter { c -> c == '\n' }.count() >= 1
                && txt.filter { c -> c == '<' }.count() > 10
                && txt.split("\n").filter { it.length >= MIN_POSSIBLE_CHAR_LENGTH_PER_LINE }.size > 1)
    }

    private fun parseMrz(processed : String) : Boolean {
        val record = MrzParser.parse(processed)
        val documentNumber = record.documentNumber
        val givenNames = record.givenNames
        val sureName = record.surname
        val birthDate = record.dateOfBirth
        val nationality = record.nationality
        val gender = record.sex
        val issuingCountry = record.issuingCountry
        val expirationDate = record.expirationDate

        val nameNeedCorrection = processed.last().isLetter()
                || givenNames.contains(PARSER_FILLER_REPLACEMENT)
                || sureName.contains(PARSER_FILLER_REPLACEMENT)

        if(record.validDocumentNumber && !givenNames.isNullOrBlank()
            && !sureName.isNullOrBlank()
            && birthDate?.isDateValid == true
            && expirationDate.isDateValid
            && !nationality.isNullOrBlank()) {

            val idResult = IDResult(idNumber = documentNumber,
                issuingCountry = issuingCountry,
                givenNames = givenNames.replace(PARSER_FILLER_REPLACEMENT, ""),
                sureName = sureName.replace(PARSER_FILLER_REPLACEMENT, ""),
                birthDate = addCenturyToBirthDate(birthDate),
                expirationDate = addCenturyToExpirationDate(expirationDate),
                nationality = nationality,
                gender = if(gender != null && gender != MrzSex.Unspecified) gender.name.toUpperCase(Locale.ENGLISH) else null,
                nameNeedCorrection = nameNeedCorrection, scannedAddress = addressText)

            postResult(idResult)
            return true
        }

        return false
    }

    private fun addCenturyToBirthDate(date : MrzDate) : LocalDate {
        //first try with 1900
        val twentiethCenturyDate = LocalDate.of(1900 + date.year, date.month, date.day)
        return if(LocalDate.now().year - twentiethCenturyDate.year > 100) {
            LocalDate.of(2000 + date.year, date.month, date.day)
        } else twentiethCenturyDate
    }

    private fun addCenturyToExpirationDate(date : MrzDate) : LocalDate {
        //first try with 2000
        val twentiethCenturyDate = LocalDate.of(2000 + date.year, date.month, date.day)
        return if(twentiethCenturyDate.year - LocalDate.now().year > 100) {
            LocalDate.of(1900 + date.year, date.month, date.day)
        } else twentiethCenturyDate
    }

    override fun close() {
        when(mlService) {
            MLService.GMS -> gmsTextRecognizer.close()
            MLService.HMS -> hmsTextRecognizer.close()
        }
    }

    companion object {
        private const val PARSER_FILLER_REPLACEMENT = ", " //mrz parser replaces all "<<" with ", " within recognized name fields
    }
}