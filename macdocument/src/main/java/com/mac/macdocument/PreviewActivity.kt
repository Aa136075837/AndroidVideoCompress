package com.mac.macdocument

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import com.tencent.smtt.sdk.TbsReaderView
import kotlinx.android.synthetic.main.activity_preview.*
import java.io.File

class PreviewActivity : AppCompatActivity(), TbsReaderView.ReaderCallback {
    companion object {
        const val FILE_PATH_KEY = "FILE_PATH_KEY_MAC"
    }

    override fun onCallBackAction(p0: Int?, p1: Any?, p2: Any?) {

    }

    private var mTbsReaderView: TbsReaderView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
    }

    override fun onStart() {
        super.onStart()
        mTbsReaderView = TbsReaderView(this, this)
        mTbsReaderView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        rootView.addView(mTbsReaderView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val filePath = intent.getStringExtra(FILE_PATH_KEY)
        initTitle(filePath)
        if (filePath.isEmpty()) {
            toast("文件路径为空")
        } else {
            disPlayFile(filePath)
        }
    }

    private fun disPlayFile(filePath: String?) {
        val bundle = Bundle()
        bundle.putString("filePath", filePath)
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().path)
        val type = getFileType(filePath)
        val preOpen = mTbsReaderView?.preOpen(type, false)
        if (preOpen!!) {
            mTbsReaderView?.openFile(bundle)
        } else {
            toast("不支持文件格式")
        }
    }

    private fun getFileType(filePath: String?): String {
        return if (filePath!!.lastIndexOf(".") < filePath.length - 1) {
            filePath.substring(filePath.lastIndexOf(".") + 1)
        } else {
            ""
        }
    }

    private fun initTitle(filePath: String?) {
        val index = filePath!!.lastIndexOf(File.separator)
        var title = filePath
        if (index > 0) {
            title = filePath.substring(index + 1)
        }
        titleTv.text = if (title.length > 20) {
            title.substring(0, 16) + "..."
        } else {
            title
        }
        backTv.setOnClickListener { finish() }
    }

    override fun onStop() {
        super.onStop()
        rootView.removeView(mTbsReaderView)
        mTbsReaderView?.onStop()
        mTbsReaderView = null
    }
}
