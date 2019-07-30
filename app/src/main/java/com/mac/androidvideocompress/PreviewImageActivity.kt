package com.mac.androidvideocompress

import android.os.Bundle
import com.bumptech.glide.Glide
import com.mac.androidvideocompress.base.BaseActivity
import kotlinx.android.synthetic.main.activity_preview_image.*

class PreviewImageActivity : BaseActivity() {
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        url = intent.getStringExtra("image_url")

        Glide.with(this).load(url).into(big_image)
    }
}
