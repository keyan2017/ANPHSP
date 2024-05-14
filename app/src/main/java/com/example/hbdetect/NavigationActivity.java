package com.example.hbdetect;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.Utils.PermissionUtil;
import com.example.hbdetect.Utils.SheetHelper;
import com.example.hbdetect.bean.Patient;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hbdetect.databinding.ActivityNavigationBinding;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int REQUEST_CODE = 12;
    private AppBarConfiguration mAppBarConfiguration;
    DatabaseHelper mDBHelper;
    private ActivityNavigationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication myApplication = MyApplication.getInstance();
        mDBHelper = new DatabaseHelper(MyApplication.getInstance(),myApplication.getDbname(),null,1);
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarNavigation.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        PermissionUtil.checkPermission(this,NEEDED_PERMISSIONS,REQUEST_CODE);
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
                MyApplication myApplication = (MyApplication) getApplicationContext();
                myApplication.setCurrentSessionBitmap(bitmap);
                myApplication.setUri(uri);
                myApplication.setPreprocessedBitmap(null);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
                try {
                    String[] title = {"病历号", "姓名", "年龄", "性别", "科室", "床号", "拍摄次数","拍摄时间","预测值","真实值"};
                    List<Patient> deviceInfos = new ArrayList<>();
                    deviceInfos = mDBHelper.selectAllPatients();
                    boolean isSuccess = SheetHelper.exportExcel(title, deviceInfos, MyApplication.getInstance().getDir(), MyApplication.getInstance().getOutputname(), NavigationActivity.this, true,mDBHelper);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isSuccess) {
                                Toast.makeText(NavigationActivity.this, "导出成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NavigationActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:
                if (PermissionUtil.checkGrant(grantResults)){
                    SheetHelper.initPath(MyApplication.getInstance().getDir(),MyApplication.getInstance());
                }else{
                    Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}