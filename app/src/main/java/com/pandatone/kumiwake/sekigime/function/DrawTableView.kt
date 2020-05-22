package com.pandatone.kumiwake.sekigime.function

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.sekigime.SekigimeResult
import java.util.*
import kotlin.math.*


/**
 * Created by atsushi_2 on 2016/07/15.
 */
/**
 * Canvasは中心が原点、下・右が正 (座標系)http://kumacoro.blogspot.com/2012/03/androidcanvaspaint-x-y-y-padintcanvas.html
 * Textは左下が原点 https://artica.ddns.net/blog/2015/08/25/post-23/
 * canvas.drawText( 文字列, GX, GY, 文字サイズ, 文字色[,背景色] )     GX: 表示開始X座標(文字始点左下座標)   GY: 表示開始Y座標(文字始点左下座標)
 * canvas.drawTextCenter( 文字列, CX, GY, 文字サイズ, 文字色[,背景色] )     CX: 表示文字の中心X座標    GY: 表示開始Y座標(文字始点左下座標)
 */
/**
 * #define
 * xP,yP : 座席の中心のX,Y座標
 */


class DrawTableView(context: Context) : View(context) {
    private var squareNo = SekigimeResult.square_no
    private var doubleDeploy: Boolean? = SekigimeResult.doubleDeploy
    private var teamArray = SekigimeResult.teamArray
    private var xCoordinate = 0f
    private var yCoordinate = 0f
    private var dp: Float = 0f
    private val tableStrokeColor = "#b59551"
    private val tableStrokeWidth = 15f
    private val tableColor = "#ffffe0"
    private val chairStrokeColor = "#000000"
    private val tableRound = 20f

    private var r: Float = 0f //balloonのround
    private var lastX = 0f
    private var lastY = 0f
    private var seatsNo: Int = 0
    private var a: Int = 0
    private var dispWidth: Int = 0
    private var x: ArrayList<Float> = ArrayList()
    private var y: ArrayList<Float> = ArrayList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        seatsNo = memberNo(tableNo)
        dp = resources.displayMetrics.density / 2
        dispWidth = MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        when (tableType) {
            "square" -> {
                height = if (doubleDeploy!!) { //両側
                    360 + (130 * ((seatsNo - squareNo * 2) / 2f).roundToInt() + 50)
                } else {
                    280 + (130 * ((seatsNo - squareNo) / 2f).roundToInt() + 50)
                }
            }
            "parallel" -> height = 130 * (seatsNo / 2f).roundToInt() + 200
            "circle" -> height = 860
            "counter" -> height = seatsNo * 132 + 100
        }
        setMeasuredDimension(dispWidth, (height * dp).toInt())
        point = 0
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
            setFocusGradation(canvas)
            setFmBackground(canvas, 0, seatsNo)
            drawMemberName(canvas, tableType)
        }
    }

    // 再描画
    fun reDraw() {
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

        if (abs(event.x - xCoordinate) > 1 && abs(event.y - yCoordinate) > 1) {

            xCoordinate = event.x
            yCoordinate = event.y
            val xArray = x
            val yArray = y
            var x: Float
            var y: Float

            for (i in xArray.indices) {
                x = xArray[i]
                if (abs(xCoordinate - x) <= r) {
                    for (j in yArray.indices) {
                        y = yArray[j]
                        if (abs(yCoordinate - y) <= r && j == i) {
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
        val tableTop = 180 * dp
        val tableHeight = if (doubleDeploy!!) { //両側
            tableTop + (130 * ((seatsNo - squareNo * 2) / 2f).roundToInt() + 50) * dp
        } else {
            tableTop + (130 * ((seatsNo - squareNo) / 2f).roundToInt() + 50) * dp
        }

        // 机
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = tableStrokeWidth * dp
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        val rectRight = dispWidth - 180 * dp
        canvas.drawRoundRect(180 * dp, tableTop, rectRight, tableHeight, tableRound, tableRound, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(180 * dp, tableTop, rectRight, tableHeight, tableRound, tableRound, cPaint)

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
        //上辺
        while (a < squareNo) {
            canvas.drawCircle(transX + 150 * dp, 105 * dp, r, sPaint)
            canvas.translate(transX, 0f)
            x.add(transX * (a + 1) + 150 * dp)
            y.add(105 * dp)
            a++
        }
        canvas.translate((-transX * a), 0f)
        //底辺
        if (doubleDeploy!!) {
            val initialY = tableHeight + 75 * dp
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

        r = 50 * dp
        sPaint.strokeWidth = 7 * dp
        val sideTopY = 270
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
        val tableTop = 100 * dp
        val tableHeight = tableTop + (130 * (seatsNo / 2f).roundToInt() + 50) * dp //四捨五入

        // 机
        //val bmp = BitmapFactory.decodeResource(resources, R.drawable.mokume)
        cPaint.isAntiAlias = true
        val rectRight = dispWidth - 180 * dp
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = tableStrokeWidth * dp
        cPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(180 * dp, tableTop, rectRight, tableHeight, tableRound, tableRound, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(180 * dp, tableTop, rectRight, tableHeight, tableRound, tableRound, cPaint)
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
        while (i < seatsNo / 2) {
            canvas.drawCircle(100 * dp, 190 * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(100 * dp)
            y.add((transY * i + 190) * dp)
            i++
        }
        canvas.translate(0f, -transY * i * dp)
        while (i < seatsNo) {
            canvas.drawCircle(rectRight + 80 * dp, 190 * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(rectRight + 80 * dp)
            y.add((transY * j + 190) * dp)
            i++
            j++
        }
        lastX = 0f
        lastY = (transY * floor(seatsNo / 2f) * dp)
        if (seatsNo == 1) {
            lastY = (transY * dp)
        } else if (y[0] - lastY != 190f) {
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

        // 円
        cPaint.color = Color.parseColor(tableStrokeColor)
        cPaint.strokeWidth = tableStrokeWidth * dp
        cPaint.isAntiAlias = true
        cPaint.style = Paint.Style.STROKE
        // (x,y,r,paint) 中心座標(x,y), 半径r
        canvas.drawCircle(centerX, 430 * dp, centerX * 0.625f, cPaint)
        cPaint.color = Color.parseColor(tableColor)
        cPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, 430 * dp, centerX * 0.625f, cPaint)

        //椅子
        sPaint.color = Color.parseColor(chairStrokeColor)
        sPaint.isAntiAlias = true
        sPaint.style = Paint.Style.STROKE
        //回転軸(x,y)
        canvas.translate(centerX, 430 * dp)
        lastX = centerX
        lastY = (430 * dp)
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
            y.add((centerY * sin(Math.toRadians(-270 - 360.0 * i / seatsNo)) + 430 * dp).toFloat())
            //(度)ずつ回転し描画
            canvas.rotate(360f / seatsNo)
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////カウンター//////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


    private fun drawCounterTable(canvas: Canvas) {
        //椅子
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
            canvas.drawCircle(160 * dp, 150 * dp, r, sPaint)
            canvas.translate(0f, transY * dp)
            x.add(160 * dp)
            y.add((transY * i + 150) * dp)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////メソッド////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    //テーブルの席数を代入 to seatsNo
    //テーブルの席数を代入 to seatsNo
    private fun memberNo(position: Int): Int {
        return teamArray[position].size
    }

    //座席にイニシャルを描画
    private fun initialName(canvas: Canvas, i: Int, centerX: Float, centerY: Float) {
        val nameInitial: String = if (StatusHolder.normalMode) {
            teamArray[tableNo][i].name[0].toString()
        } else {
            val member = teamArray[tableNo][i]
            if (member.sex == context.getString(R.string.woman)) {
                (member.id - 1000).toString()   //QuickMode id規則：女メンバー = id+1000
            } else {
                member.id.toString()             //QuickMode id規則：メンバー = id
            }
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
            when (teamArray[tableNo][i].sex) {
                resources.getText(R.string.man) -> sPaint.color = PublicMethods.getColor(context, R.color.thin_man)
                resources.getText(R.string.woman) -> sPaint.color = PublicMethods.getColor(context, R.color.thin_woman)
                else -> sPaint.color = PublicMethods.getColor(context, R.color.thin_white)
            }

            val xP = x[i] - lastX
            val yP = y[i] - lastY
            canvas.drawCircle(xP, yP, r, sPaint)
            initialName(canvas, i, xP, yP)
        }
    }

    //座席選択状態のグラデーション効果
    private fun setFocusGradation(canvas: Canvas) {
        val xP = x[point] - lastX
        val yP = y[point] - lastY
        val gradient = RadialGradient(xP, yP, 1.5f * r, Color.parseColor("#ffaa00"),
                Color.argb(0, 0, 0, 0), Shader.TileMode.CLAMP)
        val graPaint = Paint()
        graPaint.isDither = true
        ///端を滑らかにする。
        graPaint.shader = gradient
        canvas.drawCircle(xP, yP, 1.5f * r, graPaint)
        graPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        graPaint.reset()
        graPaint.isAntiAlias = true
        graPaint.style = Paint.Style.FILL
        graPaint.color = Color.WHITE
        canvas.drawCircle(xP, yP, r, graPaint)
    }

    //メンバー名の吹き出しを描画
    private fun drawMemberName(canvas: Canvas, type: String?) {
        val xP = x[point] - lastX //座席の中心X
        val yP = y[point] - lastY //座席の中心Y
        var textSize = 60 * dp
        var textWidth: Float
        val textStartX: Float
        var textBottomY: Float
        // 文字列用ペイントの生成
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        val text: String = teamArray[tableNo][point].name
        textWidth = textPaint.measureText(text)

        while (textWidth > 300 * dp) {
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
                        textBottomY = yP + 150 * dp
                    }
                    point < a -> {
                        //底辺
                        textStartX = textCenterToStartX(text, xP, textPaint)
                        textBottomY = yP - 110 * dp
                    }
                    point < (seatsNo + a) / 2 -> textStartX = (xP + 140 * dp) //左辺
                    else -> textStartX = (xP - (90 * dp) - textWidth - r) //右辺
                }
            }
            "parallel" -> {
                textStartX =
                        if (point < seatsNo / 2) {
                            (xP + 140 * dp)
                        } else {
                            (xP - (90 * dp) - textWidth - r)
                        }
                textBottomY = textCenterToBottomY(yP, textPaint)
            }
            "circle" -> {
                textStartX = textCenterToStartX(text, 0f, textPaint)
                textBottomY = textCenterToBottomY(0f, textPaint)
            }
            else -> {
                textStartX = (xP + 150 * dp)
                textBottomY = textCenterToBottomY(yP, textPaint)
            }
        }

        // 吹き出し用ペイントの生成
        val balloonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        balloonPaint.textSize = textSize
        when (teamArray[tableNo][point].sex) {
            resources.getText(R.string.man) -> balloonPaint.color = PublicMethods.getColor(context, R.color.man)
            resources.getText(R.string.woman) -> balloonPaint.color = PublicMethods.getColor(context, R.color.woman)
            else -> balloonPaint.color = PublicMethods.getColor(context, R.color.green)
        }

        val balloon = balloon(text, textStartX, textBottomY, balloonPaint)
        canvas.drawRoundRect(balloon, 30 * dp, 30 * dp, balloonPaint)
        // 文字列の描画
        canvas.drawText(text, textStartX, textBottomY, textPaint)
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
        val balloonStart = textStartX - r
        val balloonEnd = textStartToEndX(text, textStartX, paint) + r
        val balloonTop = textBottomToTopY(textBottomY, paint) - r / 2
        val balloonBottom = textBottomY + r / 2
        return RectF(balloonStart, balloonTop, balloonEnd, balloonBottom) //(left, top, right, bottom)
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
        val textWidth = paint.measureText(text) //文字列の幅を取得
        return startX + textWidth
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

    companion object {
        var point = 0 //フォーカスする席番号
        var tableNo = 0 //表示するテーブル番号
        var tableType: String = "circle"
    }

}

