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


class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

       private var drawPath: CustomPath? = null
       private var drawPaint: Paint? = null
       private var canvasPaint: Paint? = null
       private var canvasBitmap: Bitmap? = null
       private var brushSize: Float = 0.toFloat()
       private var color = Color.WHITE
       private var canvas: Canvas? = null
       private var paths = ArrayList<CustomPath>()
       private var undoPaths = ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }

    fun onClickUndo(){
        if(paths.size > 0) {
            undoPaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun onClickRedo(){
        if(undoPaths.isNotEmpty()) {
            paths.add(undoPaths.removeAt(undoPaths.size - 1))
            invalidate()
        }
    }

    private fun setUpDrawing(){   // We need this to call all variables that we prepared

        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {  // We need this to set up canvas
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {  // We need this to draw on the screen
        super.onDraw(canvas)
        canvas?.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint!!)

        for(path in paths){
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas?.drawPath(path, drawPaint!!)
        }

        if(!drawPath!!.isEmpty){
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas?.drawPath(drawPath!!, drawPaint!!)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {  // We need this to even print anything

        Log.d("DrawingView", "onTouchEvent: $event")
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = color
                drawPath!!.brushThickness = brushSize

                drawPath!!.reset()
                if(touchX != null){
                    if(touchY != null)
                        drawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if(touchX != null){
                    if(touchY != null)
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

    fun setBrushSize(size: Float){
            brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics)
            drawPaint!!.strokeWidth = brushSize
    }

    fun setColor(newColor: String){
            color = Color.parseColor(newColor)
            drawPaint!!.color = color
    }


   internal inner class CustomPath(var color: Int, var brushThickness: Float): Path(){

    }
}