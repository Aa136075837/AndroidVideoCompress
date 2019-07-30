package com.mac.androidvideocompress.base

import android.annotation.SuppressLint
import android.os.Bundle
import com.qmuiteam.qmui.arch.QMUIActivity
import com.qmuiteam.qmui.util.QMUIStatusBarHelper

@SuppressLint("Registered")
open class BaseActivity : QMUIActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QMUIStatusBarHelper.setStatusBarLightMode(this)
    }

    override fun translucentFull(): Boolean {
        return false
    }

    override fun isInSwipeBack(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
//        QDUpgradeManager.getInstance(getContext()).runUpgradeTipTaskIfExist(this)
    }
}