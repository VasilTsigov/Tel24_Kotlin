package bg.iag.tel24.ui.employee

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import coil.size.Size
import coil.transform.Transformation

/**
 * Circle crop that shifts the crop window up by [verticalShift] fraction of
 * the image height, so the face appears lower (centred) in the resulting circle
 * instead of being cut off at the top.
 */
class FaceCircleCropTransformation(private val verticalShift: Float = 0.12f) : Transformation {

    override val cacheKey = "FaceCircleCropTransformation($verticalShift)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val minDim = minOf(input.width, input.height)
        val offsetY  = (input.height * verticalShift).toInt()
        val left     = (input.width - minDim) / 2
        val top      = ((input.height - minDim) / 2 - offsetY).coerceAtLeast(0)

        val output = Bitmap.createBitmap(minDim, minDim, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint  = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw filled circle as mask
        canvas.drawCircle(minDim / 2f, minDim / 2f, minDim / 2f, paint)

        // 2. Draw source bitmap clipped to the circle
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(
            input,
            Rect(left, top, left + minDim, top + minDim),
            Rect(0, 0, minDim, minDim),
            paint
        )

        return output
    }
}
