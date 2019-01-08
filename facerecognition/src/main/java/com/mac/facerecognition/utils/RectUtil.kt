package com.mac.facerecognition.utils

import android.graphics.Rect
import android.graphics.RectF

/**
 * @author ex-yangjb001
 * @date 2019/1/8.
 */
object RectUtil {
    fun rectToRectF(r: Rect): RectF {
        return RectF(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat())
    }

    fun rectFToRect(r: RectF): Rect {
        return Rect(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
    }
}