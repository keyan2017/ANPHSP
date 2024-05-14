package com.example.hbdetect.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import com.example.hbdetect.Eyes;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DNNUtils {

    public Bitmap getSource() {
        return bitmapImage;
    }
    private int c,h,w;
    private Bitmap bitmapImage;
    public Bitmap getPreprocessedImage() {
        return preprocessedImage;
    }

    private Bitmap preprocessedImage;

    public Bitmap getSegmentedImage() {
        return segmentedImage;
    }

    private Bitmap segmentedImage;
    public void init(Context context) throws FileNotFoundException {
        System.loadLibrary("hbdetect");
        String xmlpath = getPath("cascade.xml", context);
        String xmlpath2 = getPath("UNet.onnx", context);
        String xmlpath3 = getPath("DAAModel.onnx", context);
        loadModel(xmlpath,xmlpath2,xmlpath3);
        System.out.println(xmlpath);
        System.out.println(xmlpath2);
        System.out.println(xmlpath3);
        System.out.println("---------模型输入-----------");
    }

    public void setInput(Bitmap source){
        bitmapImage = Bitmap.createBitmap(source);
    }
    public int eval(Bitmap eyePic){
        int res = 0;
        float ans=0f;
        for(int x=0;x<eyePic.getWidth();x++){
            for(int y=0;y< eyePic.getHeight();y++){
                int bit = eyePic.getPixel(x,y);
                if(bit != 0xFFFFFFFF){
                    res++;
                }
            }
        }
        return res;
    }
    public static Bitmap copyBitmap(Bitmap bmSrc){
        Bitmap bmCopy = Bitmap.createBitmap(bmSrc.getWidth(), bmSrc.getHeight(), bmSrc.getConfig());
        Paint paint = new Paint();
        //第四步：用待绘制的 bitmap 创建一个画布 (canvas) 对象
        Canvas canvas = new Canvas(bmCopy);
        //第五步：以原图为模板，开始绘制。
        canvas.drawBitmap(bmSrc, new Matrix(), paint);
        return bmCopy;
    }

    public void process(boolean eyeB){
        if (!eyeB){
            preprocessedImage = bitmapImage;
            Bitmap seg = copyBitmap(bitmapImage);
            Segmentation(seg);
            segmentedImage = seg;
        }else{
            if(bitmapImage.getHeight() < 600 && bitmapImage.getWidth() < 600){
                preprocessedImage = bitmapImage;
                Bitmap seg = copyBitmap(bitmapImage);
                Segmentation(seg);
                segmentedImage = seg;
            }else {
                Eyes L = new Eyes();
                Eyes R = new Eyes();
                getOneEyeByIndex(bitmapImage,  L,R);
                if (L.width == 1) {
                    if (R.width == 1) {
                        //Detection failed
                        preprocessedImage = null;
                        segmentedImage = null;
                    } else {
                        //Only one eye
                        Bitmap rightEye = Bitmap.createBitmap(bitmapImage, R.x, R.y, R.width, R.height);
                        Bitmap rightSeg = copyBitmap(rightEye);
                        Segmentation(rightSeg);
                        if (eval(rightSeg) < 1000) {
                            //false-positive
                            preprocessedImage = null;
                            segmentedImage = null;
                        } else {
                            preprocessedImage = rightEye;
                            segmentedImage = rightSeg;
                        }
                    }
                } else {
                    //Two eyes succeed
                    Bitmap leftEye = Bitmap.createBitmap(bitmapImage, L.x, L.y, L.width, L.height);
                    Bitmap rightEye = Bitmap.createBitmap(bitmapImage, R.x, R.y, R.width, R.height);
                    Bitmap leftSeg = copyBitmap(leftEye);
                    Bitmap rightSeg = copyBitmap(rightEye);
                    Segmentation(leftSeg);
                    Segmentation(rightSeg);
                    int evalL = eval(leftSeg);
                    int evalR = eval(rightSeg);
                    if (evalL > evalR) {
                        if (evalL < 1000) {
                            //all false-positive
                            preprocessedImage = null;
                            segmentedImage = null;
                        } else {
                            preprocessedImage = leftEye;
                            segmentedImage = leftSeg;
                        }
                    } else {
                        if (evalR < 1000) {
                            //all false-positive
                            preprocessedImage = null;
                            segmentedImage = null;
                        } else {
                            preprocessedImage = rightEye;
                            segmentedImage = rightSeg;
                        }
                    }
                }
            }
        }
    }

    public String prediction(){
        if(segmentedImage != null) {
            return String.format("%.2f", Prediction(segmentedImage));
        }else{
            return "NaN";
        }
    }
    public static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i("ONNX", "Failed to upload a file");
        }
        return "";
    }
    /**
     * A native method that is implemented by the 'hbdetect' native library,
     * which is packaged with this application.
     */

    public native void getOneEyeByIndex(Bitmap bitmap, Eyes LEye,Eyes REye);
    public native void Segmentation(Bitmap bitmap);
    public native float Prediction(Bitmap bitmap);
    private native void loadModel(String path1,String path2,String path3);
}
