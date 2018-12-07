package com.mac.compress

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.net.URISyntaxException

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
class VideoCompressAsyncTask(val listener: CompressListener, val context: Context) : AsyncTask<String, Float, String>() {

    interface CompressListener {
        fun onCompressStart()

        fun onSuccess(compressedFile: File)

        fun onFail()

        fun onProgress(percent: Float)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        listener?.onCompressStart()
    }

    override fun doInBackground(vararg params: String?): String {
        var filePath: String? = null
        try {
            filePath = MacCompressor.with(context)
                    .compressVideo(params[0].toString(), params[1].toString(),
                    object : MediaController.CompressProgressListener {
                        override fun onProgress(percent: Float) {
                            onProgressUpdate(percent)
                        }
                    })
        } catch (e: URISyntaxException) {
            Log.e("VideoCompressAsyncTask", e.message)
        }
        return filePath.toString()
    }

    override fun onProgressUpdate(vararg values: Float?) {
        super.onProgressUpdate(*values)
        listener?.onProgress(values[0]!!.toFloat())
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        val imageFile = File(result)
        val length = imageFile.length() / 1024f // Size in KB
        val value: String
        if (length >= 1024) {
            value = (length / 1024f).toString() + " MB"
        } else {
            value = length.toString() + " KB"
        }
        if (listener != null) {
            if (imageFile.length() > 0) {
                listener.onSuccess(imageFile)
            } else {
                listener.onFail()
            }
        }
        Log.i("Silicompressor", "Path: $result")
    }
}