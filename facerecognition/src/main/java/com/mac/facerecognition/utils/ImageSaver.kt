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
        val buffer = mImage!!.getPlanes()[0].buffer
        var bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        Log.e("FaceError", "youde")
        val options = BitmapFactory.Options()

        options.inSampleSize = 2
        options.inJustDecodeBounds = false

        options.inMutable = true
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val face = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)


//                float x = ((mCameraRect.bottom - mCameraRect.top) / (float) face.getWidth());
//                float y = ((mCameraRect.right - mCameraRect.left) / (float) face.getHeight());
//
//                Rect bounds = mFaces[0].getBounds();
//                switch (mCameraSensorOrientation) {
//                    case 90:
//                        face = Bitmap.createBitmap(face, (int) (face.getWidth() - bounds.bottom / y),
//                                (int) (bounds.left / x),
//                                (int) ((bounds.bottom - bounds.top) / y),
//                                (int) ((bounds.right - bounds.left) / x));
//                        break;
//                    case 270:
//                        face = Bitmap.createBitmap(face, (int) (bounds.top / x),
//                                (int) (face.getHeight() - bounds.right / y),
//                                (int) ((bounds.bottom - bounds.top) / x),
//                                (int) ((bounds.right - bounds.left) / y));
//                        break;
//                }


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