package com.minkiapps.scanner

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.iban.IbanScannerActivity
import com.minkiapps.scanner.id.IDScannerActivity
import com.minkiapps.scanner.scan.BaseScannerActivity
import com.minkiapps.scanner.sepaqr.SepaQrScannerActivity
import com.minkiapps.scanner.util.isGmsAvailable
import com.minkiapps.scanner.util.isHmsAvailable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnActMainIbanScanner.setOnClickListener {
            startActivity(BaseScannerActivity.createIntent<IbanScannerActivity>(this, getSelectedMLService()))
        }

        btnActMainMrzScanner.setOnClickListener {
            startActivity(BaseScannerActivity.createIntent<IDScannerActivity>(this, getSelectedMLService()))
        }

        btnActMainQRScanner.setOnClickListener {
            startActivity(BaseScannerActivity.createIntent<SepaQrScannerActivity>(this, getSelectedMLService()))
        }

        val gmsAvailable = isGmsAvailable()
        val hmsAvailable = isHmsAvailable()


        if(gmsAvailable) {
            ivActMainGMSAvailable.setImageResource(R.drawable.ic_baseline_check_24dp_white)
            ivActMainGMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_green))
        } else {
            ivActMainGMSAvailable.setImageResource(R.drawable.ic_baseline_clear_24dp_white)
            ivActMainGMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red))
            rgActMainMobileService.removeView(rbActMainMobileGMS)
        }

        if(hmsAvailable) {
            ivActMainHMSAvailable.setImageResource(R.drawable.ic_baseline_check_24dp_white)
            ivActMainHMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_green))
        } else {
            ivActMainHMSAvailable.setImageResource(R.drawable.ic_baseline_clear_24dp_white)
            ivActMainHMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red))
            rgActMainMobileService.removeView(rbActMainMobileHMS)
        }

        val isOneServiceAvailable = gmsAvailable or hmsAvailable
        btnActMainIbanScanner.isEnabled = isOneServiceAvailable
        btnActMainMrzScanner.isEnabled = isOneServiceAvailable
        btnActMainQRScanner.isEnabled = gmsAvailable //hms has no qr recogniser like gms

        rgActMainMobileService.setOnCheckedChangeListener { _, i ->
            btnActMainQRScanner.isEnabled = i != rbActMainMobileHMS.id
        }

        when {
            gmsAvailable -> rbActMainMobileGMS.isChecked = true
            hmsAvailable -> rbActMainMobileHMS.isChecked = true
        }
    }

    private fun getSelectedMLService() : BaseAnalyser.MLService {
        return when {
            rbActMainMobileGMS.isChecked -> BaseAnalyser.MLService.GMS
            rbActMainMobileHMS.isChecked -> BaseAnalyser.MLService.HMS
            else -> throw RuntimeException("Either GMS or HMS is available on this device!")
        }
    }
}