package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var drawPath: CustomPath? = null         //this variable stores the current path
    private var drawPaint: Paint? =
        null             //this variable defines paint props for drawing on canvas like stroke, color and style
    private var canvasPaint: Paint? = null           //instance of canvas paint view
    private var canvasBitmap: Bitmap? = null         //instance of the Bitmap
    private var brushSize: Float = 0.toFloat()       //brush size
    private var color = Color.WHITE                  //color

    /** A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host
     * the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect,
     * Path, text, Bitmap), and a paint (to describe the colors and styles for the
     * drawing)
     */
    private var canvas: Canvas? = null
    private var paths = ArrayList<CustomPath>()
    private var undoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    fun onClickUndo() {
        if (paths.size > 0) {
            undoPaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun onClickRedo() {
        if (undoPaths.isNotEmpty()) {
            paths.add(undoPaths.removeAt(undoPaths.size - 1))
            invalidate()
        }
    }

    /**
     * This method initializes the attributes of the
     * ViewForDrawing class.
     */
    private fun setUpDrawing() {

        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)

    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {  // We need this to set up canvas
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    /**
     * Draw the specified bitmap, with its top/left corner at (x,y), using the specified paint,
     * transformed by the current matrix.
     *
     *If the bitmap and canvas have different densities, this function will take care of
     * automatically scaling the bitmap to draw at the same density as the canvas.

     */
    override fun onDraw(canvas: Canvas?) {  // We need this to draw on the screen
        super.onDraw(canvas)
        canvas?.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint!!)

        for (path in paths) {
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas?.drawPath(path, drawPaint!!)
        }

        if (!drawPath!!.isEmpty) {
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas?.drawPath(drawPath!!, drawPaint!!)
        }
    }

    /**
     * This method acts as an event listener when a touch
     * event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        Log.d("DrawingView", "onTouchEvent: $event")
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = color
                drawPath!!.brushThickness = brushSize

                drawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null)
                        drawPath!!.moveTo(
                            touchX,
                            touchY
                        ) // Set the beginning of the next contour to the point (x,y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null)
                        drawPath!!.lineTo(touchX, touchY)
                }
            }

            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)
                drawPath = CustomPath(color, brushSize)
            }

            else -> return false
        }

        invalidate()

        return true
    }

    /**
     * This method is called when either the brush or the eraser
     * sizes are to be changed. This method sets the brush/eraser
     * sizes to the new values depending on user selection.
     */
    fun setBrushSize(size: Float) {
        brushSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize
    }

    /**
     * This function is called when the user desires a color change.
     * This functions sets the color of a store to selected color and able to draw on view using that color.
     */
    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        drawPaint!!.color = color
    }


    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}