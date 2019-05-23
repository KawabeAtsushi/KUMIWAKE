package com.pandatone.kumiwake.sekigime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.pandatone.kumiwake.R;
import com.pandatone.kumiwake.member.Name;

import java.util.ArrayList;

/**
 * Created by atsushi_2 on 2016/07/15.
 */
public class DrawTableView extends View {

    static float canvasHeight, r;
    static int position = 0, point = 0, last_x = 0, last_y = 0, seatsNo, a,width;
    int square_no = SekigimeResult.square_no;
    static String tableType;
    Boolean Normalmode = SekigimeResult.Normalmode, doubleDeploy = SekigimeResult.doubleDeploy;
    ArrayList<ArrayList<String>> arrayArrayQuick = SekigimeResult.arrayArrayQuick;
    ArrayList<ArrayList<Name>> arrayArrayNormal = SekigimeResult.arrayArrayNormal;
    static ArrayList<Double> x, y;
    private float xZahyou = 0, yZahyou = 0, scale;

    public DrawTableView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        memberNo(position);
        scale = getResources().getDisplayMetrics().density / 2;
        width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        switch (tableType) {
            case "square":
                height = (seatsNo - square_no * 2) * 68 + 450;
                break;
            case "parallel":
                height = seatsNo * 68 + 250;
                break;
            case "circle":
                height = 810;
                break;
            case "counter":
                height = seatsNo * 132 + 100;
                break;
        }
        setMeasuredDimension(width, (int) (height * scale));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        arrayArrayQuick = SekigimeResult.arrayArrayQuick;
        arrayArrayNormal = SekigimeResult.arrayArrayNormal;
        x = new ArrayList<>();
        y = new ArrayList<>();
        memberNo(position);
        canvasHeight = canvas.getHeight();
        switch (tableType) {
            case "square":
                drawSquareTable(canvas);
                break;
            case "parallel":
                drawParallelTable(canvas);
                break;
            case "circle":
                drawCircleTable(canvas);
                break;
            case "counter":
                drawCounterTable(canvas);
                break;
        }

        if (!tableType.equals("square")) {
            setFocusGradation(canvas);
            setFmBackground(canvas, 0, seatsNo);
            drawMemberName(canvas, tableType);
        }
    }

    public void reDraw() {
        // 再描画
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (action == MotionEvent.ACTION_MOVE) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        if (Math.abs(event.getX() - xZahyou) > 1 && Math.abs(event.getY() - yZahyou) > 1) {

            xZahyou = event.getX();
            yZahyou = event.getY();
            ArrayList<Double> xArray = x;
            ArrayList<Double> yArray = y;
            Double x, y;

            for (int i = 0; i < xArray.size(); i++) {
                x = xArray.get(i);
                if (Math.abs(xZahyou - x) <= r) {
                    for (int j = 0; j < yArray.size(); j++) {
                        y = yArray.get(j);
                        if (Math.abs(yZahyou - y) <= r && j == i) {
                            point = j;
                            reDraw();
                        }
                    }
                }
            }
        }

        return true;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル１//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void drawSquareTable(Canvas canvas) {
        Paint cPaint, sPaint;
        int tableHeight;
        if (doubleDeploy) {
            if ((seatsNo - square_no * 2) % 2 == 0) {
                tableHeight = 130 * (seatsNo - square_no * 2) / 2 + 235;
            } else {
                tableHeight = 130 * (seatsNo - square_no * 2 + 1) / 2 + 235;
            }
        } else {
            if ((seatsNo - square_no) % 2 == 0) {
                tableHeight = 130 * (seatsNo - square_no) / 2 + 235;
            } else {
                tableHeight = 130 * (seatsNo - square_no + 1) / 2 + 235;
            }
        }

        // 机
        cPaint = new Paint();
        cPaint.setColor(Color.parseColor("#000000"));
        cPaint.setStrokeWidth(10 * scale);
        cPaint.setAntiAlias(true);
        cPaint.setStyle(Paint.Style.STROKE);
        float rectRight=width-180*scale;
        canvas.drawRect(180 * scale, 230 * scale, rectRight, tableHeight * scale, cPaint);
        cPaint.setColor(Color.parseColor("#90ffffff"));
        cPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(180 * scale, 230 * scale, rectRight, tableHeight * scale, cPaint);

        //椅子
        sPaint = new Paint();
        sPaint.setColor(Color.parseColor("#000000"));
        if (square_no > 5) {
            sPaint.setStrokeWidth(3 * scale);
        } else {
            sPaint.setStrokeWidth((8 - square_no) * scale);
        }
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.STROKE);
        if (square_no > 10) {
            r = 23 * scale;
        } else {
            r = (50 - 3 * (square_no - 1)) * scale;
        }
        int transX = 0, transY = 130;
        if (square_no != 0) {
            transX = ((int)(rectRight-110*scale) / (square_no + 1));
        }

        //間隔
        a = 0;
        int b = 0, i = 0, j = 0;
        while (a < square_no) {
            canvas.drawCircle(transX + 150 * scale, 155 * scale, r, sPaint);
            canvas.translate(transX, 0);
            x.add((double) transX * (a + 1) + 150 * scale);
            y.add((double) 155 * scale);
            a++;
        }

        canvas.translate(-transX * a, 0);
        if (doubleDeploy) {
            int initialY = (int) ((tableHeight + 75) * scale);
            while (a < square_no * 2) {
                canvas.drawCircle(transX + 150 * scale, initialY, r, sPaint);
                canvas.translate(transX, 0);
                x.add((double) transX * (b + 1) + 150 * scale);
                y.add((double) (initialY));
                a++;
                b++;
            }
        }

        canvas.translate(-transX * b, 0);
        last_x = 0;
        last_y = 0;
        if (point < square_no) {
            setFocusGradation(canvas);
        }
        setFmBackground(canvas, 0, square_no);
        if (doubleDeploy) {
            if (square_no <= point && point < a) {
                setFocusGradation(canvas);
            }
            setFmBackground(canvas, square_no, a);
        }

        r = 50 * scale;
        sPaint.setStrokeWidth(7 * scale);
        while (i < (seatsNo - a) / 2) {
            canvas.drawCircle(100 * scale, 300 * scale, r, sPaint);
            canvas.translate(0, transY * scale);
            x.add((double) 100 * scale);
            y.add((double) (transY * i + 300) * scale);
            i++;
        }
        canvas.translate(0, (-transY * i) * scale);
        while (i < seatsNo - a) {
            canvas.drawCircle(rectRight+80 * scale, 300 * scale, r, sPaint);
            canvas.translate(0, transY * scale);
            x.add((double) rectRight+80 * scale);
            y.add((double) (transY * j + 300) * scale);
            i++;
            j++;
        }
        last_x = 0;
        if (seatsNo == 1) {
            last_y = (int) (transY * scale);
        } else if ((seatsNo - a) / 2 == 0) {
            last_y = (int) ((transY * Math.floor((seatsNo - a) / 2)) * scale);
        } else {
            last_y = (int) ((transY * Math.floor((seatsNo - a + 1) / 2)) * scale);
        }
        if (point >= a) {
            setFocusGradation(canvas);
        }
        setFmBackground(canvas, a, seatsNo);
        drawMemberName(canvas, tableType);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////角テーブル２/////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    public void drawParallelTable(Canvas canvas) {

        Paint cPaint, sPaint;
        int tableHeight = (int) ((130 * Math.round((seatsNo + 1) / 2) + 150) * scale);

        // 机
        cPaint = new Paint();
        cPaint.setColor(Color.parseColor("#000000"));
        cPaint.setStrokeWidth(10 * scale);
        cPaint.setAntiAlias(true);
        cPaint.setStyle(Paint.Style.STROKE);
        float rectRight=width-180*scale;
        canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight, cPaint);
        cPaint.setColor(Color.parseColor("#90ffffff"));
        cPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(180 * scale, 100 * scale, rectRight, tableHeight, cPaint);

        //椅子
        sPaint = new Paint();
        sPaint.setColor(Color.parseColor("#000000"));
        sPaint.setStrokeWidth(7 * scale);
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.STROKE);
        r = 50 * scale;
        int transY = 130;

        //間隔
        int i = 0, j = 0;
        while (i < seatsNo / 2) {
            canvas.drawCircle(100 * scale, 190 * scale, r, sPaint);
            canvas.translate(0, transY * scale);
            x.add((double) 100 * scale);
            y.add((double) (transY * i + 190) * scale);
            i++;
        }
        canvas.translate(0, (-transY * i) * scale);
        while (i < seatsNo) {
            canvas.drawCircle(rectRight+80 * scale, 190 * scale, r, sPaint);
            canvas.translate(0, transY * scale);
            x.add((double) rectRight+80 * scale);
            y.add((double) (transY * j + 190) * scale);
            i++;
            j++;
        }
        last_x = 0;
        last_y = (int) ((transY * Math.floor(seatsNo / 2)) * scale);
        if (seatsNo == 1) {
            last_y = (int) (transY * scale);
        } else if (y.get(0) - last_y != 190) {
            last_y = (int) ((transY * Math.floor((seatsNo + 1) / 2)) * scale);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////丸テーブル///////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    public void drawCircleTable(Canvas canvas) {
        Paint cPaint, sPaint;
        float center_y,center_x;

        // 円
        cPaint = new Paint();
        cPaint.setColor(Color.parseColor("#000000"));
        cPaint.setStrokeWidth(10 * scale);
        cPaint.setAntiAlias(true);
        cPaint.setStyle(Paint.Style.STROKE);
        // (x,y,r,paint) 中心座標(x,y), 半径r
        center_x=width/2;
        canvas.drawCircle(center_x, 430 * scale, (float) (center_x*0.625), cPaint);
        cPaint.setColor(Color.parseColor("#90ffffff"));
        cPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center_x, 430 * scale, (float) (center_x*0.625), cPaint);

        //椅子
        sPaint = new Paint();
        sPaint.setColor(Color.parseColor("#000000"));
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.STROKE);
        //回転軸(x,y)
        canvas.translate(center_x, 430 * scale);
        last_x = (int)center_x;
        last_y = (int) (430 * scale);
        if (seatsNo < 16) {
            r = 50 * scale;
            center_y = (float) (-(center_x*0.625)-80*scale);
            sPaint.setStrokeWidth(7 * scale);
        } else if (seatsNo < 21) {
            r = 40 * scale;
            center_y = (float) (-(center_x*0.625)-60*scale);
            sPaint.setStrokeWidth(5 * scale);
        } else {
            r = 30 * scale;
            center_y = (float) (-(center_x*0.625)-50*scale);
            sPaint.setStrokeWidth(3 * scale);
        }


        for (int i = 0; i < seatsNo; i++) {
            canvas.drawCircle(0, center_y, r, sPaint);
            x.add(-center_y * Math.cos(Math.toRadians(-270 - 360 * i / (float) seatsNo))+center_x);
            y.add(center_y * Math.sin(Math.toRadians(-270 - 360 * i / (float) seatsNo)) + 430 * scale);
            //(度)ずつ回転し描画
            canvas.rotate(360 / (float) seatsNo);
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////カウンター//////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


    public void drawCounterTable(Canvas canvas) {
        //椅子
        Paint sPaint = new Paint();
        sPaint.setColor(Color.parseColor("#000000"));
        sPaint.setStrokeWidth(7 * scale);
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.STROKE);
        r = 50 * scale;
        int transY = 130;
        last_x = 0;
        last_y = (int) ((transY * seatsNo) * scale);

        //間隔
        for (int i = 0; i < seatsNo; i++) {
            canvas.drawCircle(160 * scale, 150 * scale, r, sPaint);
            canvas.translate(0, transY * scale);
            x.add((double) 160 * scale);
            y.add((double) (transY * i + 150) * scale);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////メソッド////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void memberNo(int position) {
        if (Normalmode) {
            seatsNo = arrayArrayNormal.get(position).size();
        } else {
            seatsNo = arrayArrayQuick.get(position).size();
        }
    }

    public void initialName(Canvas canvas, int i, float X, float Y) {
        String name_initial;
        if (Normalmode) {
            name_initial = String.valueOf(arrayArrayNormal.get(position).get(i).getName().charAt(0));
        } else {
            name_initial = arrayArrayQuick.get(position).get(i).substring(5);
        }
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(r);
        float textWidth = textPaint.measureText(name_initial);
        float textStartX = X - textWidth / 2;
        float textMinY = Y + 18 * scale;
        canvas.drawText(name_initial, textStartX, textMinY, textPaint);
    }

    public void setFmBackground(Canvas canvas, int startNo, int endNo) {
        Paint sPaint = new Paint();
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.FILL);

        for (int i = startNo; i < endNo; i++) {
            if (Normalmode) {
                if (arrayArrayNormal.get(position).get(i).getSex().equals("男")) {
                    sPaint.setColor(getResources().getColor(R.color.thin_man));
                } else {
                    sPaint.setColor(getResources().getColor(R.color.thin_woman));
                }
            } else {
                if (arrayArrayQuick.get(position).get(i).matches(".*" + "♠" + ".*")) {
                    sPaint.setColor(getResources().getColor(R.color.thin_man));
                } else if (arrayArrayQuick.get(position).get(i).matches(".*" + "♡" + ".*")) {
                    sPaint.setColor(getResources().getColor(R.color.thin_woman));
                } else {
                    sPaint.setColor(getResources().getColor(R.color.thin_white));
                }
            }

            double xP = x.get(i) - last_x, yP = y.get(i) - last_y;
            canvas.drawCircle((float) xP, (float) yP, r, sPaint);
            initialName(canvas, i, (float) xP, (float) yP);
        }
    }

    public void setFocusGradation(Canvas canvas) {
        double xP = x.get(point) - last_x, yP = y.get(point) - last_y;
        RadialGradient gradient = new RadialGradient((float) xP, (float) yP, (float) 1.5 * r, Color.parseColor("#ffaa00"),
                Color.argb(0, 0, 0, 0), android.graphics.Shader.TileMode.CLAMP);
        Paint GraPaint = new Paint();
        GraPaint.setDither(true);
        ///端を滑らかにする。
        GraPaint.setShader(gradient);
        canvas.drawCircle((float) xP, (float) yP, (float) 1.5 * r, GraPaint);
        GraPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        GraPaint.reset();
        GraPaint.setAntiAlias(true);
        GraPaint.setStyle(Paint.Style.FILL);
        GraPaint.setColor(Color.WHITE);
        canvas.drawCircle((float) xP, (float) yP, r, GraPaint);
    }

    public void drawMemberName(Canvas canvas, String type) {
        double xP = x.get(point) - last_x, yP = y.get(point) - last_y;
        int textSize = (int) (60 * scale);
        float textWidth, textStartX, textY, balloonStartX, balloonStartY, balloonEndX, balloonEndY;
        // 文字列用ペイントの生成
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(60 * scale);
        String text;
        if (Normalmode) {
            text = arrayArrayNormal.get(position).get(point).getName();
        } else {
            text = arrayArrayQuick.get(position).get(point);
        }
        textWidth = textPaint.measureText(text);
        while (textWidth > 320) {
            textPaint.setTextSize(textSize);
            textWidth = textPaint.measureText(text);
            textSize--;
        }
        while (textWidth > 320*scale) {
            textPaint.setTextSize(textSize);
            textWidth = textPaint.measureText(text);
            textSize--;
        }
        textPaint.setColor(Color.WHITE);


        if (type.equals("square")) {
            textY = (float) (double) (yP + 22*scale);
            textStartX = 200 * scale;
            if (point < square_no) {
                textY = (float) yP + 150 * scale;
            } else if (point < a) {
                textY = (float) yP - 110 * scale;
            } else if (point < (seatsNo + a) / 2) {
                textStartX = (float) (double) (xP + 140 * scale);
            } else {
                textStartX = (float) (double) (xP - 90 * scale - textWidth - r);
            }
            balloonStartX = textStartX - r;
            balloonEndX = textStartX + textWidth + r;
            balloonStartY = textY - r - 15 * scale;
            balloonEndY = textY + r - 25 * scale;
        } else if (type.equals("parallel")) {
            if (point < seatsNo / 2) {
                textStartX = (float) (double) (xP + 140 * scale);
            } else {
                textStartX = (float) (double) (xP - 90 * scale - textWidth - r);
            }
            textY = (float) (double) (yP + 22 * scale);
            balloonStartX = textStartX - r;
            balloonEndX = textStartX + textWidth + r;
            balloonStartY = textY - r - 15 * scale;
            balloonEndY = textY + r - 25 * scale;
        } else if (type.equals("circle")) {
// 文字列の幅からX座標を計算
            textStartX = -textWidth / 2;
// 文字列の高さからY座標を計算
            textY = (60 / 2) * scale;
            balloonStartX = textStartX - 20 * scale;
            balloonEndX = -textStartX + 20 * scale;
            balloonStartY = -textY - 5 * scale;
            balloonEndY = textY + 20 * scale;
        } else {
            textStartX = (float) (double) (xP + 150 * scale);
            textY = (float) (double) (yP + 22 * scale);
            balloonStartX = textStartX - r;
            balloonEndX = textStartX + textWidth + r;
            balloonStartY = textY - r - 15 * scale;
            balloonEndY = textY + r - 25 * scale;
        }

// 吹き出し用ペイントの生成
        Paint balloonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        balloonPaint.setTextSize(35 * scale);
        if (Normalmode) {
            if (arrayArrayNormal.get(position).get(point).getSex().equals("男")) {
                balloonPaint.setColor(getResources().getColor(R.color.man));
            } else {
                balloonPaint.setColor(getResources().getColor(R.color.woman));
            }
        } else {
            if (arrayArrayQuick.get(position).get(point).matches(".*" + "♠" + ".*")) {
                balloonPaint.setColor(getResources().getColor(R.color.man));
            } else if (arrayArrayQuick.get(position).get(point).matches(".*" + "♡" + ".*")) {
                balloonPaint.setColor(getResources().getColor(R.color.woman));
            } else {
                balloonPaint.setColor(getResources().getColor(R.color.green));
            }
        }

        RectF balloonRectF = new RectF(balloonStartX, balloonStartY, balloonEndX, balloonEndY);
        canvas.drawRoundRect(balloonRectF, 30 * scale, 30 * scale, balloonPaint);

// 文字列の描画
        canvas.drawText(text, textStartX, textY, textPaint);
    }

}

