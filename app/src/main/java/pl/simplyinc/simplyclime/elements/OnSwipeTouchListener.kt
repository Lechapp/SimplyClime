package pl.simplyinc.simplyclime.elements

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.GestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import com.github.mikephil.charting.charts.CombinedChart
import java.lang.Math.abs
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity


class OnSwipeTouchListener(private val ctx: Context, private val mChart: CombinedChart?, private val swipeIcon:ImageView?,
                           val offset:Float, private val act:MainActivity? = null) : OnTouchListener {

    private val gestureDetector: GestureDetector
    private var swipetutorial = true
    private var busy = false

    init {
        if(SessionPref(ctx).getPref("swipetutorial") == "false" && swipeIcon != null)
            swipetutorial = false
        else {
            val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadein)
            aniFade.duration = 3000
            swipeIcon?.visibility = View.VISIBLE
            swipeIcon?.startAnimation(aniFade)
        }
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if(act == null) {
                                onSwipeRight()
                            }else onSwipeRightChangePosition()
                        } else {
                            if(act == null)
                                onSwipeLeft()
                            else onSwipeLeftChangePosition()
                        }
                        result = true
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }


    }
    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
    var xstatus = offset-0.1f
    fun onSwipeLeft() {
        if(!busy) {
            busy = true
            if (swipetutorial) {
                swipetutorial = false
                val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadeout)
                aniFade.duration = 1500
                aniFade.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        swipeIcon?.visibility = View.GONE
                    }
                })
                swipeIcon?.startAnimation(aniFade)
                SessionPref(ctx).setPref("swipetutorial", "false")
            }
            if (xstatus < 36f)
                xstatus += 12f
            else busy = false

            mChart?.moveViewToAnimated(xstatus, 0f, mChart.axisLeft.axisDependency, 1000)
            Handler().postDelayed({
                busy = false
            }, 1000)
        }
    }
    fun onSwipeBottom() {}
    fun onSwipeTop() {}
    fun onSwipeRight() {
        if(!busy) {
            busy = true
            if (swipetutorial) {
                swipetutorial = false
                val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadeout)
                aniFade.duration = 1500
                aniFade.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        swipeIcon?.visibility = View.GONE
                    }
                })
                swipeIcon?.startAnimation(aniFade)
                SessionPref(ctx).setPref("swipetutorial", "false")
            }

            if (xstatus > 0f)
                xstatus -= 12f
            else busy = false
            mChart?.moveViewToAnimated(xstatus, 0f, mChart.axisLeft.axisDependency, 1000)
            Handler().postDelayed({
                busy = false
            }, 1000)
        }
    }

    fun onSwipeRightChangePosition(){
        val pos = act?.position!!
        if(pos >= 1) {
            act.position = pos - 1
            act.setWeather(act.position)
        }
    }

    fun onSwipeLeftChangePosition(){
        val pos = act?.position!!

        if(pos <= offset) {
            act.position = pos + 1
            act.setWeather(act.position)
        }
    }
}