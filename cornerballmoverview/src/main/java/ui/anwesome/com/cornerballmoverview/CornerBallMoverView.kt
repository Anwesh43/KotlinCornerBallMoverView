package ui.anwesome.com.cornerballmoverview

/**
 * Created by anweshmishra on 07/04/18.
 */

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class CornerBallMoverView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw (canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent (event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State (var dir : Float = 0f, var prevScale : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f, 0f)

        fun update (stopcb : () -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                dir = 0f
                if (j == scales.size) {
                    stopcb()
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f && prevScale == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator (var view : View, var animated : Boolean = false) {

        fun animate (updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                }
                catch (ex : Exception) {

                }
            }
        }

        fun start () {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CornerBall(var i : Int, val state : State = State()) {
        fun draw(canvas : Canvas, paint : Paint) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val r : Float = Math.min(w, h)/10
            for (i in 0..1) {
                val ox : Float = i * (w + 2 * r) - (r+r/10)
                canvas.save()
                canvas.translate(ox + (w/2 - ox) * state.scales[0], h - (h/2 * state.scales[0] + h/2 * state.scales[2]))
                canvas.scale(state.scales[1], state.scales[1])
                canvas.drawCircle(0f, 0f, r, paint)
                canvas.restore()
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun update(stopcb : () -> Unit) {
            state.update(stopcb)
        }
    }

    data class CornerBallContainer (var i : Int) {

        val atomicId : AtomicInteger = AtomicInteger(0)

        val cornerBalls : ConcurrentLinkedQueue<CornerBall> = ConcurrentLinkedQueue()

        fun draw(canvas : Canvas, paint : Paint) {
            paint.color = Color.parseColor("#f44336")
            cornerBalls.forEach {
                it.draw(canvas, paint)
            }
        }

        fun update (stopcb : () -> Unit) {
            cornerBalls.forEach {
                it.update {
                    cornerBalls.remove(it)
                    if (cornerBalls.size == 0) {
                        stopcb()
                    }
                }
            }
        }

        fun startUpdating (startcb : () -> Unit) {
            val cornerBall : CornerBall = CornerBall(atomicId.incrementAndGet())
            cornerBalls.add(cornerBall)
            cornerBall.startUpdating {
                if (cornerBalls.size == 1) {
                    startcb()
                }
            }
        }
    }

    data class Renderer (var view : CornerBallMoverView) {

        val cornerBallContainer = CornerBallContainer(0)

        val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            cornerBallContainer.draw(canvas, paint)
            animator.animate {
                cornerBallContainer.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cornerBallContainer.startUpdating {
                animator.start()
            }
        }
    }

}