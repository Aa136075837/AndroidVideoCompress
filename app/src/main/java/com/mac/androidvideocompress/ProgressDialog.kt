package com.mac.androidvideocompress

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.progress_dialog.*

/**
 * @author ex-yangjb001
 * @date 2018/12/13.
 */
class ProgressDialog : Dialog {

    constructor(context: Context?) : super(context) {
        val inflate = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        setContentView(inflate)
        val layoutParams = inflate.layoutParams
        layoutParams.width = context!!.resources.displayMetrics.widthPixels
        inflate.layoutParams = layoutParams
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    fun setProgress(progress: Int) {
        mProgressBar.setProgress(progress)
        mProgressTv.text = progress.toString() + "%"
    }

}