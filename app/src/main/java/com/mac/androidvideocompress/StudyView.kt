package com.mac.androidvideocompress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class StudyView : View {
    lateinit var mPaint: Paint

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
//        setBackgroundColor(context?.resources?.getColor(R.color.colorAccent)!!)
        mPaint = Paint()

        mPaint.strokeWidth = 1F
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        Log.e("StudyView", "  event?.x=${event?.x}   event?.y=${event?.y}")
        Log.e("StudyView", "  event?.rawX=${event?.rawX}   event?.rawY=${event?.rawY}")
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

    override fun onDraw(canvas: Canvas?) {
        mPaint.textSize = 30F
        mPaint.color = Color.GREEN
        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), mPaint)
        canvas?.save()
        mPaint.color = Color.BLACK
        canvas?.drawText("12345", 0, "12345".length, width / 2F, height / 2F, mPaint)
        canvas?.clipRect(20F, 20F, width.toFloat() / 2, height.toFloat() / 2)
        canvas?.restore()
        Log.e("StudyView", "  width=$width   height=$height")
        Log.e("StudyView", "  left=$left   top=$top    right=$right    bottom=$bottom")
        Log.e("StudyView", "  x=$x   y=$y ")
    }
}