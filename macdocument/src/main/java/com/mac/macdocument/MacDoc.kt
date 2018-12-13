package com.mac.macdocument

import android.app.Application
import android.content.Context
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsVideo

/**
 * @author ex-yangjb001
 * @date 2018/12/13.
 */
class MacDoc {
    companion object {
        fun init(application: Application) {
            QbSdk.initX5Environment(application, object : QbSdk.PreInitCallback {
                override fun onCoreInitFinished() {
                    application.toast("X5内核加载成功 onCoreInitFinished")
                }

                override fun onViewInitFinished(p0: Boolean) {
                    if (p0) {
                        application.toast("X5内核加载成功 onViewInitFinished")
                    } else {
                        application.toast("X5内核加载失败 onViewInitFinished")
                    }
                }
            })
        }

        fun playVideoWithX5(filePath: String, context: Context) {
            val b = TbsVideo.canUseTbsPlayer(context.applicationContext)
            if (b) {
                TbsVideo.openVideo(context.applicationContext, filePath)
            }
        }
    }
}