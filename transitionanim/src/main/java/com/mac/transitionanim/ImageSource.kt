package com.mac.transitionanim

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader

/**
 * @author ex-yangjb001
 * @date 2018/12/14.
 */
class ImageSource : LoaderManager.LoaderCallbacks<Cursor> {
    private val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val IMAGE_PROJECTION = arrayOf(//查询图片需要的数据列
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            "duration",
            MediaStore.MediaColumns.DATA)
    private val SELECTION = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0")

    private val SELECTION_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()
//            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    var mActivity: FragmentActivity
    var mListener: LoadFinishListener
    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
        return CursorLoader(mActivity, QUERY_URI, IMAGE_PROJECTION, SELECTION, SELECTION_ARGS, ORDER_BY)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        val pathData = ArrayList<String>()
        while (cursor!!.moveToNext()) {
            val imagePath = cursor!!.getString(cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            pathData.add(imagePath)
        }
        mListener?.onLoadFinish(pathData)
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {

    }

    constructor(activity: FragmentActivity, listener: LoadFinishListener) {
        mActivity = activity
        mListener = listener
        val loaderManager = activity.supportLoaderManager
        loaderManager.initLoader(0, null, this)
    }

    interface LoadFinishListener {
        fun onLoadFinish(pathList: ArrayList<String>)
    }
}