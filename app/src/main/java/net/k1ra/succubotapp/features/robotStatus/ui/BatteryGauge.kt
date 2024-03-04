package net.k1ra.succubotapp.features.robotStatus.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.k1ra.succubotapp.R

/**
 * Credit: https://github.com/pkleczko/CustomGauge
 */

class BatteryGauge : View {
    private var mPaint: Paint? = null

    @get:Suppress("unused")
    var strokeWidth = 0f

    @get:Suppress("unused")
    var strokeColor = 0
    private var mRect: RectF? = null
    private var mStrokeCap: String? = null

    @get:Suppress("unused")
    var startAngle = 0

    @get:Suppress("unused")
    var sweepAngle = 0

    @get:Suppress("unused")
    var startValue = 0
    private var mEndValue = 0
    private var mValue = 0
    private var mPointAngle = 0.0
    private var mPoint = 0

    @get:Suppress("unused")
    var pointSize = 0

    @get:Suppress("unused")
    var pointStartColor = 0

    @get:Suppress("unused")
    var pointEndColor = 0

    @get:Suppress("unused")
    var dividerColor = 0
    private var mDividerSize = 0
    private var mDividerStepAngle = 0
    private var mDividersCount = 0

    @get:Suppress("unused")
    var isDividerDrawFirst = false

    @get:Suppress("unused")
    var isDividerDrawLast = false

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomGauge, 0, 0)

        // stroke style
        strokeWidth = a.getDimension(R.styleable.CustomGauge_gaugeStrokeWidth, 10f)
        strokeColor = a.getColor(
            R.styleable.CustomGauge_gaugeStrokeColor,
            ContextCompat.getColor(context, R.color.black)
        )
        strokeCap = a.getString(R.styleable.CustomGauge_gaugeStrokeCap)

        mValue = a.getInt(R.styleable.CustomGauge_gaugeValue, 0)

        // angle start and sweep (opposite direction 0, 270, 180, 90)
        startAngle = a.getInt(R.styleable.CustomGauge_gaugeStartAngle, 0)
        sweepAngle = a.getInt(R.styleable.CustomGauge_gaugeSweepAngle, 360)

        // scale (from mStartValue to mEndValue)
        startValue = a.getInt(R.styleable.CustomGauge_gaugeStartValue, 0)
        endValue = a.getInt(R.styleable.CustomGauge_gaugeEndValue, 1000)

        // pointer size and color
        pointSize = a.getInt(R.styleable.CustomGauge_gaugePointSize, 0)
        pointStartColor = a.getColor(
            R.styleable.CustomGauge_gaugePointStartColor,
            ContextCompat.getColor(context, R.color.white)
        )
        pointEndColor = a.getColor(
            R.styleable.CustomGauge_gaugePointEndColor,
            ContextCompat.getColor(context, R.color.white)
        )

        // divider options
        val dividerSize = a.getInt(R.styleable.CustomGauge_gaugeDividerSize, 0)
        dividerColor = a.getColor(
            R.styleable.CustomGauge_gaugeDividerColor,
            ContextCompat.getColor(context, R.color.white)
        )
        val dividerStep = a.getInt(R.styleable.CustomGauge_gaugeDividerStep, 0)
        isDividerDrawFirst = a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawFirst, true)
        isDividerDrawLast = a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawLast, true)

        // calculating one point sweep
        mPointAngle = Math.abs(sweepAngle).toDouble() / (mEndValue - startValue)

        // calculating divider step
        if (dividerSize > 0) {
            mDividerSize = sweepAngle / (Math.abs(mEndValue - startValue) / dividerSize)
            mDividersCount = 100 / dividerStep
            mDividerStepAngle = sweepAngle / mDividersCount
        }
        a.recycle()
        init()
    }

    private fun init() {
        //main Paint
        mPaint = Paint()
        mPaint!!.color = strokeColor
        mPaint!!.strokeWidth = strokeWidth
        mPaint!!.isAntiAlias = true
        if (!TextUtils.isEmpty(mStrokeCap)) {
            if (mStrokeCap == "BUTT") mPaint!!.strokeCap =
                Paint.Cap.BUTT else if (mStrokeCap == "ROUND") mPaint!!.strokeCap =
                Paint.Cap.ROUND
        } else mPaint!!.strokeCap = Paint.Cap.BUTT
        mPaint!!.style = Paint.Style.STROKE
        mRect = RectF()
        mPoint = (startAngle + (mValue - startValue) * mPointAngle).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = strokeWidth
        val size = (if (width < height) width else height).toFloat()
        val width = size - 2 * padding
        val height = size - 2 * padding
        //        float radius = (width > height ? width/2 : height/2);
        val radius = if (width < height) width / 2 else height / 2
        val rectLeft = (getWidth() - 2 * padding) / 2 - radius + padding
        val rectTop = (getHeight() - 2 * padding) / 2 - radius + padding
        val rectRight = (getWidth() - 2 * padding) / 2 - radius + padding + width
        val rectBottom = (getHeight() - 2 * padding) / 2 - radius + padding + height
        mRect!![rectLeft, rectTop, rectRight] = rectBottom
        mPaint!!.color = strokeColor
        mPaint!!.setShader(null)
        canvas.drawArc(mRect!!, startAngle.toFloat(), sweepAngle.toFloat(), false, mPaint!!)
        mPaint!!.color = pointStartColor
        mPaint!!.setShader(
            LinearGradient(
                getWidth().toFloat(), getHeight().toFloat(), 0f, 0f,
                pointEndColor,
                pointStartColor, Shader.TileMode.CLAMP
            )
        )
        if (pointSize > 0) { //if size of pointer is defined
            if (mPoint > startAngle + pointSize / 2) {
                canvas.drawArc(
                    mRect!!, (mPoint - pointSize / 2).toFloat(), pointSize.toFloat(), false,
                    mPaint!!
                )
            } else { //to avoid excedding start/zero point
                canvas.drawArc(mRect!!, mPoint.toFloat(), pointSize.toFloat(), false, mPaint!!)
            }
        } else { //draw from start point to value point (long pointer)
            if (mValue == startValue) //use non-zero default value for start point (to avoid lack of pointer for start/zero value)
                canvas.drawArc(
                    mRect!!, startAngle.toFloat(), DEFAULT_LONG_POINTER_SIZE.toFloat(), false,
                    mPaint!!
                ) else canvas.drawArc(
                mRect!!, startAngle.toFloat(), (mPoint - startAngle).toFloat(), false,
                mPaint!!
            )
        }
        if (mDividerSize > 0) {
            mPaint!!.color = dividerColor
            mPaint!!.setShader(null)
            var i = if (isDividerDrawFirst) 0 else 1
            val max = if (isDividerDrawLast) mDividersCount + 1 else mDividersCount
            while (i < max) {
                canvas.drawArc(
                    mRect!!,
                    (startAngle + i * mDividerStepAngle).toFloat(),
                    mDividerSize.toFloat(),
                    false,
                    mPaint!!
                )
                i++
            }
        }
    }

    var value: Int
        get() = mValue
        set(value) {
            mValue = value
            mPoint = (startAngle + (mValue - startValue) * mPointAngle).toInt()
            invalidate()
        }

    @get:Suppress("unused")
    var strokeCap: String?
        get() = mStrokeCap
        set(strokeCap) {
            mStrokeCap = strokeCap
            if (mPaint != null) {
                if (mStrokeCap == "BUTT") {
                    mPaint!!.strokeCap = Paint.Cap.BUTT
                } else if (mStrokeCap == "ROUND") {
                    mPaint!!.strokeCap = Paint.Cap.ROUND
                }
            }
        }

    @get:Suppress("unused")
    var endValue: Int
        get() = mEndValue
        set(endValue) {
            mEndValue = endValue
            mPointAngle = Math.abs(sweepAngle).toDouble() / (mEndValue - startValue)
            invalidate()
        }

    fun setDividerStep(dividerStep: Int) {
        if (dividerStep > 0) {
            mDividersCount = 100 / dividerStep
            mDividerStepAngle = sweepAngle / mDividersCount
        }
    }

    fun setDividerSize(dividerSize: Int) {
        if (dividerSize > 0) {
            mDividerSize = sweepAngle / (Math.abs(mEndValue - startValue) / dividerSize)
        }
    }

    companion object {
        private const val DEFAULT_LONG_POINTER_SIZE = 1
    }
}