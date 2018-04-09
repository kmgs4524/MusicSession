package com.york.android.musicsession.model.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.*

/**
 * Created by York on 2018/4/9.
 */
class BlurBuilder {
    var BITMAP_SCALE = 0.4f
    var BLUR_RADIUS = 7.5f

    fun blur(image: Bitmap, context: Context): Bitmap {
        val width = Math.round(image.width * BITMAP_SCALE)
        val height = Math.round(image.height * BITMAP_SCALE)

        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val renderScript = RenderScript.create(context)
        val intrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

        val tempInput = Allocation.createFromBitmap(renderScript, inputBitmap)
        val tempOutput = Allocation.createFromBitmap(renderScript, outputBitmap)

        intrinsic.setRadius(BLUR_RADIUS)
        intrinsic.setInput(tempInput)
        intrinsic.forEach(tempOutput)
        tempOutput.copyTo(outputBitmap)

//        inputBitmap.recycle()

        return outputBitmap
    }
}