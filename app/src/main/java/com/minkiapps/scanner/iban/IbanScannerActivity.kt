package com.minkiapps.scanner.iban

import android.os.Bundle
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.scan.BaseScannerActivity

class IbanScannerActivity : BaseScannerActivity<String>() {

    override fun getScannerType(): ScannerOverlayImpl.Type {
        return ScannerOverlayImpl.Type.IBAN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (analyser as IBANAnalyser).textRecognizedLiveData().observe(this) {
            scannerOverlay().drawBlueRect = it
        }
    }

    override fun initImageAnalyser(mlService: BaseAnalyser.MLService): BaseAnalyser<String> {
        return IBANAnalyser(scannerOverlay(), mlService)
    }
}