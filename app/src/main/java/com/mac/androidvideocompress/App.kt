package com.mac.androidvideocompress

import android.app.Application
import com.mac.macdocument.MacDoc
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager

/**
 * @author ex-yangjb001
 * @date 2018/12/10.
 */
class App : Application() {
    companion object {
        lateinit var mContext: Application
    }
    override fun onCreate() {
        super.onCreate()
        QMUISwipeBackActivityManager.init(this)
        mContext = this
        MacDoc.init(this)
    }
}