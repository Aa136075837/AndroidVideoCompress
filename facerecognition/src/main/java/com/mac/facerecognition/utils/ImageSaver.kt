package com.mac.facerecognition.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.Image
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author ex-yangjb001
 * @date 2019/1/8.
 */
class ImageSaver : Runnable {
    private var mImage: Image? = null
    private var mCameraSensorOrientation: Int = 0
    private var mCameraRect: Rect? = null
    private var mCacheDir: File? = null
    private var mOnImageSaveListener: OnImageSaveListener? = null

    constructor(mImage: Image?, mCameraSensorOrientation: Int, mCameraRect: Rect?, cacheDir: File, listener: OnImageSaveListener) {
        this.mImage = mImage
        this.mCameraSensorOrientation = mCameraSensorOrientation
        this.mCameraRect = mCameraRect
        this.mCacheDir = cacheDir
        this.mOnImageSaveListener = listener
    }

    override fun run() {
        val buffer = mImage!!.planes[0].buffer
        var bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        Log.e("FaceError", "youde")
        val options = BitmapFactory.Options()

        options.inSampleSize = 2
        options.inJustDecodeBounds = false

        options.inMutable = true
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val face = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val stream = ByteArrayOutputStream()
        face.compress(Bitmap.CompressFormat.JPEG, 30, stream)
        bytes = stream.toByteArray()
        face.recycle()


        var output: FileOutputStream? = null
        var savedFile: File? = null
        try {
            savedFile = File(mCacheDir, System.currentTimeMillis().toString() + "pic.jpg")
            if (savedFile.exists()) {
                savedFile.delete()
            }
            output = FileOutputStream(savedFile)
            output!!.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mImage?.close()

            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        mOnImageSaveListener?.onImageSaved(savedFile)
    }

    interface OnImageSaveListener {
        fun onImageSaved(savedFile: File?)
    }
}