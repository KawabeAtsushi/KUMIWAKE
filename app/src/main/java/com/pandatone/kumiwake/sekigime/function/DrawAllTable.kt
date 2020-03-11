package com.pandatone.kumiwake.sekigime.function

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.function.DrawTableView.Companion.tableType
import kotlinx.android.synthetic.main.select_sekigime_type.view.*
import kotlin.collections.ArrayList
import kotlin.math.*

/**
 * Created by atsushi_2 on 2016/07/15.
 */

class DrawAllTable(context: Context, private val drawTableNo: Int) : View(context) {
    private var squareNo = SekigimeResult.square_no
    private var doubleDeploy: Boolean? = SekigimeResult.doubleDeploy
    private var teamArray = SekigimeResult.teamArray
    private var dp: Float = 0f
    private val tableStrokeColor = "#b78a22"
    private val tableColor = "#ffffe0"
    private val chairStrokeColor = "#000000"

    private var r: Float = 0f //balloonのround
    private var lastX = 0f
    private var lastY = 0f
    private var seatsNo: Int = 0
    private var a: Int = 0
    private var dispWidth: Int = 0
    private var x: ArrayList<Float> = ArrayList()
    private var y: ArrayList<Float> = ArrayList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        seatsNo = memberNo(drawTableNo)
        dp = resources.displayMetrics.density / 2
        dispWidth = MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        when (tableType) {
            "square" -> {
                height = if (doubleDeploy!!) {
                    (seatsNo - squareNo * 2 + 1) * 68 + 400
                } else {
                    (seatsNo - squareNo) * 68 + 400
                }
                if (seatsNo % 2 == 0) {
                    height -= 68
                }
            }
            "parallel" -> {
                height = seatsNo * 68 + 200
                if (seatsNo % 2 == 0) {
                    height -= 68
                }
            }
            "circle" -> height = 850
            "counter" -> height = seatsNo * 132 + 60
        }
        setMeasuredDimension(dispWidth, (height * dp).toInt())
    }


    override fun onDraw(canvas: Canvas) {
        teamArray = SekigimeResult.teamArray
        x.clear()
        y.clear()
        when (tableType) {
            "square" -> drawSquareTable(canvas)
            "parallel" -> drawParallelTable(canvas)
            "circle" -> drawCircleTable(canvas)
            "counter" -> drawCounterTable(canvas)
        }

        if (tableType != "square") {
            setFmBackground(canvas, 0, seatsNo)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル１//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private fun drawSquareTable(canvas: Canvas) {
        val topTablePos = 180f
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
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = 10 * dp
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        val rectRight = dispWidth - 180 * dp
        canvas.drawRect(180 * dp, topTablePos * dp, rectRight, tableHeight * dp, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawRect(180 * dp, topTablePos * dp, rectRight, tableHeight * dp, cPaint)

        //椅子
        sPaint.color = Color.parseColor(chairStrokeColor)
        if (squareNo > 5) {
            sPaint.strokeWidth = 3 * dp
        } else {
            sPaint.strokeWidth = (8 - squareNo) * dp
        }
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = if (squareNo > 10) {
            23 * dp
        } else {
            (50 - 3 * (squareNo - 1)) * dp
        }
        var transX = 0f
        val transY = 130f
        if (squareNo != 0) {
            transX = (rectRight - 110 * dp) / (squareNo + 1)
        }

        //間隔
        a = 0
        var b = 0
        var i = 0
        var j = 0
        val topChairY = topTablePos - 75f
        //上辺
        while (a < squareNo) {
            canvas.drawCircle(transX + 150 * dp, topChairY * dp, r, sPaint)
            canvas.translate(transX, 0f)
            x.add(transX * (a + 1) + 150 * dp)
            y.add(topChairY * dp)
            a++
        }
        canvas.translate((-transX * a), 0f)
        //底辺
        if (doubleDeploy!!) {
            val initialY = (tableHeight + 75) * dp
            while (a < squareNo * 2) {
                canvas.drawCircle(transX + 150 * dp, initialY, r, sPaint)
                canvas.translate(transX, 0f)
                x.add(transX * (b + 1) + 150 * dp)
                y.add(initialY)
                a++
                b++
            }
        }
        canvas.translate(-transX * b, 0f)
        //左辺
        lastX = 0f
        lastY = 0f
        setFmBackground(canvas, 0, squareNo)
        if (doubleDeploy!!) {
            setFmBackground(canvas, squareNo, a)
        }

        r = 50 * dp
        sPaint.strokeWidth = 7 * dp
        val sideTopY = topTablePos + 90f
        while (i < (seatsNo - a) / 2) {
            canvas.drawCircle(100 * dp, sideTopY * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(100 * dp)
            y.add((transY * i + sideTopY) * dp)
            i++
        }
        canvas.translate(0f, -transY * i * dp)
        //右辺
        while (i < seatsNo - a) {
            canvas.drawCircle(rectRight + 80 * dp, sideTopY * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(rectRight + 80 * dp)
            y.add((transY * j + sideTopY) * dp)
            i++
            j++
        }
        lastX = 0f
        lastY = when {
            seatsNo == 1 -> (transY * dp)
            (seatsNo - a) / 2 == 0 -> (transY * floor((seatsNo - a) / 2f) * dp)
            else -> (transY * floor(((seatsNo - a + 1) / 2f)) * dp)
        }
        setFmBackground(canvas, a, seatsNo)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル２/////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawParallelTable(canvas: Canvas) {

        val topTablePos = 50f

        val cPaint = Paint()
        val sPaint = Paint()
        val tableHeight = if (seatsNo % 2 == 0) {
            ((130 * seatsNo / 2f).roundToInt() + 150) * dp
        } else {
            ((130 * (seatsNo + 1) / 2f).roundToInt() + 150) * dp
        }

        // 机
        //val bmp = BitmapFactory.decodeResource(resources, R.drawable.mokume)
        cPaint.isAntiAlias = true
        val rectRight = dispWidth - 180 * dp
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = 10 * dp
        cPaint.style = Paint.Style.STROKE
        canvas.drawRect(180 * dp, topTablePos * dp, rectRight, tableHeight, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawRect(180 * dp, topTablePos * dp, rectRight, tableHeight, cPaint)
//        val rect = RectF(180 * dp, 100 * dp, rectRight, tableHeight)
//        canvas.drawBitmap(bmp, null, rect, cPaint)

        //椅子
        sPaint.color = Color.parseColor(chairStrokeColor)
        sPaint.strokeWidth = 7 * dp
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = 50 * dp
        val transY = 130

        //間隔
        var i = 0
        var j = 0
        val startChairY = topTablePos + 90f
        while (i < seatsNo / 2) {
            canvas.drawCircle(100 * dp, startChairY * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(100 * dp)
            y.add((transY * i + startChairY) * dp)
            i++
        }
        canvas.translate(0f, -transY * i * dp)
        while (i < seatsNo) {
            canvas.drawCircle(rectRight + 80 * dp, startChairY * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(rectRight + 80 * dp)
            y.add((transY * j + startChairY) * dp)
            i++
            j++
        }
        lastX = 0f
        lastY = (transY * floor(seatsNo / 2f) * dp)
        if (seatsNo == 1) {
            lastY = (transY * dp)
        } else if (y[0] - lastY != startChairY) {
            lastY = (transY * floor((seatsNo + 1) / 2f) * dp)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////丸テーブル///////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawCircleTable(canvas: Canvas) {
        val cPaint = Paint()
        val sPaint = Paint()
        val centerY: Float
        val centerX: Float = dispWidth / 2f
        val offsetCenterY = 450f

        // 円
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = 10 * dp
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        // (x,y,r,paint) 中心座標(x,y), 半径r
        canvas.drawCircle(centerX, offsetCenterY * dp, centerX * 0.625f, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, offsetCenterY * dp, centerX * 0.625f, cPaint)

        //椅子
        sPaint.color = Color.parseColor(chairStrokeColor)
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        //回転軸(x,y)
        canvas.translate(centerX, offsetCenterY * dp)
        lastX = centerX
        lastY = (offsetCenterY * dp)
        when {
            seatsNo < 16 -> {
                r = 50 * dp
                centerY = -centerX * 0.625f - 80 * dp
                sPaint.strokeWidth = 7 * dp
            }
            seatsNo < 21 -> {
                r = 40 * dp
                centerY = -centerX * 0.625f - 60 * dp
                sPaint.strokeWidth = 5 * dp
            }
            else -> {
                r = 30 * dp
                centerY = -centerX * 0.625f - 50 * dp
                sPaint.strokeWidth = 3 * dp
            }
        }


        for (i in 0 until seatsNo) {
            canvas.drawCircle(0f, centerY, r, sPaint)
            x.add((-centerY * cos(Math.toRadians((-270 - 360.0 * i / seatsNo))) + centerX).toFloat())
            y.add((centerY * sin(Math.toRadians(-270 - 360.0 * i / seatsNo)) + offsetCenterY * dp).toFloat())
            //(度)ずつ回転し描画
            canvas.rotate(360f / seatsNo)
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////カウンター//////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawCounterTable(canvas: Canvas) {
        //椅子
        val topChairPos = 100f
        val sPaint = Paint()
        sPaint.color = Color.parseColor(chairStrokeColor)
        sPaint.strokeWidth = 7 * dp
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        r = 50 * dp
        val transY = 130
        lastX = 0f
        lastY = (transY * seatsNo * dp)

        //間隔
        for (i in 0 until seatsNo) {
            canvas.drawCircle(160 * dp, topChairPos * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(160 * dp)
            y.add((transY * i + topChairPos) * dp)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////メソッド////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    //テーブルの席数を代入 to seatsNo
    private fun memberNo(position: Int): Int {
        return teamArray[position].size
    }

    //座席にイニシャルを描画
    private fun initialName(canvas: Canvas, i: Int, centerX: Float, centerY: Float) {
        val nameInitial: String = if (StatusHolder.normalMode) {
            teamArray[drawTableNo][i].name[0].toString()
        } else {
            teamArray[drawTableNo][i].id.toString()   //QuickMode命名規則：メンバー + id
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = r
        canvas.drawTextCenter(nameInitial, centerX, centerY, textPaint)
    }

    //性別によって座席の色を変更
    private fun setFmBackground(canvas: Canvas, startNo: Int, endNo: Int) {
        val sPaint = Paint()
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.FILL

        for (i in startNo until endNo) {
            when (teamArray[drawTableNo][i].sex) {
                resources.getText(R.string.man) -> sPaint.color = PublicMethods.getColor(context, R.color.thin_man)
                resources.getText(R.string.woman) -> sPaint.color = PublicMethods.getColor(context, R.color.thin_woman)
                else -> sPaint.color = PublicMethods.getColor(context, R.color.thin_white)
            }

            val xP = x[i] - lastX
            val yP = y[i] - lastY
            canvas.drawCircle(xP, yP, r, sPaint)
            initialName(canvas, i, xP, yP)
            drawMemberName(canvas, tableType, i)
        }
    }

    //メンバー名の吹き出しを描画
    private fun drawMemberName(canvas: Canvas, type: String?, point: Int) {
        val xP = x[point] - lastX //座席の中心X
        val yP = y[point] - lastY //座席の中心Y
        var textSize = 40 * dp
        val textStartX: Float
        var textBottomY: Float
        // 文字列用ペイントの生成
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        val text: String = teamArray[drawTableNo][point].name
        var textWidth = textPaint.measureText(text)
        while (textWidth > 200 * dp) {
            textPaint.textSize = textSize
            textWidth = textPaint.measureText(text)
            textSize--
        }
        textPaint.color = Color.WHITE


        when (type) {
            "square" -> {
                textBottomY = textCenterToBottomY(yP, textPaint)
                when {
                    point < squareNo -> {
                        //上辺
                        textStartX = textCenterToStartX(text, xP, textPaint)
                        if (squareNo != 1) {
                            textBottomY = nonOverlapSquare(point, yP, textPaint)
                        }
                    }
                    point < a -> {
                        //底辺
                        textStartX = textCenterToStartX(text, xP, textPaint)
                        if (squareNo != 1) {
                            textBottomY = nonOverlapSquare(point, yP, textPaint)
                        }
                    }
                    point < (seatsNo + a) / 2 -> textStartX = 50 * dp //左辺
                    else -> textStartX = textEndToStartX(text, dispWidth - 50 * dp, textPaint) //右辺
                }
            }
            "parallel" -> {
                textStartX =
                        if (point < seatsNo / 2) {
                            50 * dp //左辺
                        } else {
                            textEndToStartX(text, dispWidth - 50 * dp, textPaint) //右辺
                        }
                textBottomY = textCenterToBottomY(yP, textPaint)
            }
            "circle" -> {
                val startX = textCenterToStartX(text, xP, textPaint)
                textStartX = nonProtrudeCircle(text, startX, textPaint)
                val bottomY = textCenterToBottomY(yP, textPaint)
                textBottomY = nonOverlapCircle(bottomY, point,textPaint)
            }
            else -> {
                r = 60 * dp
                textSize += 10 * dp
                textPaint.textSize = textSize
                textStartX = (xP + 120 * dp)
                textBottomY = textCenterToBottomY(yP, textPaint)
            }
        }

        // 吹き出し用ペイントの生成
        val balloonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        balloonPaint.textSize = textSize
        when (teamArray[drawTableNo][point].sex) {
            resources.getText(R.string.man) -> balloonPaint.color = PublicMethods.getColor(context, R.color.man)
            resources.getText(R.string.woman) -> balloonPaint.color = PublicMethods.getColor(context, R.color.woman)
            else -> balloonPaint.color = PublicMethods.getColor(context, R.color.green)
        }

        val balloon = balloon(text, textStartX, textBottomY, balloonPaint)
        canvas.drawRoundRect(balloon, 30 * dp, 30 * dp, balloonPaint)
        // 文字列の描画
        canvas.drawText(text, textStartX, textBottomY, textPaint)
        if (tableType == "counter") {
            r = 50 * dp
        }
    }

    //拡張関数中心にしたい座標を指定して文字を描画  centerX:中心にしたいX座標 centerY:中心にしたいY座標
    private fun Canvas.drawTextCenter(text: String, centerX: Float, centerY: Float, paint: Paint) {
        val textWidth = paint.measureText(text) //文字列の幅を取得
        val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
        val textCenterX = centerX - textWidth / 2
        val textCenterY = centerY - (metrics.ascent + metrics.descent) / 2f
        this.drawText(text, textCenterX, textCenterY, paint)
    }

    //バルーンView生成
    private fun balloon(text: String, textStartX: Float, textBottomY: Float, paint: Paint): RectF {
        val balloonStart = textStartX - r / 2
        val balloonEnd = textStartToEndX(text, textStartX, paint) + r / 2
        val balloonTop = textBottomToTopY(textBottomY, paint) + r
        val balloonBottom = textBottomY - r
        return RectF(balloonStart, balloonTop, balloonEnd, balloonBottom)
    }

    //squareのバルーンが重ならないように描画
    private fun nonOverlapSquare(point: Int, yP: Float, textPaint: Paint): Float {
        return if (point % 2 == 1) {
            textCenterToBottomY(yP - 35 * dp, textPaint)
        } else {
            textCenterToBottomY(yP + 35 * dp, textPaint)
        }
    }

    //circleのバルーンが画面からはみ出ないように描画
    private fun nonProtrudeCircle(text: String, startX: Float, textPaint: Paint): Float {
        val endX = textStartToEndX(text, startX, textPaint) + r / 2
        return when {
            endX > dispWidth / 2 - 10 * dp -> {
                startX - (endX - dispWidth / 2 + 10 * dp) //元の値-はみ出た分-マージン
            }
            startX < -dispWidth / 2 + 10 * dp -> {
                -dispWidth / 2 + r / 2 + 10 * dp //ディスプレイの左端から10dpの位置
            }
            else -> {
                startX
            }
        }
    }

    //circleのバルーンが重ならないように描画
    private fun nonOverlapCircle(bottomY: Float, point: Int,textPaint: Paint): Float {
        if (point == 0) { //一番上の席
            val nextCenterY = y[1] - lastY
            val dist = abs(bottomY - nextCenterY) * dp //底辺と次の席の中心点の距離
            return if (dist < 75 * dp) { //重ならない距離90dp
                bottomY - (75 * dp -dist) //重ならない位置　+ マージン
            } else {
                bottomY
            }
        } else if (point == seatsNo / 2) { //一番下の席
            val nextCenterY = y[point + 1] - lastY
            val dist = abs(textBottomToTopY(bottomY,textPaint) - nextCenterY)*dp //上辺と次の席の中心点の距離
            Log.d("Dist2",dist.toString())
            return if (dist <= 75 * dp) { //重ならない距離90dp
                bottomY + (75 * dp - dist) //重ならない位置　+ マージン
            } else {
                bottomY
            }
        } else {
            return bottomY
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //テキスト座標取得メソッド//
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //テキストのトップY座標を取得 bottomY -> TopY
    private fun textBottomToTopY(bottomY: Float, paint: Paint): Float {
        val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
        val fh = metrics.top + metrics.descent //(なぜかこれがぴったり)
        return bottomY + fh
    }

    //テキストの終端X座標を取得 StartX -> EndX
    private fun textStartToEndX(text: String, startX: Float, paint: Paint): Float {
        val textWidth = paint.measureText(text)//文字列の幅を取得
        return startX + textWidth
    }

    //テキストの始点X座標を取得 EndX -> StartX
    private fun textEndToStartX(text: String, endX: Float, paint: Paint): Float {
        val textWidth = paint.measureText(text) //文字列の幅を取得
        return endX - textWidth
    }

    //テキストの下底Y座標を取得 CenterY -> bottomY
    private fun textCenterToBottomY(centerY: Float, paint: Paint): Float {
        val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
        val fh = -metrics.ascent + metrics.descent
        return centerY + fh / 2 - metrics.descent
    }

    //テキストの始点X座標を取得 CenterX -> StartX
    private fun textCenterToStartX(text: String, centerX: Float, paint: Paint): Float {
        val textWidth = paint.measureText(text) //文字列の幅を取得
        return centerX - textWidth / 2
    }
}

