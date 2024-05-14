package com.example.hbdetect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.hbdetect.ui.home.HomeFragment;

import java.io.FileNotFoundException;

public class Mainactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        FragmentManager fm=getSupportFragmentManager();
        fm.beginTransaction().add(R.id.contents,new HomeFragment()).commit();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null) {
            if (intent.getData() != null) {
                Uri uri = intent.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Log.d("onActivityResult",requestCode+"-----------");
                MyApplication myApplication = (MyApplication) getApplicationContext();
                myApplication.setCurrentSessionBitmap(bitmap);
                myApplication.setUri(uri);
                myApplication.setPreprocessedBitmap(null);
            }
        }
    }
}
