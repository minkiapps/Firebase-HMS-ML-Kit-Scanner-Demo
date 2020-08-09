package com.minkiapps.scanner

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.minkiapps.scanner.iban.IbanScannerActivity
import com.minkiapps.scanner.id.IDScannerActivity
import com.minkiapps.scanner.sepaqr.SepaQrScannerActivity
import com.minkiapps.scanner.util.isGmsAvailable
import com.minkiapps.scanner.util.isHmsAvailable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnActMainIbanScanner.setOnClickListener {
            startActivity(Intent(this, IbanScannerActivity::class.java))
        }

        btnActMainMrzScanner.setOnClickListener {
            startActivity(Intent(this, IDScannerActivity::class.java))
        }

        btnActMainQRScanner.setOnClickListener {
            startActivity(Intent(this, SepaQrScannerActivity::class.java))
        }

        val gmsAvailable = isGmsAvailable()
        val hmsAvailable = isHmsAvailable()

        if(gmsAvailable) {
            ivActMainGMSAvailable.setImageResource(R.drawable.ic_baseline_check_24dp_white)
            ivActMainGMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_green))
        } else {
            ivActMainGMSAvailable.setImageResource(R.drawable.ic_baseline_clear_24dp_white)
            ivActMainGMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red))
        }

        if(hmsAvailable) {
            ivActMainHMSAvailable.setImageResource(R.drawable.ic_baseline_check_24dp_white)
            ivActMainHMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_green))
        } else {
            ivActMainHMSAvailable.setImageResource(R.drawable.ic_baseline_clear_24dp_white)
            ivActMainHMSAvailable.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red))
        }

        val isOneServiceAvailable = gmsAvailable or hmsAvailable
        btnActMainIbanScanner.isEnabled = isOneServiceAvailable
        btnActMainMrzScanner.isEnabled = isOneServiceAvailable
        btnActMainQRScanner.isEnabled = isOneServiceAvailable
    }


}