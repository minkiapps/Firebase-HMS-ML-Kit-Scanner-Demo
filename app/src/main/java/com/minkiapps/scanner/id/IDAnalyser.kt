package com.minkiapps.scanner.id

import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.id.parser.MrzParser
import com.minkiapps.scanner.id.parser.types.MrzDate
import com.minkiapps.scanner.id.parser.types.MrzSex
import com.minkiapps.scanner.id.processor.MrzTextPreProcessor
import com.minkiapps.scanner.id.processor.MrzTextPreProcessor.MIN_POSSIBLE_CHAR_LENGTH_PER_LINE
import com.minkiapps.scanner.overlay.ScannerOverlay
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import org.threeten.bp.LocalDate
import timber.log.Timber
import java.util.*

internal class IDAnalyser(scannerOverlay: ScannerOverlay) : BaseAnalyser<IDResult>(scannerOverlay) {

    private val textRecognizer: TextRecognizer = TextRecognition.getClient()

    private var addressText : String? = null

    private val mrzBlockMutableLiveData : MutableLiveData<ScannerOverlayImpl.GraphicBlock> = MutableLiveData()
    val mrzBlockLiveData : LiveData<ScannerOverlayImpl.GraphicBlock> = mrzBlockMutableLiveData

    override fun onInputImagePrepared(inputImage: InputImage) {
        val task = Tasks.await(textRecognizer.process(inputImage))

        var detectedPossibleMrzBlock = false
        task.textBlocks.forEach { block ->
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

    private fun detectPossibleAddressText(block: Text.TextBlock) {
        val lines = block.lines

        if(lines.size > 1
                && lines[0].text.contains("Anschrift")
                || lines[0].text.contains("Adresse")
                || lines[0].text.contains("Address")) {
            val addressText = block.lines.subList(1, lines.size).joinToString(separator = ", ") { it.text }
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

    companion object {
        private const val PARSER_FILLER_REPLACEMENT = ", " //mrz parser replaces all "<<" with ", " within recognized name fields
    }

    override fun close() {
        textRecognizer.close()
    }
}