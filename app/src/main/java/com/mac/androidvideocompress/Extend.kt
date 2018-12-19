package com.mac.androidvideocompress

import android.content.Context
import android.util.TypedValue

/**
 * @author ex-yangjb001
 * @date 2018/12/19.
 */

fun Context.dip2Px(dip: Int): Int {
    val density = resources.getDisplayMetrics().density
    return (dip.toFloat() * density + 0.5f).toInt()
}

fun Context.px2dip(px: Int): Int {
    val density = resources.getDisplayMetrics().density
    return (px.toFloat() / density + 0.5f).toInt()
}

fun Context.sp2px(sp: Int): Int {
    return (TypedValue.applyDimension(2, sp.toFloat(), resources.getDisplayMetrics()) + 0.5f).toInt()
}