package com.example.allinone

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

abstract class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false
        val diffY = e2.y - e1.y
        val diffX = e2.x - e1.x
        if (abs(diffX) > abs(diffY)) {
            if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                if (diffX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
                return true
            }
        }
        return false
    }

    abstract fun onSwipeLeft()
    abstract fun onSwipeRight()
}
