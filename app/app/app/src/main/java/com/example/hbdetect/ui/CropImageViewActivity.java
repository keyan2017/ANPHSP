package com.example.hbdetect.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.hbdetect.MyApplication;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.BitmapClippingView;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.FileNotFoundException;

public class CropImageViewActivity extends AppCompatActivity {

    private CropImageView cropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image_view);

        // 获取传递的 Bitmap
        Uri currentSession = getIntent().getParcelableExtra("currentSessionBitmap");
        Bitmap bitmap= null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(currentSession));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BitmapClippingView my_cavas=(BitmapClippingView)findViewById(R.id.my_cavas);
//把刚刚本地相册拿到的bitmap传进去，用户进行自定义裁剪
        my_cavas.setBitmap(bitmap,3,4);
        Button button=findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap resulBitmap=my_cavas.getBitmap(CropImageViewActivity.this,600,800);
                MyApplication myApplication = (MyApplication) getApplicationContext();
                myApplication.setPreprocessedBitmap(resulBitmap);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        // 在这里处理用户按下返回按钮的逻辑
        // 例如，弹出提示框、执行特定操作等
        //super.onBackPressed();
        finish();
        super.onBackPressed();
    }

}