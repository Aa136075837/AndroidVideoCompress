package com.mac.facerecognition.utils

import android.util.Size

/**
 * @author ex-yangjb001
 * @date 2019/1/8.
 */
class CompareSizesByArea : Comparator<Size>{
    override fun compare(o1: Size?, o2: Size?): Int {
        return java.lang.Long.signum(o1!!.width.toLong() * o1.height - o2!!.width.toLong() * o2.height)
    }
}