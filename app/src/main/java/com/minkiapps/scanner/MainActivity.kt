package com.minkiapps.scanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minkiapps.scanner.iban.IbanScannerActivity
import com.minkiapps.scanner.id.IDScannerActivity
import com.minkiapps.scanner.sepaqr.SepaQrScannerActivity
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
    }

}