package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var currentColor: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setBrushSize(20.toFloat())

        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_colors)

        currentColor = linearLayout[1] as ImageButton
        currentColor!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.selected_color_palette)
        )

        val brushPaintIcon = findViewById<ImageButton>(R.id.brushSize)
        brushPaintIcon.setOnClickListener {
            showDialogBrushSize()
        }

    }

    private fun showDialogBrushSize() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.image_small)
        smallBtn.setOnClickListener {
            drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.image_medium)
        mediumBtn.setOnClickListener {
            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.image_large)
        largeBtn.setOnClickListener {
            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun colorClicked(view: View) {
        if (view !== currentColor) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.selected_color_palette)
            )
            currentColor?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_palette)
            )
            currentColor = view
        }
    }
}