package com.minkiapps.scanner.util

import android.graphics.*
import com.minkiapps.scanner.scan.FrameMetadata
import java.io.ByteArrayOutputStream

object BitmapUtil {
    fun getBitmap(data: ByteArray, metadata: FrameMetadata): Bitmap {

        val image = YuvImage(
            data, ImageFormat.NV21, metadata.width, metadata.height, null
        )
        val stream = ByteArrayOutputStream()
        image.compressToJpeg(
            Rect(0, 0, metadata.width, metadata.height),
            80,
            stream
        )
        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        stream.close()
        return rotateBitmap(bmp, metadata.rotation, false, false)
    }

    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }
}