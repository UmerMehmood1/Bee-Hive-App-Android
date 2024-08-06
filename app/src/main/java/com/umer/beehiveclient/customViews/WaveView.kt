package com.umer.beehiveclient.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DrawFilter
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


/**
 * Created by Administrator on 2016/11/30.
 */
class WaveView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    //初始化路径
    private val mAbovePath = Path()
    private val mBelowWavePath = Path()

    //初始化画笔
    private val mAboveWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBelowWavePaint: Paint

    private val mDrawFilter: DrawFilter

    private var φ = 0f

    private var mWaveAnimationListener: OnWaveAnimationListener? = null

    init {
        mAboveWavePaint.isAntiAlias = true
        mAboveWavePaint.style = Paint.Style.FILL
        mAboveWavePaint.color = Color.WHITE

        mBelowWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBelowWavePaint.isAntiAlias = true
        mBelowWavePaint.style = Paint.Style.FILL
        mBelowWavePaint.color = Color.WHITE
        mBelowWavePaint.alpha = 80
        //画布抗锯齿
        mDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawFilter = mDrawFilter

        mAbovePath.reset()
        mBelowWavePath.reset()

        φ -= 0.1f

        var y: Float
        var y2: Float

        val ω = 2 * Math.PI / width

        mAbovePath.moveTo(left.toFloat(), bottom.toFloat())
        mBelowWavePath.moveTo(left.toFloat(), bottom.toFloat())
        var x = 0f
        while (x <= width) {
            y = (8 * cos(ω * x + φ) + 8).toFloat()
            y2 = (8 * sin(ω * x + φ)).toFloat()
            mAbovePath.lineTo(x, y)
            mBelowWavePath.lineTo(x, y2)
            mWaveAnimationListener?.OnWaveAnimation(y)
            x += 20f
        }
        mAbovePath.lineTo(right.toFloat(), bottom.toFloat())
        mBelowWavePath.lineTo(right.toFloat(), bottom.toFloat())

        canvas.drawPath(mAbovePath, mAboveWavePaint)
        canvas.drawPath(mBelowWavePath, mBelowWavePaint)

        postInvalidateDelayed(20)
    }

    fun setOnWaveAnimationListener(l: OnWaveAnimationListener?) {
        this.mWaveAnimationListener = l
    }

    interface OnWaveAnimationListener {
        fun OnWaveAnimation(y: Float)
    }
}

