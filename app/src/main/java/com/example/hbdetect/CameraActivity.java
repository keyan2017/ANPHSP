package com.example.hbdetect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements  TextureView.SurfaceTextureListener{
    private OrientationEventListener mOrientationListener;
    private int morientation;
    private String[] cameraIds;
    private  String cameraId;
    private CameraManager cameraManager;//从系统服务中获取相机管理器
    private TextureView textureView;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder builder = null;
    private int currentCamera = 0;
    private Size mPreviewSize;  //最佳的预览尺寸
    private Size mCaptureSize;

    //2023-12-4   jc
    private Sensor rotationSensor;
    private SensorManager sensorManager;
    private SensorEventListener rotationListener;
    private float[] accelerometerValues = new float[3];
    private float[] magnetometerValues = new float[3];
    float roll;
    private FaceDetectionView faceDetectionView;
    //存储最终的结果
    private int degree=-1;//旋转度
    private float drawrect=0;//框的角度



    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,//相机
    };


    @SuppressLint({"MissingPermission", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //权限检查
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS,
                    ACTION_REQUEST_PERMISSIONS);
        }
        faceDetectionView=findViewById(R.id.faceDetectionView);
        textureView=findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);//设置布局
        ImageView capture_button = findViewById(R.id.capture_button);
        TextView back_button = findViewById(R.id.back_button);
        ImageView switch_button = findViewById(R.id.switch_button);

        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraDevice.close();
                //获取原图
                Bitmap bmp = textureView.getBitmap();
                Bitmap bmpXZ=null;
                //先进行旋转
                if(drawrect!=0){
                    //需要旋转再旋转
                    Matrix rotationMatrix = new Matrix();
                    rotationMatrix.postRotate(drawrect);
                    bmpXZ = Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),rotationMatrix,true);
                }
                //截图框内的图片
                Bitmap bmpCJ= Bitmap.createBitmap(bmp, faceDetectionView.getRectLeft(), faceDetectionView.getRectTop(), faceDetectionView.getRectRight() - faceDetectionView.getRectLeft(), faceDetectionView.getRectBottom() - faceDetectionView.getRectTop());
                //是否需要旋转
                if(drawrect!=0){
                    //需要旋转再旋转
                    Matrix rotationMatrix = new Matrix();
                    rotationMatrix.postRotate(drawrect);
                    bmpCJ = Bitmap.createBitmap(bmpCJ,0,0,bmpCJ.getWidth(),bmpCJ.getHeight(),rotationMatrix,true);
                }
                MyApplication myApplication = (MyApplication)getApplicationContext();
                myApplication.setCurrentSessionBitmap(bmpXZ==null?bmp:bmpXZ);
                myApplication.setPreprocessedBitmap(bmpCJ);
                finish();
            }

        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraDevice.close();
                finish();
            }

        });
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraDevice.close();
                if(currentCamera == 1){
                    currentCamera = 0;
                }else{
                    currentCamera = 1;
                }
                openCamera2();
            }

        });
    }

    /**
     * 权限检查
     * @param neededPermissions 需要的权限
     * @return 是否全部被允许
     */
    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        //布局初始化完成,打开相机
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            openCamera2();
            faceDetectionView.drawGreenBoxA();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera2(){
        //一、获取cameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraIds = cameraManager.getCameraIdList();//获取摄像机id的集合
            cameraId = cameraIds[currentCamera];
            setupCamera(textureView.getWidth(),textureView.getHeight());
            cameraManager.openCamera(cameraId,callback,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    CameraDevice.StateCallback callback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };
    private void setupCamera(int width, int height) throws CameraAccessException {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE); //启动一个系统服务

        //遍历手机系统中的摄像头，拿到摄像头ID
        for (String cameraID : cameraManager.getCameraIdList()) {
            //拿到当前摄像头的一些参数
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
            //获得当前摄像头的朝向
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            //如果当前摄像头朝前，那就这个就不要了，直接换下一个
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                continue;
            }

            //获得当前摄像头的分辨率
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {       //找到摄像头能够输出的。最符合我们当前显示器界面分辨率的最小值
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);  //括号里是预览界面的尺寸
                mCaptureSize=Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size o1, Size o2) {
                        return Long.signum(o1.getWidth()*o1.getHeight()-o2.getWidth()*o2.getHeight());
                    }
                });
            }
            break;
        }
    }


    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<Size>();     //一个存放分辨率的列表
        for (Size option : sizeMap) {
            if (width > height) {   //横屏
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {     //竖屏
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 1) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
                }
            });
        }
        return sizeMap[0];

    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        cameraCaptureSession.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(MyApplication.getInstance(), "Failed to create camera capture session", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACTION_REQUEST_PERMISSIONS: {
                // 如果请求被取消，结果数组将为空
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 授权成功，执行相关操作
                    //打开相机
                    openCamera2();
                } else {
                    // 授权失败，禁用相关功能或显示提示信息

                }
                return;
            }
            // 处理其他权限请求
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture  surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        // 注册方向回调，检测屏幕方向改变
        if (null == mOrientationListener) {
            mOrientationListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    morientation = orientation;
                    int thisdegree;

                    // 根据方向值执行相应的逻辑
                    if (orientation >= 0 && orientation < 75) {
                        thisdegree = 0;
                    } else if (orientation >= 75 && orientation < 135) {
                        thisdegree = -1;
                    } else if (orientation >= 135 && orientation < 225) {
                        thisdegree = 0;
                    } else if (orientation >= 225 && orientation < 315) {
                        thisdegree = 1;
                    } else {
                        thisdegree = 0;
                    }
                    // 判断方向是否发生变化
                    if (thisdegree != degree) {
                        degree = thisdegree;
                        // 根据方向绘制相应的绿框
                        switch (thisdegree) {
                            case 0:
                                // 设备处于竖直方向
                                faceDetectionView.drawGreenBoxA();
                                drawrect = 0;
                                break;
                            case -1:
                                // 设备处于左横屏方向
                                faceDetectionView.drawGreenBoxB();
                                drawrect = 90;
                                break;
                            case 1:
                                // 设备处于右横屏方向
                                faceDetectionView.drawGreenBoxB();
                                drawrect = -90;
                                break;
                            default:
                                // 其他情况
                                faceDetectionView.drawGreenBoxA();
                                break;
                        }
                    }
                }

            };
            mOrientationListener.enable();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (null != mOrientationListener) {
            mOrientationListener.disable();
            mOrientationListener = null;
        }
    }


}
