package com.mac.androidvideocompress

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.vlayout.VirtualLayoutAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import kotlinx.android.synthetic.main.item_alv.view.*

/**
 * @author ex-yangjb001
 * @date 2018/12/19.
 */
class ALvRecyclerAdapter(layoutManager: VirtualLayoutManager) : VirtualLayoutAdapter<ALvRecyclerAdapter.ALvRecyclerHolder>(layoutManager) {
    val icons = arrayOf(R.drawable.add,
            R.drawable.add_member_sel, R.drawable.add_member_seled, R.drawable.album)
    lateinit var mContext: Context

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_alv
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ALvRecyclerHolder {
        mContext = parent.context
        val view = LayoutInflater.from(parent.context).inflate(position, parent, false)
        return ALvRecyclerHolder(view)
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: ALvRecyclerHolder, position: Int) {
        val layoutParams = VirtualLayoutManager.LayoutParams(100,
                100)

        holder.textView.text = position.toString()
        when (position % 7) {
            1 -> {
                layoutParams.width = mContext.dip2Px(55)
                layoutParams.height = mContext.dip2Px(100)
            }
            2 -> {
                layoutParams.width = mContext.dip2Px(55)
                layoutParams.height = mContext.dip2Px(100)
            }
            0 -> {
                layoutParams.width = mContext.dip2Px(120)
                layoutParams.height = mContext.dip2Px(100)
            }
            3 -> {
                layoutParams.width = mContext.dip2Px(120)
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            4 -> {
                layoutParams.width = mContext.dip2Px(80)
                layoutParams.height = mContext.dip2Px(60)
            }
            5 -> {
                layoutParams.width = mContext.dip2Px(120)
                layoutParams.height = mContext.dip2Px(90)
            }
            6 -> {
                layoutParams.width = mContext.dip2Px(80)
                layoutParams.height = mContext.dip2Px(60)
            }
        }
        holder.itemView.layoutParams = layoutParams

        holder.imageView.setImageResource(icons[position % 4])
    }

    inner class ALvRecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.alvItemIv
        val textView = itemView.alvItemTv
    }
}