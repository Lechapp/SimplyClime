package pl.simplyinc.simplyclime.elements

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.animation.ObjectAnimator
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Color.DKGRAY
import android.graphics.Color.GRAY
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import pl.simplyinc.simplyclime.R
import kotlin.math.min


class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var strokeWidth = 4f
    private var progress = 0f
    private var min = 0
    private var max = 100
    private var startAngle = (-180).toFloat()
    private var color = GRAY
    private var rectF: RectF? = null
    private var backgroundPaint: Paint? = null
    private var foregroundPaint: Paint? = null

    fun setType(type:String){
        when(type){
            "day" -> {
                foregroundPaint!!.color = ContextCompat.getColor(context,R.color.sunrise)
            }
            "night" -> {
                this.startAngle = 0.toFloat()
                foregroundPaint!!.color = ContextCompat.getColor(context,R.color.moon)
            }
        }
        invalidate()

    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    init{
        rectF = RectF()
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, 0, 0)
        try {

            strokeWidth = typedArray.getDimension(R.styleable.CircleProgressBar_progressBarThickness, strokeWidth)
            progress = typedArray.getFloat(R.styleable.CircleProgressBar_progress, progress)
            color = typedArray.getInt(R.styleable.CircleProgressBar_progressbarColor, color)
            min = typedArray.getInt(R.styleable.CircleProgressBar_min, min)
            max = typedArray.getInt(R.styleable.CircleProgressBar_max, max)

        } finally {
            typedArray.recycle()
        }

        backgroundPaint = Paint(ANTI_ALIAS_FLAG)
        backgroundPaint!!.color = adjustAlpha(color, 0.3f)
        backgroundPaint!!.style = Paint.Style.STROKE
        backgroundPaint!!.strokeWidth = strokeWidth

        foregroundPaint = Paint(ANTI_ALIAS_FLAG)
        foregroundPaint!!.color = color
        foregroundPaint!!.style = Paint.Style.STROKE
        foregroundPaint!!.strokeWidth = strokeWidth

    }


    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        canvas.drawOval(rectF!!, backgroundPaint!!)
        val angle = 360 * progress / max
        canvas.drawArc(rectF!!, startAngle, angle, false, foregroundPaint!!)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)

        setMeasuredDimension(min, min)

        rectF!!.set(0 + strokeWidth / 2, 0 + strokeWidth / 2, min - strokeWidth / 2, min - strokeWidth / 2)

    }


    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        return Color.argb(alpha, red, green, blue)
    }

    fun setProgressWithAnimation(progress: Float) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress)
        objectAnimator.duration = 1500
        objectAnimator.interpolator = DecelerateInterpolator()
        objectAnimator.start()
    }

}