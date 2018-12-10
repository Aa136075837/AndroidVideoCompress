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
import com.bumptech.glide.Glide
import com.mac.compressjava.videocompressor.SiliCompressor
import com.mac.compressjava.videocompressor.VideoCompressAsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), VideoCompressAsyncTask.CompressListener {
    val TAG: String = "MainActivity"
    val REQUEST_VIDEO_CODE: Int = 0x125
    val REQUEST_IMAGE_CODE: Int = 0x321
    val REQUEST_PERMISSION_STORAGE = 0x4562

    lateinit var centerUrl: String
    lateinit var leftUrl: String
    lateinit var rightUrl: String
    var isCompressFinished = false
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

        image_left.setOnClickListener {
            if (isCompressFinished) {
                toPreviewImage(leftUrl)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE_CODE)
            }
        }

        image_center.setOnClickListener {
            if (isCompressFinished) {
                toPreviewImage(centerUrl)
            }
        }
        image_right.setOnClickListener {
            if (isCompressFinished) {
                toPreviewImage(rightUrl)
            }
        }
    }

    private fun toPreviewImage(url: String) {
        val intent = Intent(this, PreviewImageActivity::class.java)
        intent.putExtra("image_url", url)
        startActivity(intent)
    }

    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    if (requestCode == REQUEST_VIDEO_CODE) {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                        val videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                        val f = compressFilePath("/videos")
                        compress(File(videoPath), f.path)
                    } else if (requestCode == REQUEST_IMAGE_CODE) {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                        val f = compressFilePath("/images")
                        val compressedImagePath = SiliCompressor.with(this).compress(imagePath, f)
                        val compressedSize = File(compressedImagePath).length()

                        val genThumbImgFile = ImageUtils.genThumbImgFile(imagePath, 1024 * 1024)

                        centerUrl = genThumbImgFile.absolutePath
                        leftUrl = imagePath
                        rightUrl = compressedImagePath

                        Glide.with(this).load(imagePath).into(image_left)
                        Glide.with(this).load(compressedImagePath).into(image_right)
                        Glide.with(this).load(genThumbImgFile.absolutePath).into(image_center)

                        tvLeft.text = getFileSizeMb(size)
                        tvRight.text = getFileSizeMb(compressedSize)
                        tvCenter.text = getFileSizeMb(genThumbImgFile.length())
                        isCompressFinished = true
                    }
                }
                cursor.close()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * @param size b
     */
    fun getFileSizeMb(size: Long): String {
        return String.format("%.3f", size / 1024f / 1024f) + "MB"
    }

    private fun compressFilePath(type: String): File {
        val f = File(getStoragePath() + type)

        if (!f.exists()) {
            f.mkdir()
        }
        return f
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
