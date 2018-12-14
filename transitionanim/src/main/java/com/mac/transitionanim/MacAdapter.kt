package com.mac.transitionanim

import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_recycler_a.view.*

class MacAdapter(val activity: FragmentActivity, val adapterData: ArrayList<String>) : RecyclerView.Adapter<MacAdapter.MacViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MacViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_a, parent, false)
        return MacViewHolder(view)
    }

    override fun getItemCount(): Int {
        return adapterData.size
    }

    override fun onBindViewHolder(holder: MacViewHolder, position: Int) {
        Glide.with(activity).load(adapterData[position]).into(holder.imageView)
        holder.imageView.setOnClickListener {
            ViewCompat.setTransitionName(holder.imageView, "image")
            val intent = Intent(activity, BActivity::class.java)
            intent.putExtra("path", adapterData[position])
            val pair1 = Pair<View, String>(holder.imageView, ViewCompat.getTransitionName(holder.imageView).toString())
            val makeSceneTransitionAnimation = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pair1)
            ActivityCompat.startActivity(activity, intent, makeSceneTransitionAnimation.toBundle())
        }
    }

    fun setData(imagePath: ArrayList<String>) {
        adapterData.clear()
        adapterData.addAll(imagePath)
    }

    class MacViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.item_iv
    }

}