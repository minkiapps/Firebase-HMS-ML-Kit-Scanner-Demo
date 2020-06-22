package com.minkiapps.scanner.analyser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.minkiapps.scanner.overlay.ScannerOverlay
import com.minkiapps.scanner.scan.FrameMetadata
import com.minkiapps.scanner.util.BitmapUtil
import com.minkiapps.scanner.util.YuvNV21Util
import timber.log.Timber

abstract class BaseAnalyser<T>(private val scannerOverlay: ScannerOverlay) : ImageAnalysis.Analyzer, LifecycleObserver {

    private val imageMutableData = MutableLiveData<Bitmap>()
    private val mutableLiveData = MutableLiveData<T>()
    private val errorData = MutableLiveData<Exception>()
    private val debugInfoData = MutableLiveData<String>()

    fun liveData() : LiveData<T> = mutableLiveData
    fun errorLiveData() : LiveData<Exception> = errorData
    fun bitmapLiveData() : LiveData<Bitmap> = imageMutableData
    fun debugInfoLiveData() : LiveData<String> = debugInfoData

    var emitDebugInfo : Boolean = true
    var emitBitmap : Boolean = true

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val imageProxyReadyEpoch = System.currentTimeMillis()
            val rotation = imageProxy.imageInfo.rotationDegrees
            Timber.d("New image from proxy width : ${imageProxy.width} height : ${imageProxy.height} format : ${imageProxy.format} rotation: $rotation")
            val scannerRect = getScannerRectToPreviewViewRelation(imageProxy.width.toFloat() / imageProxy.height)

            val image = imageProxy.image!!
            val cropRect = image.getCropRectAccordingToRotation(scannerRect, rotation)
            image.cropRect = cropRect

            val byteArray = YuvNV21Util.yuv420toNV21(image)
            val inputImage = InputImage.fromByteArray(byteArray, cropRect.width(), cropRect.height(), rotation, InputImage.IMAGE_FORMAT_NV21)

            if(emitBitmap) {
                val bitmap = BitmapUtil.getBitmap(byteArray, FrameMetadata(cropRect.width(), cropRect.height(), rotation))
                imageMutableData.postValue(bitmap)
            }
            val imagePreparedReadyEpoch = System.currentTimeMillis()
            Timber.d("Bitmap prepared width: ${cropRect.width()} height: ${cropRect.height()}")

            val size = if(rotation == 90 || rotation == 270) {
                Size(cropRect.height(), cropRect.width())
            } else {
                Size(cropRect.width(), cropRect.height())
            }
            onInputImagePrepared(inputImage, size)

            val imageProcessedEpoch = System.currentTimeMillis()

            if(emitDebugInfo) {
                debugInfoData.postValue("""
                   Image proxy (${imageProxy.width},${imageProxy.height}) format : ${imageProxy.format} rotation: $rotation 
                   Cropped Image (${size.width},${size.height}) Preparing took: ${imagePreparedReadyEpoch - imageProxyReadyEpoch}ms
                   OCR Processing took : ${imageProcessedEpoch - imagePreparedReadyEpoch}ms
                """.trimIndent())
            }

            imageProxy.close()
        } catch (e : Exception) {
            errorData.postValue(e)
        }
    }

    protected fun postResult(value : T?) {
        mutableLiveData.postValue(value)
    }

    private fun getScannerRectToPreviewViewRelation(previewSizeRatio: Float): ScannerRectToPreviewViewRelation {
        val size = scannerOverlay.size
        val width = size.width
        val height = size.height
        val previewWidth = height / previewSizeRatio
        val widthDeltaLeft = (previewWidth - width) / 2

        val scannerRect = scannerOverlay.scanRect
        val rectStartX = widthDeltaLeft + scannerRect.left
        val rectStartY = scannerRect.top

        return ScannerRectToPreviewViewRelation(
            rectStartX / previewWidth,
            rectStartY / height,
            scannerRect.width() / previewWidth,
            scannerRect.height() / height
        )
    }

    abstract fun onInputImagePrepared(inputImage: InputImage, size: Size)

    data class ScannerRectToPreviewViewRelation(val relativePosX: Float,
                                                val relativePosY: Float,
                                                val relativeWidth: Float,
                                                val relativeHeight: Float)

    private fun Image.getCropRectAccordingToRotation(scannerRect: BaseAnalyser.ScannerRectToPreviewViewRelation, rotation: Int) : Rect {
        return when(rotation) {
            0, 360 -> {
                val startX = (scannerRect.relativePosX * this.width).toInt()
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val startY = (scannerRect.relativePosY * this.height).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            90, -270 -> {
                val startX = (scannerRect.relativePosY * this.width).toInt()
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val startY = (scannerRect.relativePosX * this.height).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            180, -180 -> {
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                val startX = (scannerRect.relativePosX * this.width).toInt()
                val startY = (this.height - scannerRect.relativePosY * this.height - numberPixelH).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            270, -90 -> {
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                val startX = (this.width - scannerRect.relativePosY * this.width - numberPixelW).toInt()
                val startY = (scannerRect.relativePosX * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            else -> throw IllegalArgumentException("Rotation degree ($rotation) not supported!")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    abstract fun close()
}