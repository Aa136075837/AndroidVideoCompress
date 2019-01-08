package com.mac.facerecognition.utils

import android.graphics.Rect

/**
 * @author ex-yangjb001
 * @date 2019/1/8.
 */
class CustomFace {
    private var rect: Rect? = null

    constructor(rect: Rect?) {
        this.rect = rect
    }

    fun getBounds(): Rect? {
        return rect
    }
}