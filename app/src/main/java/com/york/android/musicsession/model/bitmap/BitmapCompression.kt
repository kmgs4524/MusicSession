package com.york.android.musicsession.model.bitmap

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

/**
 * Created by York on 2018/2/27.
 */
object BitmapCompression {
    fun compressBySize(pathName: String, requestWidth: Int, requestHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        // 首先改變圖片採樣率
        // inJustDecodeBounds sets to true will let decoder return null in order to just query the bitmap
        // and avoid allocating memory for its pixels
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        // 計算取樣率
        options.inSampleSize = calculateFitSize(requestWidth, requestHeight, options)
        // 改變圖片質量，預設使用ARGB_8888(每像素採用 4 bytes)
        // RGB_565，每像素採用 2 bytes
        options.inPreferredConfig = Bitmap.Config.RGB_565
        // 設回false，產生新的Bitmap
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(pathName, options)
    }

    private fun calculateFitSize(reqWidth: Int, reqHeight: Int, options: BitmapFactory.Options): Int {
        // 原始圖片的寬高
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // 在保證解析出的bitmap寬高分別大於目標尺寸寬高的前提下，取可能的inSampleSize的最大值
            // inSampleSize 只能是2的次方
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
//                Bitmap.createScaledBitmap()
            }
        }

        Log.d("calculateFitSize", "imSampleSize: ${inSampleSize}")
        return inSampleSize
    }

}