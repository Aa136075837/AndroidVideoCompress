package com.mac.androidvideocompress

import android.os.Bundle
import com.mac.androidvideocompress.base.BaseActivity

class CoroutinesActivity : BaseActivity() {
    private val TAG = "Coroutines"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutines)
        initEvent()
    }

    private fun initEvent() {


    }

    suspend fun load() {

    }
}
