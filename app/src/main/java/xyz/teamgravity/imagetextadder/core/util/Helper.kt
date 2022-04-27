package xyz.teamgravity.imagetextadder.core.util

import android.graphics.Bitmap
import xyz.teamgravity.imagetextadder.core.constant.Const

object Helper {

    fun getImageExtension(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.PNG -> Const.PNG
            Bitmap.CompressFormat.JPEG -> Const.JPG
            else -> Const.JPG
        }
    }
}