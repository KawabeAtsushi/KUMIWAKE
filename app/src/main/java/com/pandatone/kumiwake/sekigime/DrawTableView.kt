package com.pandatone.kumiwake.sekigime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import java.util.*

/**
 * Created by atsushi_2 on 2016/07/15.
 */
class DrawTableView(context: Context) : View(context) {
    private var squareNo = SekigimeResult.square_no
    private var normalMode: Boolean? = StatusHolder.normalMode
    private var doubleDeploy: Boolean? = SekigimeResult.doubleDeploy
    private var arrayArrayQuick = SekigimeResult.arrayArrayQuick
    private var arrayArrayNormal = SekigimeResult.arrayArrayNormal
    private var xCoordinate = 0f
    private var yCoordinate = 0f
    private var scale: Float = 0.toFloat()

    private var canvasHeight: Float = 0.toFloat()
    private var r: Float = 0.toFloat()
    private var lastX = 0
    private var lastY = 0
    private var seatsNo: Int = 0
    private var a: Int = 0
    private var dispWidth: Int = 0
    private lateinit var x: ArrayList<Double>
    private lateinit var y: ArrayList<Double>

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        memberNo(position)
        scale = resources.displayMetrics.density / 2
        dispWidth = MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        when (tableType) {
            "square" -> height = (seatsNo - squareNo * 2) * 68 + 450
            "parallel" -> height = seatsNo * 68 + 250
            "circle" -> height = 810
            "counter" -> height = seatsNo * 132 + 100
        }
        setMeasuredDimension(dispWidth, (height * scale).toInt())
    }


    override fun onDraw(canvas: Canvas) {
        arrayArrayQuick = SekigimeResult.arrayArrayQuick
        arrayArrayNormal = SekigimeResult.arrayArrayNormal
        x = ArrayList()
        y = ArrayList()
        memberNo(position)
        canvasHeight = height.toFloat()
        when (tableType) {
            "square" -> drawSquareTable(canvas)
            "parallel" -> drawParallelTable(canvas)
            "circle" -> drawCircleTable(canvas)
            "counter" -> drawCounterTable(canvas)
        }

        if (tableType != "square") {
            setFocusGradation(canvas)
            setFmBackground(canvas, 0, seatsNo)
            drawMemberName(canvas, tableType)
        }
    }

    fun reDraw() {
        // 再描画
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            parent.requestDisallowInterceptTouchEvent(true)
        } else if (action == MotionEvent.ACTION_MOVE) {
            parent.requestDisallowInterceptTouchEvent(false)
        }

        if (Math.abs(event.x - xCoordinate) > 1 && Math.abs(event.y - yCoordinate) > 1) {

            xCoordinate = event.x
            yCoordinate = event.y
            val xArray = x
            val yArray = y
            var x: Double?
            var y: Double?

            for (i in xArray.indices) {
                x = xArray[i]
                if (Math.abs(xCoordinate - x) <= r) {
                    for (j in yArray.indices) {
                        y = yArray[j]
                        if (Math.abs(yCoordinate - y) <= r && j == i) {
                            point = j
                            reDraw()
                        }
                    }
                }
            }
        }

        return true
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル１//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private fun drawSquareTable(canvas: Canvas) {
        val cPaint = Paint()
        val sPaint = Paint()
        val tableHeight: Int = if (doubleDeploy!!) {
            if ((seatsNo - squareNo * 2) % 2 == 0) {
                130 * (seatsNo - squareNo * 2) / 2 + 235
            } else {
                130 * (seatsNo - squareNo * 2 + 1) / 2 + 235
            }
        } else {
            if ((seatsNo - squareNo) % 2 == 0) {
                130 * (seatsNo - squareNo) / 2 + 235
            } else {
                130 * (seatsNo - squareNo + 1) / 2 + 235
            }
        }

        // 机
        cPaint.color = Color.parseColor("#000000")
        cPaint.strokeWidth = 10 * scale
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        val rectRight = dispWidth - 180 * scale
        canvas.drawRect(180 * scale, 230 * scale, rectRight, tableHeight * scale, cPaint)
        cPaint.color = Color.parseColor("#90ffffff")
        cPaint.style = Paint.Style.FILL
        canvas.drawRect(180 * scale, 230 * scale, rectRight, tableHeight * scale, cPaint)

        //椅子
        sPaint.color = Color.parseColor("#000000")
        if (squareNo > 5) {
            sPaint.strokeWidth = 3 * scale
        } else {
            sPaint.strokeWidth = (8 - squareNo) * scale
        }
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = if (squareNo > 10) {
            23 * scale
        } else {
            (50 - 3 * (squareNo - 1)) * scale
        }
        var transX = 0
        val transY = 130
        if (squareNo != 0) {
            transX = (rectRight - 110 * scale).toInt() / (squareNo + 1)
        }

        //間隔
        a = 0
        var b = 0
        var i = 0
        var j = 0
        while (a < squareNo) {
            canvas.drawCircle(transX + 150 * scale, 155 * scale, r, sPaint)
            canvas.translate(transX.toFloat(), 0f)
            x.add(transX.toDouble() * (a + 1) + 150 * scale)
            y.add(155.toDouble() * scale)
            a++
        }

        canvas.translate((-transX * a).toFloat(), 0f)
        if (doubleDeploy!!) {
            val initialY = ((tableHeight + 75) * scale).toInt()
            while (a < squareNo * 2) {
                canvas.drawCircle(transX + 150 * scale, initialY.toFloat(), r, sPaint)
                canvas.translate(transX.toFloat(), 0f)
                x.add(transX.toDouble() * (b + 1) + 150 * scale)
                y.add(initialY.toDouble())
                a++
                b++
            }
        }

        canvas.translate((-transX * b).toFloat(), 0f)
        lastX = 0
        lastY = 0
        if (point < squareNo) {
            setFocusGradation(canvas)
        }
        setFmBackground(canvas, 0, squareNo)
        if (doubleDeploy!!) {
            if (point in squareNo until a) {
                setFocusGradation(canvas)
            }
            setFmBackground(canvas, squareNo, a)
        }

        r = 50 * scale
        sPaint.strokeWidth = 7 * scale
        while (i < (seatsNo - a) / 2) {
            canvas.drawCircle(100 * scale, 300 * scale, r, sPaint)
            canvas.translate(0f, transY * scale)
            x.add(100.toDouble() * scale)
            y.add((transY * i + 300).toDouble() * scale)
            i++
        }
        canvas.translate(0f, -transY * i * scale)
        while (i < seatsNo - a) {
            canvas.drawCircle(rectRight + 80 * scale, 300 * scale, r, sPaint)
            canvas.translate(0f, transY * scale)
            x.add(rectRight.toDouble() + 80 * scale)
            y.add((transY * j + 300).toDouble() * scale)
            i++
            j++
        }
        lastX = 0
        lastY = when {
            seatsNo == 1 -> (transY * scale).toInt()
            (seatsNo - a) / 2 == 0 -> (transY * Math.floor(((seatsNo - a) / 2).toDouble()) * scale).toInt()
            else -> (transY * Math.floor(((seatsNo - a + 1) / 2).toDouble()) * scale).toInt()
        }
        if (point >= a) {
            setFocusGradation(canvas)
        }
        setFmBackground(canvas, a, seatsNo)
        drawMemberName(canvas, tableType)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル２/////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawParallelTable(canvas: Canvas) {

        val cPaint = Paint()
        val sPaint = Paint()
        val tableHeight = ((130 * Math.round(((seatsNo + 1) / 2).toFloat()) + 150) * scale).toInt()

        // 机
        cPaint.color = Color.parseColor("#000000")
        cPaint.strokeWidth = 10 * scale
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        val rectRight = dispWidth - 180 * scale
        canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight.toFloat(), cPaint)
        cPaint.color = Color.parseColor("#90ffffff")
        cPaint.style = Paint.Style.FILL
        canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight.toFloat(), cPaint)

        //椅子
        sPaint.color = Color.parseColor("#000000")
        sPaint.strokeWidth = 7 * scale
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = 50 * scale
        val transY = 130

        //間隔
        var i = 0
        var j = 0
        while (i < seatsNo / 2) {
            canvas.drawCircle(100 * scale, 190 * scale, r, sPaint)
            canvas.translate(0f, transY * scale)
            x.add(100.toDouble() * scale)
            y.add((transY * i + 190).toDouble() * scale)
            i++
        }
        canvas.translate(0f, -transY * i * scale)
        while (i < seatsNo) {
            canvas.drawCircle(rectRight + 80 * scale, 190 * scale, r, sPaint)
            canvas.translate(0f, transY * scale)
            x.add(rectRight.toDouble() + 80 * scale)
            y.add((transY * j + 190).toDouble() * scale)
            i++
            j++
        }
        lastX = 0
        lastY = (transY * Math.floor((seatsNo / 2).toDouble()) * scale).toInt()
        if (seatsNo == 1) {
            lastY = (transY * scale).toInt()
        } else if (y[0] - lastY != 190.0) {
            lastY = (transY * Math.floor(((seatsNo + 1) / 2).toDouble()) * scale).toInt()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////丸テーブル///////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawCircleTable(canvas: Canvas) {
        val cPaint = Paint()
        val sPaint = Paint()
        val centerY: Float
        val centerX: Float = (dispWidth / 2).toFloat()

        // 円
        cPaint.color = Color.parseColor("#000000")
        cPaint.strokeWidth = 10 * scale
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        // (x,y,r,paint) 中心座標(x,y), 半径r
        canvas.drawCircle(centerX, 430 * scale, (centerX * 0.625).toFloat(), cPaint)
        cPaint.color = Color.parseColor("#90ffffff")
        cPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, 430 * scale, (centerX * 0.625).toFloat(), cPaint)

        //椅子
        sPaint.color = Color.parseColor("#000000")
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        //回転軸(x,y)
        canvas.translate(centerX, 430 * scale)
        lastX = centerX.toInt()
        lastY = (430 * scale).toInt()
        when {
            seatsNo < 16 -> {
                r = 50 * scale
                centerY = (-(centerX * 0.625) - 80 * scale).toFloat()
                sPaint.strokeWidth = 7 * scale
            }
            seatsNo < 21 -> {
                r = 40 * scale
                centerY = (-(centerX * 0.625) - 60 * scale).toFloat()
                sPaint.strokeWidth = 5 * scale
            }
            else -> {
                r = 30 * scale
                centerY = (-(centerX * 0.625) - 50 * scale).toFloat()
                sPaint.strokeWidth = 3 * scale
            }
        }


        for (i in 0 until seatsNo) {
            canvas.drawCircle(0f, centerY, r, sPaint)
            x.add(-centerY * Math.cos(Math.toRadians((-270 - 360 * i / seatsNo.toFloat()).toDouble())) + centerX)
            y.add(centerY * Math.sin(Math.toRadians((-270 - 360 * i / seatsNo.toFloat()).toDouble())) + 430 * scale)
            //(度)ずつ回転し描画
            canvas.rotate(360 / seatsNo.toFloat())
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////カウンター//////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawCounterTable(canvas: Canvas) {
        //椅子
        val sPaint = Paint()
        sPaint.color = Color.parseColor("#000000")
        sPaint.strokeWidth = 7 * scale
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = 50 * scale
        val transY = 130
        lastX = 0
        lastY = (transY * seatsNo * scale).toInt()

        //間隔
        for (i in 0 until seatsNo) {
            canvas.drawCircle(160 * scale, 150 * scale, r, sPaint)
            canvas.translate(0f, transY * scale)
            x.add(160.toDouble() * scale)
            y.add((transY * i + 150).toDouble() * scale)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////メソッド////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun memberNo(position: Int) {
        seatsNo = if (normalMode!!) {
            arrayArrayNormal[position].size
        } else {
            arrayArrayQuick[position].size
        }
    }

    private fun initialName(canvas: Canvas, i: Int, X: Float, Y: Float) {
        val nameInitial: String = if (normalMode!!) {
            arrayArrayNormal[position][i].name[0].toString()
        } else {
            arrayArrayQuick[position][i].replace("[^0-9]".toRegex(), "")   //文字列から数値のみ抜き出し
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = r
        val textWidth = textPaint.measureText(nameInitial)
        val textStartX = X - textWidth / 2
        val textMinY = Y + 18 * scale
        canvas.drawText(nameInitial, textStartX, textMinY, textPaint)
    }

    private fun setFmBackground(canvas: Canvas, startNo: Int, endNo: Int) {
        val sPaint = Paint()
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.FILL

        for (i in startNo until endNo) {
            if (normalMode!!) {
                if (arrayArrayNormal[position][i].sex == resources.getText(R.string.man)) {
                    sPaint.color = ContextCompat.getColor(context, R.color.thin_man)
                } else {
                    sPaint.color = ContextCompat.getColor(context, R.color.thin_woman)
                }
            } else {
                when {
                    arrayArrayQuick[position][i].matches((".*" + "♠" + ".*").toRegex()) -> sPaint.color = ContextCompat.getColor(context, R.color.thin_man)
                    arrayArrayQuick[position][i].matches((".*" + "♡" + ".*").toRegex()) -> sPaint.color = ContextCompat.getColor(context, R.color.thin_woman)
                    else -> sPaint.color = ContextCompat.getColor(context, R.color.thin_white)
                }
            }

            val xP = x[i] - lastX
            val yP = y[i] - lastY
            canvas.drawCircle(xP.toFloat(), yP.toFloat(), r, sPaint)
            initialName(canvas, i, xP.toFloat(), yP.toFloat())
        }
    }

    private fun setFocusGradation(canvas: Canvas) {
        val xP = x[point] - lastX
        val yP = y[point] - lastY
        val gradient = RadialGradient(xP.toFloat(), yP.toFloat(), 1.5.toFloat() * r, Color.parseColor("#ffaa00"),
                Color.argb(0, 0, 0, 0), Shader.TileMode.CLAMP)
        val graPaint = Paint()
        graPaint.isDither = true
        ///端を滑らかにする。
        graPaint.shader = gradient
        canvas.drawCircle(xP.toFloat(), yP.toFloat(), 1.5.toFloat() * r, graPaint)
        graPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        graPaint.reset()
        graPaint.isAntiAlias = true
        graPaint.style = Paint.Style.FILL
        graPaint.color = Color.WHITE
        canvas.drawCircle(xP.toFloat(), yP.toFloat(), r, graPaint)
    }

    private fun drawMemberName(canvas: Canvas, type: String?) {
        val xP = x[point] - lastX
        val yP = y[point] - lastY
        var textSize = (60 * scale).toInt()
        var textWidth: Float
        var textStartX: Float
        var textY: Float
        val balloonStartX: Float
        val balloonStartY: Float
        val balloonEndX: Float
        val balloonEndY: Float
        // 文字列用ペイントの生成
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = 60 * scale
        val text: String = if (normalMode!!) {
            arrayArrayNormal[position][point].name
        } else {
            arrayArrayQuick[position][point]
        }
        textWidth = textPaint.measureText(text)
        while (textWidth > 320) {
            textPaint.textSize = textSize.toFloat()
            textWidth = textPaint.measureText(text)
            textSize--
        }
        while (textWidth > 320 * scale) {
            textPaint.textSize = textSize.toFloat()
            textWidth = textPaint.measureText(text)
            textSize--
        }
        textPaint.color = Color.WHITE


        when (type) {
            "square" -> {
                textY = (yP + 22 * scale).toFloat()
                textStartX = 200 * scale
                when {
                    point < squareNo -> textY = yP.toFloat() + 150 * scale
                    point < a -> textY = yP.toFloat() - 110 * scale
                    point < (seatsNo + a) / 2 -> textStartX = (xP + 140 * scale).toFloat()
                    else -> textStartX = (xP - (90 * scale).toDouble() - textWidth.toDouble() - r.toDouble()).toFloat()
                }
                balloonStartX = textStartX - r
                balloonEndX = textStartX + textWidth + r
                balloonStartY = textY - r - 15 * scale
                balloonEndY = textY + r - 25 * scale
            }
            "parallel" -> {
                textStartX = if (point < seatsNo / 2) {
                    (xP + 140 * scale).toFloat()
                } else {
                    (xP - (90 * scale).toDouble() - textWidth.toDouble() - r.toDouble()).toFloat()
                }
                textY = (yP + 22 * scale).toFloat()
                balloonStartX = textStartX - r
                balloonEndX = textStartX + textWidth + r
                balloonStartY = textY - r - 15 * scale
                balloonEndY = textY + r - 25 * scale
            }
            "circle" -> {
                // 文字列の幅からX座標を計算
                textStartX = -textWidth / 2
                // 文字列の高さからY座標を計算
                textY = 60 / 2 * scale
                balloonStartX = textStartX - 20 * scale
                balloonEndX = -textStartX + 20 * scale
                balloonStartY = -textY - 5 * scale
                balloonEndY = textY + 20 * scale
            }
            else -> {
                textStartX = (xP + 150 * scale).toFloat()
                textY = (yP + 22 * scale).toFloat()
                balloonStartX = textStartX - r
                balloonEndX = textStartX + textWidth + r
                balloonStartY = textY - r - 15 * scale
                balloonEndY = textY + r - 25 * scale
            }
        }

        // 吹き出し用ペイントの生成
        val balloonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        balloonPaint.textSize = 35 * scale
        if (normalMode!!) {
            if (arrayArrayNormal[position][point].sex == resources.getText(R.string.man)) {
                balloonPaint.color = ContextCompat.getColor(context, R.color.man)
            } else {
                balloonPaint.color = ContextCompat.getColor(context, R.color.woman)
            }
        } else {
            when {
                arrayArrayQuick[position][point].matches((".*" + "♠" + ".*").toRegex()) -> balloonPaint.color = ContextCompat.getColor(context, R.color.man)
                arrayArrayQuick[position][point].matches((".*" + "♡" + ".*").toRegex()) -> balloonPaint.color = ContextCompat.getColor(context, R.color.woman)
                else -> balloonPaint.color = ContextCompat.getColor(context, R.color.green)
            }
        }

        val balloonRectF = RectF(balloonStartX, balloonStartY, balloonEndX, balloonEndY)
        canvas.drawRoundRect(balloonRectF, 30 * scale, 30 * scale, balloonPaint)

        // 文字列の描画
        canvas.drawText(text, textStartX, textY, textPaint)
    }

    companion object {
        var point = 0
        var position = 0
        var tableType: String = "circle"
    }

}

