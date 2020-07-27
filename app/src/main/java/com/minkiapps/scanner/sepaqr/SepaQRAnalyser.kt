package com.minkiapps.scanner.sepaqr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlay
import timber.log.Timber

class SepaQRAnalyser(scannerOverlay: ScannerOverlay,
                     mlService: MLService
) : BaseAnalyser<SepaData>(scannerOverlay, mlService) {

    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    private val qrRecognizedData = MutableLiveData<Boolean>()
    fun qrRecognizedLiveData(): LiveData<Boolean> = qrRecognizedData

    override fun onBitmapPrepared(bitmap: Bitmap) {
        val inputImage : InputImage = InputImage.fromBitmap(bitmap, 0)
        val result = Tasks.await(barcodeScanner.process(inputImage))

        qrRecognizedData.postValue(result.isNotEmpty())
        var qrDetected = false
        result.forEach {  barcode ->
            //Timber.d("Barcode type ${barcode.format} Raw value: ${barcode.rawValue}")

            barcode.rawValue?.let {
                try {
                    val sepaData = SepaQrParser.parse(it)
                    postResult(sepaData)
                    qrDetected = true
                } catch (e : SepaQrParser.ParseException) {
                    Timber.w(e, "Failed to parse sepa qr")
                }
            }
        }

        if(!qrDetected) {
            postResult(null)
        }
    }

    override fun close() {
        barcodeScanner.close()
    }
}