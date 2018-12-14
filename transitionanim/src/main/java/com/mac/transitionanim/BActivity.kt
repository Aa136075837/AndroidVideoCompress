package com.mac.transitionanim

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_b.*

class BActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_b)
        ViewCompat.setTransitionName(mBPhoto, "image")
        window.enterTransition = Fade()
        window.exitTransition = Fade()
        val transitionSet = TransitionSet()
        transitionSet.addTransition(ChangeBounds())
        transitionSet.addTransition(ChangeTransform())
        transitionSet.addTarget(mBPhoto)
        Glide.with(this).load(intent.getStringExtra("path")).into(mBPhoto)
        window.sharedElementEnterTransition = transitionSet
        window.sharedElementExitTransition = transitionSet
    }
}
