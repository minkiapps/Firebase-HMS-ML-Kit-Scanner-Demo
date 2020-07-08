package com.minkiapps.scanner.iban

import android.os.Bundle
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.scan.BaseScannerActivity

class IbanScannerActivity : BaseScannerActivity<String>() {

    private val ibanAnalyser by lazy {
        IBANAnalyser(scannerOverlay())
    }

    override fun getImageAnalyser(): BaseAnalyser<String> = ibanAnalyser

    override fun getScannerType(): ScannerOverlayImpl.Type {
        return ScannerOverlayImpl.Type.IBAN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ibanAnalyser.textRecognizedLiveData().observe(this) {
            scannerOverlay().drawBlueRect = it
        }
    }
}