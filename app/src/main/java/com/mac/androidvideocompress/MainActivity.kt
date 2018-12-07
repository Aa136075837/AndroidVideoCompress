package com.mac.androidvideocompress

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mac.compressjava.videocompressor.VideoCompressAsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), VideoCompressAsyncTask.CompressListener {
    val TAG: String = "MainActivity"
    val REQUEST_VIDEO_CODE: Int = 0x125
    val REQUEST_PERMISSION_STORAGE = 0x4562
    override fun onSuccess(compressedFile: File) {
        Log.e(TAG, "压缩完成")
    }

    override fun onFail() {
        Log.e(TAG, "压缩失败")
    }

    override fun onProgress(percent: Float) {
        Log.e(TAG, "压缩进度 $percent%")
    }

    override fun onCompressStart() {
        Log.e(TAG, "开始压缩")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        selectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_VIDEO_CODE)
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                        val videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        val f = File(getStoragePath() + "/videos")

                        if (!f.exists()) {
                            f.mkdir()
                        }
                        compress(File(videoPath), f.path)
                    }
                    cursor.close()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun compress(sourceFile: File, compressPath: String) {
        VideoCompressAsyncTask(this, this).execute(sourceFile.absolutePath, compressPath)
    }

    fun existSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getStoragePath(): String {
        return if (existSDCard()) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            Environment.getDataDirectory().absolutePath
        }
    }
}
