package com.minkiapps.scanner.id

import android.os.Bundle
import androidx.lifecycle.Observer
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.scan.BaseScannerActivity
import kotlinx.android.synthetic.main.activity_scanner.*

class IDScannerActivity : BaseScannerActivity<IDResult>() {

    private val idAnalyser by lazy {
        IDAnalyser(olActScanner)
    }

    override fun getImageAnalyser(): BaseAnalyser<IDResult> {
        return idAnalyser
    }

    override fun getScannerType(): ScannerOverlayImpl.Type {
        return ScannerOverlayImpl.Type.ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        idAnalyser.mrzBlockLiveData.observe(this, Observer {
            olActScanner.drawGraphicBlocks(listOf(it))
        })
    }

}