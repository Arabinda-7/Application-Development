package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class IdeaMindMapDialog(context: Context, private val note: Note) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
        }

        val btnClose = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            setPadding(24, 24, 24, 24)
            setOnClickListener { dismiss() }
        }

        val mindMapView = MindMapView(context, note)
        root.addView(mindMapView)
        root.addView(btnClose, FrameLayout.LayoutParams(120, 120))

        setContentView(root)
    }

    private class MindMapView(context: Context, val note: Note) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 4f
            alpha = 100
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val centerX = width / 2f
            val centerY = height / 2f

            // Draw connections
            val features = note.subFeatures
            if (features.isNotEmpty()) {
                val radius = Math.min(width, height) * 0.35f
                features.forEachIndexed { index, feature ->
                    val angle = (index.toFloat() / features.size) * 2 * Math.PI
                    val x = centerX + radius * Math.cos(angle).toFloat()
                    val y = centerY + radius * Math.sin(angle).toFloat()
                    canvas.drawLine(centerX, centerY, x, y, linePaint)
                    
                    // Draw node
                    paint.color = if (feature.isCompleted) Color.GRAY else Color.parseColor("#1A73E8")
                    canvas.drawCircle(x, y, 40f, paint)
                    
                    paint.color = Color.WHITE
                    paint.textSize = 24f
                    canvas.drawText(feature.name.take(10), x, y + 70f, paint)
                }
            }

            // Draw center node
            paint.color = if (note.vibeColor != -1) note.vibeColor else Color.parseColor("#FFB800")
            canvas.drawCircle(centerX, centerY, 80f, paint)
            
            paint.color = Color.BLACK
            paint.textSize = 32f
            paint.isFakeBoldText = true
            canvas.drawText(note.title.take(12), centerX, centerY + 10f, paint)
        }
    }
}