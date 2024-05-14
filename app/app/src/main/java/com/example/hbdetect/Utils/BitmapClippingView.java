package com.example.hbdetect.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * 自定义View: Bitmap自定义裁剪工具
 * @Author 绝命三郎
 */
public class BitmapClippingView extends View {

    public BitmapClippingView(Context context) {
        this(context,null);
    }

    public BitmapClippingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BitmapClippingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float widthSize;//布局宽度
    private float heightSize;//布局高度

    private Bitmap bitmap;//要裁剪的位图
    private float bitmapWidth;//位图原始宽度
    private float bimapHeight;//位图原始高度
    private float proportionWidth;//比例：宽  如裁图比例3:4，此处传3
    private float proportionHeight;//比例：高  如裁图比例3:4，此处传4

    private Paint bitmapPaint;//图片画笔
    private Paint shadowPaint;//阴影画笔
    private Paint linePaint;//线条画笔

    float scaleStep;//缩放比例

    private boolean initTag=true;//用于判断是不是首次绘制
    private float leftLine=-1;//选区左线
    private float topLine=-1;//选区上线
    private float rightLine=-1;//选区右线
    private float bottomLine=-1;//选区下线

    private String focus="NONE";//事件焦点
    private final String BODY="BODY";//BODY：拖动整体
    private final String NONE="NONE";//NONE:释放焦点


    private final String LEFT_TOP = "LEFT_TOP";
    private final String TOP = "TOP";
    private final String RIGHT_TOP = "RIGHT_TOP";
    private final String LEFT = "LEFT";
    private final String LEFT_BOTTOM = "LEFT_BOTTOM";
    private final String BOTTOM = "BOTTOM";
    private final String RIGHT_BOTTOM = "RIGHT_BOTTOM";
    private final String RIGHT = "RIGHT";


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        widthSize=MeasureSpec.getSize(widthMeasureSpec);
        heightSize=MeasureSpec.getSize(heightMeasureSpec);

        bitmapPaint=new Paint();
        bitmapPaint.setStrokeWidth(0);

        shadowPaint=new Paint();
        shadowPaint.setColor(Color.parseColor("#57FF9800"));
        shadowPaint.setStrokeWidth(4);
        shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        linePaint=new Paint();
        linePaint.setColor(Color.parseColor("#FF9800"));
        linePaint.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap==null)return;

        //绘制参考背景背景
        scaleStep=widthSize/bitmapWidth;
        float backgroundImgHeight=bimapHeight*scaleStep;
        heightSize=backgroundImgHeight;//把有效图像高度设置作为布局高度计算
        Rect rect=new Rect(0,0,(int)bitmapWidth,(int)bimapHeight);//裁剪图片中的部分(此处：全图)
        RectF rectF=new RectF(0,0,widthSize,backgroundImgHeight);//显示在屏幕中的什么位置
        canvas.drawBitmap(bitmap,rect,rectF,bitmapPaint);
        canvas.save();

        if (initTag){
            //绘制初始状态的选框（最大选框）
            if (bitmapWidth>bimapHeight){
                //宽大于高，取高
                float checkboxHeight=backgroundImgHeight/2f;//选框的高
                float checkboxWidth=((checkboxHeight/proportionHeight)*proportionWidth)/2f;//选框的宽
                leftLine=(widthSize/2f)-(checkboxWidth/2f);
                topLine=(heightSize/2f)-(checkboxHeight/2f);
                rightLine=(widthSize/2f)+(checkboxWidth/2f);
                bottomLine=(heightSize/2f)+(checkboxHeight/2f);
            }else {
                //高大于宽 取宽
                float checkboxWidth=widthSize/2f;//选框的宽
                float checkboxHeight=(widthSize/proportionWidth)*proportionHeight/2f;//选框的高
                leftLine=(widthSize/2f)-(checkboxWidth/2f);
                topLine=(heightSize/2f)-(checkboxHeight/2f);
                rightLine=(widthSize/2f)+(checkboxWidth/2f);
                bottomLine=(heightSize/2f)+(checkboxHeight/2f);
            }
            initTag=false;
        }

        //绘制选择的区域
        //绘制周边阴影部分（分四个方块）
        linePaint.setColor(Color.parseColor("#FF9800"));
        linePaint.setStrokeWidth(4);
        canvas.drawRect(0,0,leftLine,heightSize,shadowPaint);//左
        canvas.drawRect(leftLine+4,0,rightLine-4,topLine,shadowPaint);//上
        canvas.drawRect(rightLine,0,widthSize,heightSize,shadowPaint);//右
        canvas.drawRect(leftLine+4,bottomLine,rightLine-4,heightSize,shadowPaint);//下

        //绘制选区边缘线
        canvas.drawLine(leftLine,topLine,rightLine,topLine,linePaint);
        canvas.drawLine(rightLine,topLine,rightLine,bottomLine,linePaint);
        canvas.drawLine(rightLine,bottomLine,leftLine,bottomLine,linePaint);
        canvas.drawLine(leftLine,bottomLine,leftLine,topLine,linePaint);

        //绘制左上和右下调节点
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(6);
        canvas.drawLine(rightLine-4,bottomLine-4,rightLine-4,bottomLine-40-4,linePaint);
        canvas.drawLine(rightLine-4,bottomLine-4,rightLine-40-4,bottomLine-4,linePaint);
        canvas.drawLine(leftLine+4,topLine+4,leftLine+40+4,topLine+4,linePaint);
        canvas.drawLine(leftLine+4,topLine+4,leftLine+4,topLine+40+4,linePaint);

        //绘制焦点圆
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(rightLine-4,bottomLine-4,80,linePaint);
        canvas.drawCircle(leftLine+4,topLine+4,80,linePaint);

        //绘制扇形
        linePaint.setColor(Color.parseColor("#57FF0000"));
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF mRectF = new RectF(rightLine-4-40, bottomLine-4-40, rightLine-4+40, bottomLine-4+40);
        canvas.drawArc(mRectF, 270, 270, true, linePaint);

        linePaint.setColor(Color.parseColor("#57FF0000"));
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF mRectF2 = new RectF(leftLine+4-40, topLine+4-40, leftLine+4+40, topLine+4+40);
        canvas.drawArc(mRectF2, 90, 270, true, linePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if (leftLine==-1)return false;
        if (topLine==-1)return false;
        if (rightLine==-1)return false;
        if (bottomLine==-1)return false;
        if (bitmap==null)return false;

        float touchX=event.getX();
        float touchY=event.getY();




        if (event.getAction()==MotionEvent.ACTION_DOWN){
            return actionDown(touchX,touchY);
        }

        if (event.getAction()==MotionEvent.ACTION_MOVE){
            return actionMove(touchX,touchY);
        }

        if (event.getAction()==MotionEvent.ACTION_UP){
            return actionUp(touchX,touchY);
        }

        return true;
    }

    //抬起
    private boolean actionUp(float touchX, float touchY) {
        Log.d("fxHou","抬起X="+touchX+"   touchY="+touchY);
        Log.d("fxHou","释放焦点");
        focus=NONE;//释放焦点
        return true;
    }

    //移动
    private boolean actionMove(float touchX, float touchY) {
        Log.d("fxHou","滑动X="+touchX+"   touchY="+touchY);
        if (focus.equals(LEFT_TOP)){
            //移动边线
            leftLine=touchX;
            topLine=bottomLine-(((rightLine-leftLine)/proportionWidth)*proportionHeight);//约束比例

            //限制最小矩形 宽
            if (rightLine-leftLine<100){
                leftLine=rightLine-100;
                topLine=bottomLine-(((rightLine-leftLine)/proportionWidth)*proportionHeight);
                //重绘
                postInvalidate();
                return true;
            }

            //限制最小矩形 高
            if (bottomLine-topLine<100){
                topLine=bottomLine-100;
                leftLine=rightLine-((bottomLine-topLine)/proportionHeight)*proportionWidth;
                //重绘
                postInvalidate();
                return true;
            }

            //防止超出边界
            if (leftLine<0){
                leftLine=0;
                topLine=bottomLine-(((rightLine-leftLine)/proportionWidth)*proportionHeight);
            }

            //防止超出边界
            if (topLine<0){
                topLine=0;
                leftLine=rightLine-((bottomLine-topLine)/proportionHeight)*proportionWidth;
            }

            //重绘
            postInvalidate();
            return true;
        }else if (focus.equals(RIGHT_BOTTOM)){
            //移动边线
            rightLine=touchX;
            bottomLine=topLine+(((rightLine-leftLine)/proportionWidth)*proportionHeight);//约束比例

            //限制最小矩形 宽
            if (rightLine-leftLine<100){
                rightLine=leftLine+100;
                bottomLine=topLine+(((rightLine-leftLine)/proportionWidth)*proportionHeight);
                //重绘
                postInvalidate();
                return true;
            }

            //限制最小矩形 高
            if (bottomLine-topLine<100){
                bottomLine=topLine+100;
                rightLine=leftLine+(((bottomLine-topLine)/proportionHeight)*proportionWidth);
                //重绘
                postInvalidate();
                return true;
            }

            //防止超出边界
            if (rightLine>widthSize){
                rightLine=widthSize;
                bottomLine=topLine+(((rightLine-leftLine)/proportionWidth)*proportionHeight);
            }

            //防止超出边界
            if (bottomLine>heightSize){
                bottomLine=heightSize;
                rightLine=leftLine+(((bottomLine-topLine)/proportionHeight)*proportionWidth);
            }
            //重绘
            postInvalidate();
            return true;
        }else if (focus.equals(BODY)){
            float moveX=touchX-downX;
            float moveY=touchY-downY;
            leftLine=downLeftLine+moveX;
            rightLine=downRightLine+moveX;
            topLine=downTopLine+moveY;
            bottomLine=downBottomLine+moveY;

            if (leftLine<0){
                rightLine=(rightLine-leftLine);
                leftLine=0;

                if (topLine<0){
                    bottomLine=bottomLine-topLine;
                    topLine=0;
                    //重绘
                    postInvalidate();
                    return true;
                }

                if (bottomLine>heightSize){
                    topLine=heightSize-(bottomLine-topLine);
                    bottomLine=heightSize;
                    //重绘
                    postInvalidate();
                    return true;
                }

                //重绘
                postInvalidate();
                return true;
            }

            if (rightLine>widthSize){
                leftLine=widthSize-(rightLine-leftLine);
                rightLine=widthSize;

                if (topLine<0){
                    bottomLine=bottomLine-topLine;
                    topLine=0;
                    //重绘
                    postInvalidate();
                    return true;
                }

                if (bottomLine>heightSize){
                    topLine=heightSize-(bottomLine-topLine);
                    bottomLine=heightSize;
                    //重绘
                    postInvalidate();
                    return true;
                }

                //重绘
                postInvalidate();
                return true;
            }

            if (topLine<0){
                bottomLine=bottomLine-topLine;
                topLine=0;
                //重绘
                postInvalidate();
                return true;
            }

            if (bottomLine>heightSize){
                topLine=heightSize-(bottomLine-topLine);
                bottomLine=heightSize;
                //重绘
                postInvalidate();
                return true;
            }
            //重绘
            postInvalidate();
            return true;
        }
        return true;
    }

    //按下
    private float downX,downY,downLeftLine,downTopLine,downRightLine,downBottomLine;
    private boolean actionDown(float touchX, float touchY) {
        downX=touchX;
        downY=touchY;
        downLeftLine=leftLine;
        downTopLine=topLine;
        downRightLine=rightLine;
        downBottomLine=bottomLine;
        Log.d("fxHou","按下X="+touchX+"   touchY="+touchY);
        boolean condition1=touchX>leftLine-40 && touchX<leftLine+40;
        boolean condition2=touchY>topLine-40 && touchY<topLine+40;
        if (condition1 && condition2){
            Log.d("fxHou","左上获得焦点");
            focus=LEFT_TOP;//左上获得焦点
            return true;
        }

        boolean condition3=touchX>rightLine-40 && touchX<rightLine+40;
        boolean condition4=touchY>bottomLine-40 && touchY<bottomLine+40;
        if (condition3 && condition4){
            Log.d("fxHou","右下获得焦点");
            focus=RIGHT_BOTTOM;//右下获得焦点
            return true;
        }

        boolean condition5=touchX>leftLine && touchX<rightLine;
        boolean condition6=touchY>topLine && touchY<bottomLine;
        if (condition5 && condition6){
            Log.d("fxHou","整体获得焦点");
            focus=BODY;//整体获得焦点
            return true;
        }

        return true;
    }

    /**
     * 设置要裁剪的位图
     * @param bitmap 要裁剪的位图
     * @param proportionWidth  比例：宽  如裁图比例3:4，此处传3
     * @param proportionHeight 比例：高  如裁图比例3:4，此处传4
     */
    public void setBitmap(Bitmap bitmap,int proportionWidth,int proportionHeight){
        this.bitmap=bitmap;
        bitmapWidth=bitmap.getWidth();
        bimapHeight=bitmap.getHeight();
        this.proportionWidth=proportionWidth;
        this.proportionHeight=proportionHeight;
        initTag=true;
        postInvalidate();
    }

    /**
     * 获取裁剪后的位图
     * @param context
     * @param minPixelWidth 限制最小宽度（像素）
     * @param minPixelHeight 限制最小高度（像素）
     * @return 裁切后的位图
     */
    public Bitmap getBitmap(Context context,int minPixelWidth,int minPixelHeight){
        if (bitmap==null)return null;
        int startX= (int) (leftLine/scaleStep);
        int startY= (int) (topLine/scaleStep);
        int cutWidth=(int) ((rightLine/scaleStep)-(leftLine/scaleStep));
        int cutHeight=(int) (bottomLine/scaleStep-topLine/scaleStep);

        Bitmap newBitmap=Bitmap.createBitmap(bitmap,startX,startY,cutWidth,cutHeight,null,false);

//        if (newBitmap.getWidth()<minPixelWidth || newBitmap.getHeight()<minPixelHeight){
//            Toast.makeText(context, "图片太模糊了", Toast.LENGTH_SHORT).show();
//            return null;
//        }

        return newBitmap;
    }
}
