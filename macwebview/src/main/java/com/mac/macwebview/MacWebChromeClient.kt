package com.mac.macwebview

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import com.mac.macwebview.utils.MacConstant

class MacWebChromeClient : WebChromeClient {
    private var mActivity: Activity
    private lateinit var uploadMessage: ValueCallback<Uri>
    private var uploadMessageArray: ValueCallback<Array<Uri>>? = null

    constructor(context: Context) {
        mActivity = context as Activity
    }

    fun getUploadMessage(): ValueCallback<Uri> {
        return uploadMessage
    }

    fun setUploadMessage(uploadMessage: ValueCallback<Uri>) {
        this.uploadMessage = uploadMessage
    }

    fun getUploadMessageArray(): ValueCallback<Array<Uri>> {
        return uploadMessageArray!!
    }

    fun setUploadMessageArray(uploadMessageArray: ValueCallback<Array<Uri>>) {
        this.uploadMessageArray = uploadMessageArray
    }

    /**
     * For 3.0+ Devices (Start)
     * onActivityResult attached before constructor
     *
     * @param uploadMsg
     * @param acceptType
     */
    protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String) {
        if (null == mActivity) {
            return
        }
        uploadMessage = uploadMsg
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        mActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), MacConstant.FILE_CHOOSER_RESULT_CODE)
    }


    /**
     * For Lollipop 5.0+ Devices
     *
     * @param webView
     * @param filePathCallback
     * @param fileChooserParams
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
        if (null == mActivity) {
            return false
        }
        if (uploadMessageArray != null) {
            uploadMessageArray?.onReceiveValue(null)
            uploadMessageArray = null
        }

        uploadMessageArray = filePathCallback

        val intent = fileChooserParams.createIntent()
        try {
            mActivity.startActivityForResult(intent, MacConstant.REQUEST_SELECT_FILE)
        } catch (e: ActivityNotFoundException) {
            uploadMessageArray = null
            Toast.makeText(mActivity, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    /**
     * For Android 4.1 only
     *
     * @param uploadMsg
     * @param acceptType
     * @param capture
     */
    protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
        if (null == mActivity) {
            return
        }
        uploadMessage = uploadMsg
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        mActivity.startActivityForResult(Intent.createChooser(intent, "File Browser"), MacConstant.FILE_CHOOSER_RESULT_CODE)
    }

    protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
        if (null == mActivity) {
            return
        }
        uploadMessage = uploadMsg
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), MacConstant.FILE_CHOOSER_RESULT_CODE)
    }
}