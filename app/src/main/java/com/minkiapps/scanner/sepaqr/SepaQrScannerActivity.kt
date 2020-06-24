package com.minkiapps.scanner.sepaqr

import android.os.Bundle
import androidx.lifecycle.Observer
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.scan.BaseScannerActivity
import kotlinx.android.synthetic.main.activity_iban_scanner.*

class SepaQrScannerActivity : BaseScannerActivity<SepaData>() {

    private val analyser by lazy {
        SepaQRAnalyser(olActScanner)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyser.qrRecognizedLiveData().observe(this, Observer {
            olActScanner.drawBlueRect = it
        })
    }

    override fun getImageAnalyser(): BaseAnalyser<SepaData> {
        return analyser
    }

    override fun getScannerType(): ScannerOverlayImpl.Type {
        return ScannerOverlayImpl.Type.SEPAQR
    }
}