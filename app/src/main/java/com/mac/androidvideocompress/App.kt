package com.mac.androidvideocompress

import android.app.Application

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
        mContext = this
    }
}