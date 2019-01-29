package com.mac.androidvideocompress

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import com.mac.facerecognition.FaceRecognitionActivity
import com.mac.transitionanim.AActivity
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {
    private val data = ArrayList<String>()
    val REQUEST_PERMISSION_STORAGE = 0x4962

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        checkPermission()

        data.add("视频、图片压缩。图片组合、文件预览")
        data.add("人脸")
        data.add("alv")
        data.add("跳转")
        data.add("协程")
        data.add("encode")
        mListView.adapter = CompressAdapter()
        mListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    toActivity(MainActivity::class.java)
                }
                1 -> {
                    toActivity(FaceRecognitionActivity::class.java)
                }
                2 -> {
                    toActivity(ALvActivity::class.java)
                }
                3 -> {
                    toActivity(AActivity::class.java)
                }
                4 -> {
                    toActivity(CoroutinesActivity::class.java)
                }
                5 -> {
                    toActivity(EnCodeActivity::class.java)
                }
                else -> {
                }
            }
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
            ), REQUEST_PERMISSION_STORAGE)
        }
    }

    inner class CompressAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var holder: Holder
            var view: View? = null
            if (convertView == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.item_list, parent, false)
                holder = Holder()
                holder.mTextView = view.findViewById(R.id.item_tv)
                view.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            holder.mTextView.text = data[position]
            return view!!
        }

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return data.size
        }

    }

    inner class Holder {
        lateinit var mTextView: TextView
    }
}
