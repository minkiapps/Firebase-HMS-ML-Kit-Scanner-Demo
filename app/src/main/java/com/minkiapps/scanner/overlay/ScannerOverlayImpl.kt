package com.minkiapps.scanner.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.View
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.google.mlkit.vision.text.Text
import com.minkiapps.scanner.R
import com.minkiapps.scanner.util.getEnum
import com.minkiapps.scanner.util.px

class ScannerOverlayImpl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScannerOverlay {

    private val transparentPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private val strokePaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            strokeWidth = context.px(3f)
            style = Paint.Style.STROKE
        }
    }

    var drawBlueRect : Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    fun drawGraphicBlocks(graphicBlocks : List<GraphicBlock>) {
        this.graphicBlocks = graphicBlocks
        drawBlueRect = true
    }

    var type: Type
    private var graphicBlocks : List<GraphicBlock>? = null

    private val blueColor = Color.BLUE

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScannerOverlayImpl, 0, 0)
        type = typedArray.getEnum(R.styleable.ScannerOverlayImpl_type, Type.IBAN)
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#88000000"))

        val radius = context.px(4f)
        val rectF = scanRect
        canvas.drawRoundRect(rectF, radius, radius, transparentPaint)
        strokePaint.color = if(drawBlueRect) blueColor else Color.WHITE
        canvas.drawRoundRect(rectF, radius, radius, strokePaint)

        graphicBlocks?.forEach { block ->
            val scaleX = scanRect.width() / block.bitmapSize.width
            val scaleY = scanRect.height() / block.bitmapSize.height

            canvas.withTranslation(scanRect.left, scanRect.top) {
                withScale(scaleX, scaleY) {
                    drawRoundRect(RectF(block.textBlock.boundingBox!!), radius, radius, strokePaint)
                }
            }
        }
        graphicBlocks = null
    }

    override val size: Size
        get() = Size(width, height)

    override val scanRect: RectF
        get() = when (type) {
            Type.IBAN -> {
                val rectW = width * 0.9f
                val l = (width - rectW) / 2
                val r = width - l
                val t = height * 0.15f
                val b = t + rectW / 10
                RectF(l, t, r, b)
            }
            Type.ID -> {
                val rectW = width * 0.95f
                val l = (width - rectW) / 2
                val r = width - l
                val rectH = rectW / 1.5f
                val t = height * 0.15f
                val b = t + rectH
                RectF(l, t, r, b)
            }
            Type.SEPAQR -> {
                val size = width * 0.6f
                val l = (width - size) / 2
                val r = width - l
                val t = height * 0.15f
                val b = t + size
                RectF(l, t, r, b)
            }
        }

    enum class Type {
        IBAN,
        ID,
        SEPAQR
    }

    data class GraphicBlock(val textBlock: Text.TextBlock, val bitmapSize: Size)
}