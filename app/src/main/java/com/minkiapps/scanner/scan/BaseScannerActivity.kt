package com.minkiapps.scanner.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.minkiapps.scanner.R
import com.minkiapps.scanner.analyser.BaseAnalyser
import com.minkiapps.scanner.overlay.ScannerOverlayImpl
import com.minkiapps.scanner.util.extraSerializableOrThrow
import kotlinx.android.synthetic.main.activity_scanner.*
import timber.log.Timber
import java.util.concurrent.Executors

abstract class BaseScannerActivity<T> : AppCompatActivity(R.layout.activity_scanner) {

    private var torchOn : Boolean = false
    private val analyserExecutor = Executors.newSingleThreadExecutor()

    private val mlService : BaseAnalyser.MLService by extraSerializableOrThrow(EXTRA_MOBILE_SERVICE)

    protected val analyser : BaseAnalyser<T> by lazy {
        initImageAnalyser(mlService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        olActScanner.type = getScannerType()
        olActScanner.mlService = mlService
        setUp()
    }

    private fun setUp() {
        analyser.bitmapLiveData().observe(this, {
            ivActScannerCroppedPreview.setImageBitmap(it)
        })

        analyser.errorLiveData().observe(this, { e ->
            Timber.e(e, "Analysing failed")
            Toast.makeText(this, "Scanner failed, reason: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        })

        analyser.liveData().observe(this, { result ->
            tvActScannerScannedResult.text = ""
            result?.let {
                tvActScannerScannedResult.text = it.toString()
            }
        })

        analyser.debugInfoLiveData().observe(this, {
            val surfaceView = pvActScanner[0]
            val info = "$it\nPreview Size (${surfaceView.width}, ${surfaceView.height}) " +
                    "Translation (${surfaceView.translationX}, ${surfaceView.translationY}) " +
                    "Scale (${surfaceView.scaleX}, ${surfaceView.scaleY}) " +
                    "Pivot (${surfaceView.pivotX}, ${surfaceView.pivotY}) " +
                    "Rotation (${surfaceView.rotation}) " +
                    "Container Size (${pvActScanner.width}, ${pvActScanner.height})"
            //Transition (${surfaceView.translationX}, ${surfaceView.translationY}) Scale (${surfaceView.scaleX}, ${surfaceView.scaleY})
            tvActScannerDebugInfo.text = info
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            lifecycle.addObserver(analyser)
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT))
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT))
                .build()
                .also {
                    it.setAnalyzer(analyserExecutor, analyser)
                }

            // Select back camera
            val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                preview.setSurfaceProvider(pvActScanner.surfaceProvider)

                fabActScannerTorch.setOnClickListener {
                    torchOn = !torchOn
                    camera.cameraControl.enableTorch(torchOn)
                    setTorchUI()
                }
                setTorchUI()
            } catch (exc: Exception) {
                Timber.e(exc,"Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun setTorchUI() {
        fabActScannerTorch.setImageResource(if(torchOn) R.drawable.ic_baseline_flash_off_24dp_white else R.drawable.ic_baseline_flash_on_24dp_white)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    protected fun scannerOverlay() : ScannerOverlayImpl = olActScanner

    abstract fun initImageAnalyser(mlService: BaseAnalyser.MLService): BaseAnalyser<T>

    abstract fun getScannerType(): ScannerOverlayImpl.Type

    companion object {
        const val EXTRA_MOBILE_SERVICE = "EXTRA_MOBILE_SERVICE"

        private const val TARGET_PREVIEW_WIDTH = 960
        private const val TARGET_PREVIEW_HEIGHT = 1280

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        inline fun <reified T : AppCompatActivity>createIntent(context: Context, mobileService: BaseAnalyser.MLService) : Intent {
            val intent = Intent(context, T::class.java)
            intent.putExtra(EXTRA_MOBILE_SERVICE, mobileService)
            return intent
        }
    }

}