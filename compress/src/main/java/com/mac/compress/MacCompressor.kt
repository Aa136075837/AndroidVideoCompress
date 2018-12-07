package com.mac.compress

import android.content.Context
import android.util.Log
import java.io.File
import java.net.URISyntaxException

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
class MacCompressor private constructor(context: Context) {
    val LOG_TAG: String = "MacCompressor"

    companion object {
        fun with(context: Context): MacCompressor {
            return Builder(context).build()
        }
    }

    @Throws(URISyntaxException::class)
    fun compressVideo(videoFilePath: String, destinationDir: String, listener: MediaController.CompressProgressListener): String {
        return compressVideo(videoFilePath, destinationDir, 0, 0, 0, listener)
    }

    @Throws(URISyntaxException::class)
    fun compressVideo(videoFilePath: String, destinationDir: String, outWidth: Int, outHeight: Int, bitrate: Int, listener: MediaController.CompressProgressListener): String {
        val convertVideo = MediaController.getInstance().convertVideo(videoFilePath, File(destinationDir), outWidth, outHeight, bitrate, listener)
        if (convertVideo) {
            Log.v(LOG_TAG, "Video Conversion Complete")
        } else {
            Log.v(LOG_TAG, "Video conversion in progress")
        }
        return MediaController.cachedFile?.path!!
    }

    class Builder {
        var mContext: Context
        val singleton: MacCompressor

        constructor(context: Context) {
            if (context == null) {
                throw IllegalArgumentException("Context mast not be null")
            }
            this.mContext = context.applicationContext
            singleton = MacCompressor(context)
        }

        fun build(): MacCompressor {
            return singleton
        }
    }
}