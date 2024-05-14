package com.example.hbdetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FaceDetectionView extends View {
    private Paint paint;
    private int rectLeft, rectTop, rectRight, rectBottom;

    public int getRectLeft() {
        return rectLeft;
    }

    public int getRectTop() {
        return rectTop;
    }

    public int getRectRight() {
        return rectRight;
    }

    public int getRectBottom() {
        return rectBottom;
    }

    public FaceDetectionView(Context context) {
        super(context);
        init();
    }

    public FaceDetectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    // 方法A，画一个宽80%，高50%的绿框 竖着的时候
    public void drawGreenBoxA() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // 计算绿框的位置和大小
        int rectWidth = (int) (viewWidth * 0.7);
        int rectHeight = (int) (viewHeight * 0.3);
        rectLeft = (viewWidth - rectWidth) / 2;
        rectTop = (viewHeight - rectHeight) / 2;
        rectRight = rectLeft + rectWidth;
        rectBottom = rectTop + rectHeight;

        // 重新绘制
        invalidate();
    }

    // 方法B，画一个宽为50%，高为80%的绿框   横着的时候
    public void drawGreenBoxB() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // 计算绿框的位置和大小
        int rectWidth = (int) (viewWidth * 0.5);
        int rectHeight = (int) (viewHeight * 0.5);
        rectLeft = (viewWidth - rectWidth) / 2;
        rectTop = (viewHeight - rectHeight) / 2;
        rectRight = rectLeft + rectWidth;
        rectBottom = rectTop + rectHeight;

        // 重新绘制
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制绿框
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
    }
}
