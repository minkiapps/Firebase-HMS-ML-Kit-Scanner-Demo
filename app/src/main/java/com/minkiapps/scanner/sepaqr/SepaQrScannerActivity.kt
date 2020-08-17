package com.minkiapps.scanner.sepaqr

import android.os.Bundle
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.scan.BaseScannerActivity
import kotlinx.android.synthetic.main.activity_scanner.*

class SepaQrScannerActivity : BaseScannerActivity<SepaData>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (analyser as SepaQRAnalyser).qrRecognizedLiveData().observe(this) {
            olActScanner.drawBlueRect = it
        }
    }

    override fun getScannerType(): ScannerOverlayImpl.Type {
        return ScannerOverlayImpl.Type.SEPAQR
    }

    override fun initImageAnalyser(mlService: BaseAnalyser.MLService): BaseAnalyser<SepaData> {
        return SepaQRAnalyser(scannerOverlay(), mlService)
    }
}