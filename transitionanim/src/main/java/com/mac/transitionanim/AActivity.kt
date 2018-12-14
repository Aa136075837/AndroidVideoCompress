package com.mac.transitionanim

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_a.*

class AActivity : AppCompatActivity() {
    var data = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
//        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//        window.exitTransition = Slide(Gravity.LEFT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a)

        initRecycler()
    }

    private fun initRecycler() {
        mARecycler.layoutManager = GridLayoutManager(this@AActivity, 4)
        val adapter = MacAdapter(this@AActivity, data)
        mARecycler.adapter = adapter
        ImageSource(this, object : ImageSource.LoadFinishListener {
            override fun onLoadFinish(imagePath: ArrayList<String>) {
                if (imagePath.size > 0) {
                    adapter.setData(imagePath)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }
}
