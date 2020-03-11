package com.pandatone.kumiwake.sekigime.function

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.PublicMethods
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.sekigime.SekigimeResult
import com.pandatone.kumiwake.sekigime.function.DrawTableView.Companion.tableNo
import com.pandatone.kumiwake.sekigime.function.DrawTableView.Companion.tableType
import java.lang.reflect.Member
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

/**
 * Created by atsushi_2 on 2016/07/15.
 */

class DrawAllTable(context: Context) : View(context) {
        private var squareNo = SekigimeResult.square_no
        private var doubleDeploy: Boolean? = SekigimeResult.doubleDeploy
        private var teamArray = SekigimeResult.teamArray
        private var scale: Float = 0f

        private var canvasHeight: Float = 0f
        private var r: Float = 0f //balloonのround
        private var lastX = 0f
        private var lastY = 0f
        private var seatsNo: Int = 0
        private var a: Int = 0
        private var dispWidth: Int = 0
        private var x: ArrayList<Float> = ArrayList()
        private var y: ArrayList<Float> = ArrayList()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            memberNo(tableNo)
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
            teamArray = SekigimeResult.teamArray
            x.clear()
            y.clear()
            memberNo(tableNo)
            canvasHeight = height.toFloat()
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

        // 再描画
        fun reDraw() {
            invalidate()
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
            var transX = 0f
            val transY = 130f
            if (squareNo != 0) {
                transX = (rectRight - 110 * scale) / (squareNo + 1)
            }

            //間隔
            a = 0
            var b = 0
            var i = 0
            var j = 0
            while (a < squareNo) {
                canvas.drawCircle(transX + 150 * scale, 155 * scale, r, sPaint)
                canvas.translate(transX, 0f)
                x.add(transX * (a + 1) + 150 * scale)
                y.add(155 * scale)
                a++
            }

            canvas.translate((-transX * a), 0f)
            if (doubleDeploy!!) {
                val initialY = (tableHeight + 75) * scale
                while (a < squareNo * 2) {
                    canvas.drawCircle(transX + 150 * scale, initialY, r, sPaint)
                    canvas.translate(transX, 0f)
                    x.add(transX * (b + 1) + 150 * scale)
                    y.add(initialY)
                    a++
                    b++
                }
            }

            canvas.translate(-transX * b, 0f)
            lastX = 0f
            lastY = 0f
            setFmBackground(canvas, 0, squareNo)
            if (doubleDeploy!!) {
                setFmBackground(canvas, squareNo, a)
            }

            r = 50 * scale
            sPaint.strokeWidth = 7 * scale
            while (i < (seatsNo - a) / 2) {
                canvas.drawCircle(100 * scale, 300 * scale, r, sPaint)
                canvas.translate(0f, transY * scale)
                x.add(100 * scale)
                y.add((transY * i + 300) * scale)
                i++
            }
            canvas.translate(0f, -transY * i * scale)
            while (i < seatsNo - a) {
                canvas.drawCircle(rectRight + 80 * scale, 300 * scale, r, sPaint)
                canvas.translate(0f, transY * scale)
                x.add(rectRight + 80 * scale)
                y.add((transY * j + 300) * scale)
                i++
                j++
            }
            lastX = 0f
            lastY = when {
                seatsNo == 1 -> (transY * scale)
                (seatsNo - a) / 2 == 0 -> (transY * floor((seatsNo - a) / 2f) * scale)
                else -> (transY * floor(((seatsNo - a + 1) / 2f)) * scale)
            }
            setFmBackground(canvas, a, seatsNo)
        }


        //////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////角テーブル２/////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////


        private fun drawParallelTable(canvas: Canvas) {

            val cPaint = Paint()
            val sPaint = Paint()
            val tableHeight = ((130 * ((seatsNo + 1) / 2f).roundToInt() + 150) * scale)

            // 机
            cPaint.color = Color.parseColor("#000000")
            cPaint.strokeWidth = 10 * scale
            cPaint.isAntiAlias = true
            cPaint.style = Paint.Style.STROKE
            val rectRight = dispWidth - 180 * scale
            canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight, cPaint)
            cPaint.color = Color.parseColor("#90ffffff")
            cPaint.style = Paint.Style.FILL
            canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight, cPaint)

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
                x.add(100 * scale)
                y.add((transY * i + 190) * scale)
                i++
            }
            canvas.translate(0f, -transY * i * scale)
            while (i < seatsNo) {
                canvas.drawCircle(rectRight + 80 * scale, 190 * scale, r, sPaint)
                canvas.translate(0f, transY * scale)
                x.add(rectRight + 80 * scale)
                y.add((transY * j + 190) * scale)
                i++
                j++
            }
            lastX = 0f
            lastY = (transY * floor(seatsNo / 2f) * scale)
            if (seatsNo == 1) {
                lastY = (transY * scale)
            } else if (y[0] - lastY != 190f) {
                lastY = (transY * floor((seatsNo + 1) / 2f) * scale)
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
            cPaint.color = Color.parseColor("#000000")
            cPaint.strokeWidth = 10 * scale
            cPaint.isAntiAlias = true
            cPaint.style = Paint.Style.STROKE
            // (x,y,r,paint) 中心座標(x,y), 半径r
            canvas.drawCircle(centerX, 430 * scale, centerX * 0.625f, cPaint)
            cPaint.color = Color.parseColor("#90ffffff")
            cPaint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, 430 * scale, centerX * 0.625f, cPaint)

            //椅子
            sPaint.color = Color.parseColor("#000000")
            sPaint.isAntiAlias = true
            sPaint.style = Paint.Style.STROKE
            //回転軸(x,y)
            canvas.translate(centerX, 430 * scale)
            lastX = centerX
            lastY = (430 * scale)
            when {
                seatsNo < 16 -> {
                    r = 50 * scale
                    centerY = -centerX * 0.625f - 80 * scale
                    sPaint.strokeWidth = 7 * scale
                }
                seatsNo < 21 -> {
                    r = 40 * scale
                    centerY = -centerX * 0.625f - 60 * scale
                    sPaint.strokeWidth = 5 * scale
                }
                else -> {
                    r = 30 * scale
                    centerY = -centerX * 0.625f - 50 * scale
                    sPaint.strokeWidth = 3 * scale
                }
            }


            for (i in 0 until seatsNo) {
                canvas.drawCircle(0f, centerY, r, sPaint)
                x.add((-centerY * Math.cos(Math.toRadians((-270 - 360.0 * i / seatsNo))) + centerX).toFloat())
                y.add((centerY * Math.sin(Math.toRadians(-270 - 360.0 * i / seatsNo)) + 430 * scale).toFloat())
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
            sPaint.color = Color.parseColor("#000000")
            sPaint.strokeWidth = 7 * scale
            sPaint.isAntiAlias = true
            sPaint.style = Paint.Style.STROKE
            r = 50 * scale
            val transY = 130
            lastX = 0f
            lastY = (transY * seatsNo * scale)

            //間隔
            for (i in 0 until seatsNo) {
                canvas.drawCircle(160 * scale, 150 * scale, r, sPaint)
                canvas.translate(0f, transY * scale)
                x.add(160 * scale)
                y.add((transY * i + 150) * scale)
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////メソッド////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////

        //テーブルの席数を代入 to seatsNo
        private fun memberNo(position: Int) {
            seatsNo = teamArray[position].size
        }

        //座席にイニシャルを描画
        private fun initialName(canvas: Canvas, i: Int, centerX: Float, centerY: Float) {
            val nameInitial: String = if (StatusHolder.normalMode) {
                teamArray[tableNo][i].name[0].toString()
            } else {
                teamArray[tableNo][i].id.toString()   //QuickMode命名規則：メンバー + id
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
                drawMemberName(canvas, tableType, 0, i)
            }
        }

        //メンバー名の吹き出しを描画
        private fun drawMemberName(canvas: Canvas, type: String?,tableNo:Int,point:Int) {
            val xP = x[point] - lastX //座席の中心X
            val yP = y[point] - lastY //座席の中心Y
            var textSize = 50 * scale
            var textWidth: Float
            val textStartX: Float
            val textBottomY: Float
            // 文字列用ペイントの生成
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textSize = textSize
            val text: String = teamArray[tableNo][point].name
            textWidth = textPaint.measureText(text)
            while (textWidth > 200 * scale) {
                textPaint.textSize = textSize
                textWidth = textPaint.measureText(text)
                textSize--
            }
            textPaint.color = Color.WHITE


            when (type) {
                "square" -> {
                    textBottomY = textBottomY(yP, textPaint)
                    when {
                        point < squareNo -> {
                            //上辺
                            textStartX = textStartX(text, xP, textPaint)
                        }
                        point < a -> {
                            //底辺
                            textStartX = textStartX(text, xP, textPaint)
                        }
                        point < (seatsNo + a) / 2 -> textStartX = textStartX(text, xP + 60 * scale, textPaint) //左辺
                        else -> textStartX = textStartX(text, xP - 60 * scale, textPaint) //右辺
                    }
                }
                "parallel" -> {
                    textStartX =
                            if (point < seatsNo / 2) {
                                textStartX(text, xP + 60 * scale, textPaint)
                            } else {
                                textStartX(text, xP - 60 * scale, textPaint)
                            }
                    textBottomY = textBottomY(yP, textPaint)
                }
                "circle" -> {
                    textStartX = textStartX(text, xP, textPaint)
                    textBottomY = textBottomY(yP, textPaint)
                }
                else -> {
                    textStartX = (xP + 150 * scale)
                    textBottomY = textBottomY(yP, textPaint)
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
            canvas.drawRoundRect(balloon, 30 * scale, 30 * scale, balloonPaint)
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
            val balloonEnd = textEndX(text, textStartX, paint) + r
            val balloonTop = textTopY(textBottomY, paint) + r
            val balloonBottom = textBottomY - r
            return RectF(balloonStart, balloonTop, balloonEnd, balloonBottom)
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////
        //テキスト座標取得メソッド//
        //////////////////////////////////////////////////////////////////////////////////////////////////
        //テキストの中心X座標を取得 StartX -> CenterX
        private fun textCenterX(text: String, startX: Float, paint: Paint): Float {
            val textWidth = paint.measureText(text) //文字列の幅を取得
            return startX + textWidth / 2
        }

        //テキストの中心Y座標を取得 bottomY -> CenterY
        private fun textCenterY(bottomY: Float, paint: Paint): Float {
            val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
            return bottomY - (metrics.ascent + metrics.descent) / 2f
        }

        //テキストのトップY座標を取得 bottomY -> TopY
        private fun textTopY(bottomY: Float, paint: Paint): Float {
            val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
            return bottomY + (metrics.ascent + metrics.descent)
        }

        //テキストの終端X座標を取得 StartX -> EndX
        private fun textEndX(text: String, startX: Float, paint: Paint): Float {
            val textWidth = paint.measureText(text) //文字列の幅を取得
            return startX + textWidth
        }

        //テキストの下底Y座標を取得 CenterY -> bottomY
        private fun textBottomY(centerY: Float, paint: Paint): Float {
            val metrics: Paint.FontMetrics = paint.fontMetrics //FontMetricsを取得
            return centerY - (metrics.ascent + metrics.descent) / 2f
        }

        //テキストの始点X座標を取得 CenterX -> StartX
        private fun textStartX(text: String, centerX: Float, paint: Paint): Float {
            val textWidth = paint.measureText(text) //文字列の幅を取得
            return centerX - textWidth / 2
        }
    }

