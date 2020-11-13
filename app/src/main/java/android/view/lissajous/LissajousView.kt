package android.view.lissajous

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

class LissajousView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var noOfCircles = 5
        set(value) {
            if (value < 4 || value > 6) {
                return
            }
            field = value
            arr = generatePathArrays()
            cancelAnimator()
            resetPaths()
            calculateCircleRadius()
            valueAnimator.start()
            invalidate()
        }

    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
    }

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 100
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
        pathEffect = DashPathEffect(
            floatArrayOf(8.0f, 8.0f),
            200.0f
        )
    }

    private var theta = 0.0f

    private var circleRadius = 0.0f

    private var isMoveTo = true

    private val padding = 30.0f

    private val valueAnimator = ValueAnimator.ofFloat(360.0f, 0.0f).apply {
        duration = 15000L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()
        addUpdateListener {
            theta = it.animatedValue as Float
            generatePath()
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                super.onAnimationRepeat(animation)
                resetPaths()
            }
        })
    }

    private var arr = generatePathArrays()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateCircleRadius()
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawCircles(it)
            drawPaths(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnimator()
    }

    private fun cancelAnimator() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
    }

    private fun calculateCircleRadius() {
        circleRadius = ((width - noOfCircles * padding) / (noOfCircles + 1)) / 2.0f
    }

    private fun resetPaths() {
        for (j in 0 until noOfCircles) {
            for (i in 0 until noOfCircles) {
                arr[j][i].reset()
            }
        }
        isMoveTo = true
    }

    private fun generatePathArrays() = Array(noOfCircles) {
        Array(noOfCircles) {
            Path()
        }
    }

    private fun generatePath() {
        var yy = circleRadius * 3.0f + padding
        for (j in 0 until noOfCircles) {
            val cY =
                yy + ((circleRadius) * sin(Math.toRadians((theta * (j + 1)).toDouble())).toFloat())
            var xx = circleRadius * 3.0f + padding
            for (i in 0 until noOfCircles) {
                val cX =
                    xx + ((circleRadius) * cos(Math.toRadians((theta * (i + 1)).toDouble())).toFloat())

                xx += circleRadius * 2 + padding
                if (isMoveTo) {
                    arr[j][i].moveTo(cX, cY)
                } else {
                    arr[j][i].lineTo(cX, cY)
                }
            }
            yy += circleRadius * 2 + padding
        }
        if (isMoveTo) {
            isMoveTo = false
        }
    }

    private fun drawCircles(canvas: Canvas) {
        var xx = circleRadius * 3.0f + padding
        for (i in 0 until noOfCircles) {
            canvas.drawCircle(xx, circleRadius, circleRadius, circlePaint)
            val cX =
                xx + ((circleRadius) * cos(Math.toRadians((theta * (i + 1)).toDouble())).toFloat())
            val cY =
                circleRadius + ((circleRadius) * sin(Math.toRadians((theta * (i + 1)).toDouble())).toFloat())

            xx += circleRadius * 2 + padding
            canvas.drawCircle(cX, cY, 8.0f, pointPaint)
            canvas.drawLine(cX, cY, cX, width.toFloat(), linePaint)
        }
        var yy = circleRadius * 3.0f + padding
        for (i in 0 until noOfCircles) {
            canvas.drawCircle(circleRadius, yy, circleRadius, circlePaint)
            val cX =
                circleRadius + ((circleRadius) * cos(Math.toRadians((theta * (i + 1)).toDouble())).toFloat())
            val cY =
                yy + ((circleRadius) * sin(Math.toRadians((theta * (i + 1)).toDouble())).toFloat())
            canvas.drawCircle(cX, cY, 8.0f, pointPaint)
            canvas.drawLine(cX, cY, width.toFloat(), cY, linePaint)
            yy += circleRadius * 2 + padding
        }
    }

    private fun drawPaths(canvas: Canvas) {
        for (j in 0 until noOfCircles) {
            for (i in 0 until noOfCircles) {
                canvas.drawPath(arr[j][i], pathPaint)
            }
        }
    }
}