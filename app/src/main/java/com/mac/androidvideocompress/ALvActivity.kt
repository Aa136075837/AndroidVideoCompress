package com.mac.androidvideocompress

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.HORIZONTAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.alibaba.android.vlayout.layout.GridLayoutHelper
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper
import kotlinx.android.synthetic.main.activity_alv.*
import kotlinx.android.synthetic.main.item_alv.view.*

class ALvActivity : AppCompatActivity() {
    val icons = arrayOf(R.drawable.add,
            R.drawable.add_member_sel, R.drawable.add_member_seled, R.drawable.album)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alv)

        initVLayoutAdapter()
//        initDefAdatper()

        mRvALv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(10, 10, 10, 10)
            }
        })
    }

    fun initVLayoutAdapter() {
        val virManager = VirtualLayoutManager(this)
        virManager.orientation = VirtualLayoutManager.HORIZONTAL
        mRvALv.layoutManager = virManager

        val helpers = ArrayList<LayoutHelper>()
        val gridLayoutHelper = GridLayoutHelper(3)
        gridLayoutHelper.itemCount = 3

        val staggerHelper = StaggeredGridLayoutHelper(2)
        staggerHelper.itemCount = 3

        val linHelper = LinearLayoutHelper()
        linHelper.itemCount = 1

        helpers.add(staggerHelper)
        helpers.add(linHelper)
        helpers.add(gridLayoutHelper)
        virManager.setLayoutHelpers(helpers)


        val adapter = ALvRecyclerAdapter(virManager)
        mRvALv.adapter = adapter
    }

    fun initDefAdatper() {
        val manager = StaggeredGridLayoutManager(2, HORIZONTAL)
        manager.orientation = HORIZONTAL
        mRvALv.layoutManager = manager
        mRvALv.adapter = ALvAdapter()
    }

    inner class ALvAdapter : RecyclerView.Adapter<ALvHolder>() {
        override fun getItemViewType(position: Int): Int {
            return R.layout.item_alv
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ALvHolder {
            val view = LayoutInflater.from(p0.context).inflate(p1, p0, false)
            return ALvHolder(view)
        }

        override fun getItemCount(): Int {
            return 20
        }

        override fun onBindViewHolder(p0: ALvHolder, p1: Int) {

            val layoutParams = p0.imageView.layoutParams
            p0.textView.text = p1.toString()
            when (p1) {
                1 -> {
                    layoutParams.width = this@ALvActivity.dip2Px(50)
                    layoutParams.height = this@ALvActivity.dip2Px(100)
                }
                2 -> {
                    layoutParams.width = this@ALvActivity.dip2Px(50)
                    layoutParams.height = this@ALvActivity.dip2Px(100)
                }
                0 -> {
                    layoutParams.width = this@ALvActivity.dip2Px(100)
                    layoutParams.height = this@ALvActivity.dip2Px(100)
                }
                3 -> {
                    layoutParams.width = this@ALvActivity.dip2Px(200)
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }

            }
            p0.itemView.layoutParams = layoutParams
            p0.imageView.setImageResource(icons[p1 % 4])
        }


    }

    inner class ALvHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.alvItemIv
        val textView = itemView.alvItemTv
    }
}
